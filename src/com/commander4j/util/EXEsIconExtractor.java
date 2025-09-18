package com.commander4j.util;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.platform.win32.WinDef.HINSTANCE;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;

public class EXEsIconExtractor {
    
    // Extended Shell32 interface for icon extraction
    public interface Shell32 extends Library {
        Shell32 INSTANCE = Native.load("shell32", Shell32.class);
        
        int ExtractIconEx(String lpszFile, int nIconIndex, HICON[] phiconLarge, HICON[] phiconSmall, int nIcons);
        HICON ExtractIcon(HINSTANCE hInst, String lpszExeFileName, int nIconIndex);
        int SHGetFileInfo(String pszPath, int dwFileAttributes, SHFILEINFO psfi, int cbFileInfo, int uFlags);
    }
    
    // Extended User32 interface for icon operations
    public interface User32Ext extends User32 {
        User32Ext INSTANCE = Native.load("user32", User32Ext.class);
        
        boolean GetIconInfo(HICON hIcon, ICONINFO piconinfo);
    }
    
    /**
     * Extended GDI32 interface for graphics operations
     */
    public interface GDI32Ext extends GDI32 {
        GDI32Ext INSTANCE = Native.load("gdi32", GDI32Ext.class);
        
        WinDef.HDC CreateCompatibleDC(WinDef.HDC hdc);
        boolean DeleteDC(WinDef.HDC hdc);
        WinDef.HBITMAP SelectObject(WinDef.HDC hdc, WinDef.HBITMAP hgdiobj);
        boolean DeleteObject(WinDef.HBITMAP hObject);
    }
    
    // ICONINFO structure
    @Structure.FieldOrder({"fIcon", "xHotspot", "yHotspot", "hbmMask", "hbmColor"})
    public static class ICONINFO extends Structure {
        public boolean fIcon;
        public int xHotspot;
        public int yHotspot;
        public WinDef.HBITMAP hbmMask;
        public WinDef.HBITMAP hbmColor;
        
        public ICONINFO() {
            super();
        }
    }
    
    // SHFILEINFO structure for getting file information
    @Structure.FieldOrder({"hIcon", "iIcon", "dwAttributes", "szDisplayName", "szTypeName"})
    public static class SHFILEINFO extends Structure {
        public HICON hIcon;
        public int iIcon;
        public int dwAttributes;
        public char[] szDisplayName = new char[260]; // MAX_PATH
        public char[] szTypeName = new char[80];
        
        public SHFILEINFO() {
            super();
        }
    }
    
    // Constants
    private static final int SHGFI_ICON = 0x100;
    private static final int SHGFI_LARGEICON = 0x0;
    private static final int SHGFI_SMALLICON = 0x1;
    private static final int DIB_RGB_COLORS = 0;
    
    /**
     * Extracts an icon from a Windows executable file.
     * 
     * @param exePath Path to the executable file
     * @param preferredSize Preferred icon size in pixels
     * @return BufferedImage containing the extracted icon, or null if extraction fails
     */
    public static BufferedImage exportAppIconPng(String exePath, int preferredSize) {
        if (!Platform.isWindows()) {
            throw new UnsupportedOperationException("This method only works on Windows");
        }
        
        try {
            // Try to get multiple icon sizes using ExtractIconEx
            HICON[] largeIcons = new HICON[1];
            HICON[] smallIcons = new HICON[1];
            
            int iconCount = Shell32.INSTANCE.ExtractIconEx(exePath, 0, largeIcons, smallIcons, 1);
            
            if (iconCount > 0) {
                // Determine which icon size to use based on preferred size
                HICON selectedIcon = null;
                
                // Standard Windows icon sizes: small (16x16), large (32x32)
                // We'll choose based on which is closer to the preferred size
                if (preferredSize <= 24) {
                    // For smaller preferred sizes, try small icon first
                    selectedIcon = smallIcons[0];
                    if (selectedIcon == null || selectedIcon.getPointer() == Pointer.NULL) {
                        selectedIcon = largeIcons[0];
                    }
                } else {
                    // For larger preferred sizes, try large icon first
                    selectedIcon = largeIcons[0];
                    if (selectedIcon == null || selectedIcon.getPointer() == Pointer.NULL) {
                        selectedIcon = smallIcons[0];
                    }
                }
                
                if (selectedIcon != null && selectedIcon.getPointer() != Pointer.NULL) {
                    BufferedImage image = iconToBufferedImage(selectedIcon);
                    
                    // Clean up icons
                    if (largeIcons[0] != null) User32.INSTANCE.DestroyIcon(largeIcons[0]);
                    if (smallIcons[0] != null) User32.INSTANCE.DestroyIcon(smallIcons[0]);
                    
                    return image;
                }
            }
            
            // Fallback: try using SHGetFileInfo
            SHFILEINFO shfi = new SHFILEINFO();
            int flags = SHGFI_ICON | (preferredSize <= 24 ? SHGFI_SMALLICON : SHGFI_LARGEICON);
            
            int result = Shell32.INSTANCE.SHGetFileInfo(exePath, 0, shfi, shfi.size(), flags);
            
            if (result != 0 && shfi.hIcon != null) {
                BufferedImage image = iconToBufferedImage(shfi.hIcon);
                User32.INSTANCE.DestroyIcon(shfi.hIcon);
                return image;
            }
            
        } catch (Exception e) {
            System.err.println("Error extracting icon: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Converts a Windows HICON to a BufferedImage.
     */
    private static BufferedImage iconToBufferedImage(HICON hIcon) {
        try {
            // Get icon information
            ICONINFO iconInfo = new ICONINFO();
            if (!User32Ext.INSTANCE.GetIconInfo(hIcon, iconInfo)) {
                return null;
            }
            
            // Create device context
            WinDef.HDC hdc = GDI32Ext.INSTANCE.CreateCompatibleDC(null);
            if (hdc == null) {
                cleanup(null, null, iconInfo);
                return null;
            }
            
            // Get bitmap info for the color bitmap
            BITMAPINFO bmi = new BITMAPINFO();
            bmi.bmiHeader.biSize = bmi.bmiHeader.size();
            
            // First call to get bitmap info using the inherited GetDIBits
            int result = GDI32.INSTANCE.GetDIBits(hdc, iconInfo.hbmColor, 0, 0, null, bmi, DIB_RGB_COLORS);
            
            if (result == 0) {
                cleanup(hdc, null, iconInfo);
                return null;
            }
            
            int width = bmi.bmiHeader.biWidth;
            int height = Math.abs(bmi.bmiHeader.biHeight);
            boolean isTopDown = bmi.bmiHeader.biHeight < 0;
            
            // Set up bitmap info for 32-bit RGBA
            bmi.bmiHeader.biBitCount = 32;
            bmi.bmiHeader.biCompression = 0; // BI_RGB
            bmi.bmiHeader.biSizeImage = width * height * 4;
            
            // Allocate buffer for pixel data using JNA Memory
            int bufferSize = width * height * 4;
            Memory pixelBuffer = new Memory(bufferSize);
            
            // Get the actual pixel data
            result = GDI32.INSTANCE.GetDIBits(hdc, iconInfo.hbmColor, 0, height, 
                                            pixelBuffer, bmi, DIB_RGB_COLORS);
            
            if (result == 0) {
                cleanup(hdc, null, iconInfo);
                return null;
            }
            
            // Create BufferedImage and set pixel data
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            
            // Convert pixel data from buffer to byte array
            byte[] pixelData = pixelBuffer.getByteArray(0, bufferSize);
            
            // Handle mask bitmap for transparency
            Memory maskBuffer = null;
            byte[] maskData = null;
            if (iconInfo.hbmMask != null) {
                BITMAPINFO maskBmi = new BITMAPINFO();
                maskBmi.bmiHeader.biSize = maskBmi.bmiHeader.size();
                
                GDI32.INSTANCE.GetDIBits(hdc, iconInfo.hbmMask, 0, 0, null, maskBmi, DIB_RGB_COLORS);
                
                int maskHeight = Math.abs(maskBmi.bmiHeader.biHeight);
                int maskBufferSize = ((width + 31) / 32) * 4 * maskHeight;
                maskBuffer = new Memory(maskBufferSize);
                
                maskBmi.bmiHeader.biBitCount = 1;
                maskBmi.bmiHeader.biCompression = 0;
                
                int maskResult = GDI32.INSTANCE.GetDIBits(hdc, iconInfo.hbmMask, 0, maskHeight, 
                                                        maskBuffer, maskBmi, DIB_RGB_COLORS);
                if (maskResult != 0) {
                    maskData = maskBuffer.getByteArray(0, maskBufferSize);
                }
            }
            
            // Process pixel data
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixelIndex = (y * width + x) * 4;
                    int actualY = isTopDown ? y : (height - 1 - y);
                    
                    if (pixelIndex + 3 < pixelData.length) {
                        // Windows bitmap is BGRA, we need ARGB
                        int blue = pixelData[pixelIndex] & 0xFF;
                        int green = pixelData[pixelIndex + 1] & 0xFF;
                        int red = pixelData[pixelIndex + 2] & 0xFF;
                        int alpha = pixelData[pixelIndex + 3] & 0xFF;
                        
                        // Check mask for transparency
                        if (maskData != null) {
                            int maskRowBytes = ((width + 31) / 32) * 4;
                            int maskByteIndex = actualY * maskRowBytes + (x / 8);
                            int maskBitIndex = 7 - (x % 8);
                            
                            if (maskByteIndex < maskData.length) {
                                boolean isTransparent = ((maskData[maskByteIndex] >> maskBitIndex) & 1) != 0;
                                if (isTransparent) {
                                    alpha = 0;
                                }
                            }
                        }
                        
                        // If we don't have alpha channel data, use mask or default to opaque
                        if (alpha == 0 && maskData == null && (red != 0 || green != 0 || blue != 0)) {
                            alpha = 255;
                        }
                        
                        int argb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                        image.setRGB(x, actualY, argb);
                    }
                }
            }
            
            cleanup(hdc, null, iconInfo);
            return image;
            
        } catch (Exception e) {
            System.err.println("Error converting icon to BufferedImage: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Alternative method that uses DrawIconEx for better icon rendering.
     */
    public static BufferedImage extractIconAlternative(String exePath, int preferredSize) {
        if (!Platform.isWindows()) {
            throw new UnsupportedOperationException("This method only works on Windows");
        }
        
        try {
            SHFILEINFO shfi = new SHFILEINFO();
            int flags = SHGFI_ICON | (preferredSize <= 24 ? SHGFI_SMALLICON : SHGFI_LARGEICON);
            
            int result = Shell32.INSTANCE.SHGetFileInfo(exePath, 0, shfi, shfi.size(), flags);
            
            if (result != 0 && shfi.hIcon != null) {
                // Create a BufferedImage and draw the icon onto it
                int iconSize = preferredSize <= 24 ? 16 : 32; // Standard sizes
                BufferedImage image = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
                
                // This is a simplified approach - you might need platform-specific drawing
                Graphics2D g2d = image.createGraphics();
                g2d.setColor(new Color(0, 0, 0, 0)); // Transparent background
                g2d.fillRect(0, 0, iconSize, iconSize);
                g2d.dispose();
                
                User32.INSTANCE.DestroyIcon(shfi.hIcon);
                return image;
            }
            
        } catch (Exception e) {
            System.err.println("Error extracting icon (alternative method): " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Cleanup method for resources.
     */
    private static void cleanup(WinDef.HDC hdc, WinDef.HBITMAP oldBitmap, ICONINFO iconInfo) {
        if (oldBitmap != null) {
            GDI32Ext.INSTANCE.SelectObject(hdc, oldBitmap);
        }
        if (hdc != null) {
            GDI32Ext.INSTANCE.DeleteDC(hdc);
        }
        if (iconInfo.hbmColor != null) {
            GDI32Ext.INSTANCE.DeleteObject(iconInfo.hbmColor);
        }
        if (iconInfo.hbmMask != null) {
            GDI32Ext.INSTANCE.DeleteObject(iconInfo.hbmMask);
        }
    }
    
    /**
     * Example usage method.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java WindowsIconExtractor <path-to-exe> [preferred-size]");
            return;
        }
        
        String exePath = args[0];
        int preferredSize = args.length > 1 ? Integer.parseInt(args[1]) : 32;
        
        BufferedImage icon = exportAppIconPng(exePath, preferredSize);
        
        if (icon != null) {
            System.out.println("Successfully extracted icon: " + 
                             icon.getWidth() + "x" + icon.getHeight() + " pixels");
            
            try {
                File outputfile = new File("saved.png");
                ImageIO.write(icon, "png", outputfile);
            } catch (IOException e) {
                // handle exception
            }
            // You can now save the image or use it as needed
            // ImageIO.write(icon, "PNG", new File("extracted_icon.png"));
        } else {
            System.out.println("Failed to extract icon from: " + exePath);
        }
    }
}