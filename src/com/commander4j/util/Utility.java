package com.commander4j.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

public class Utility
{
	
	public String getFontDisplayName(Font fnt)
	{
		String result = "";
		
		result = fnt.getFamily();
		result = result + "," + parseFontStyle(fnt.getStyle());
		result = result + "," + String.valueOf(fnt.getSize());
		
		return result;
	}
	
	public int parseFontStyle(String style)
	{
		int result = Font.PLAIN;
		
		switch (style)
		{
			case "Plain":
				result = Font.PLAIN;
				break;
			case "Bold":
				result = Font.BOLD;
				break;
			case "Italic":
				result = Font.ITALIC;
				break;
			case "Bold Italic":
				result = Font.BOLD | Font.ITALIC;
				break;
		}
		
		return result;
	}

	public String parseFontStyle(int style)
	{		
		String result = "Plain";
		
		switch (style)
		{
			case Font.PLAIN:
				result = "Plain";
				break;
			case Font.BOLD:
				result = "Bold";
				break;
			case Font.ITALIC:
				result = "Italic";
				break;
		}
		
		if (style == (Font.BOLD | Font.ITALIC))
		{
			result = "Bold Italic";
		}
		
		return result;
	}
	
	public File stringToPath(String path )
	{
		File result = null;
		
		Path pathDirectory = Paths.get(path);
		boolean directoryValid = Files.exists(pathDirectory);
		
		if (directoryValid)
		{
			result = new File(path);
		}
		
		return result;
	}
	
	public static void setLookandFeel()
	{

		try
		{
			SetLookAndFeel("Metal", "Ocean");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void SetLookAndFeel(String LOOKANDFEEL, String THEME)
	{
		try
		{
			if (LOOKANDFEEL.equals("Metal"))
			{
				if (THEME.equals("DefaultMetal"))
					MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
				else if (THEME.equals("Ocean"))
					MetalLookAndFeel.setCurrentTheme(new OceanTheme());

				UIManager.setLookAndFeel(new MetalLookAndFeel());

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void setLookAndFeel(String LAF)
	{
		try
		{
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			{
				if (LAF.equals(info.getName()))
				{
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}
		catch (Exception e)
		{

		}
	}
	
	public static GraphicsDevice getGraphicsDevice()
	{
		GraphicsDevice result;

		Point mouseLocation = MouseInfo.getPointerInfo().getLocation();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		GraphicsDevice[] devices;

		try
		{
			devices = ge.getScreenDevices();

			GraphicsDevice currentDevice = null;

			for (GraphicsDevice device : devices)
			{
				Rectangle bounds = device.getDefaultConfiguration().getBounds();
				if (bounds.contains(mouseLocation))
				{
					currentDevice = device;
					break;
				}
			}

			GraphicsDevice[] gs = ge.getScreenDevices();

			String defaultID = currentDevice.getIDstring();

			int monitorIndex = 0;

			for (int x = 0; x < gs.length; x++)
			{
				if (gs[x].getIDstring().equals(defaultID))
				{
					monitorIndex = x;
					break;
				}
			}

			result = gs[monitorIndex];
		}
		catch (HeadlessException ex)
		{
			result = null;
		}

		return result;
	}
	
	public static int getOSWidthAdjustment()
	{
		int result = 0;
		if (OSValidator.isWindows())
		{
			result = 0;
		}
		if (OSValidator.isMac())
		{
			result = -15;
		}
		if (OSValidator.isSolaris())
		{
			result = 0;
		}
		if (OSValidator.isUnix())
		{
			result = 0;
		}
		return result;
	}
	
	public static int getOSHeightAdjustment()
	{
		int result = 0;
		if (OSValidator.isWindows())
		{
			result = 0;
		}
		if (OSValidator.isMac())
		{
			result = -13;
		}
		if (OSValidator.isSolaris())
		{
			result = 0;
		}
		if (OSValidator.isUnix())
		{
			result = 0;
		}
		return result;
	}
	
	public String replaceNullObjectwithBlank(Object value)
	{
		String result = "";

		if (value != null)
		{
			result = value.toString();
		}

		return result;
	}

	public String replaceNullStringwithBlank(String value)
	{
		if (value == null)
		{
			value = "";
		}

		return value;
	}
	
	public String padSpace(int size)
	{
		String s = "";

		for (int i = 0; i < size; i++)
		{
			s = s + " ";
		}

		return s;
	}

	public String padString(int size, String character)
	{
		String s = "";

		for (int i = 0; i < size; i++)
		{
			s = s + character;
		}

		return s;
	}
	
	public String padString(String input, boolean right, int size, String character)
	{
		int inputlength = 0;
		String result = replaceNullStringwithBlank(input);

		inputlength = result.length();

		if (inputlength > size)
		{
			// result = result.substring(0,size-1);
			result = result.substring(0, size);
		} else
		{
			if (inputlength < size)
			{
				if (right == true)
				{
					result = result + padString(size - inputlength, character);
				} else
				{
					result = padString(size - inputlength, character) + result;
				}
			}
		}

		return result;
	}
	
	public String left(String inputstr, int size)
	{
		String result = replaceNullStringwithBlank(inputstr);

		if (size > inputstr.length())
		{
			size = inputstr.length();
		}

		if (size >= 0)
		{
			result = inputstr.substring(0, size);
		}
		else
		{
			result = "";
		}

		return result;
	}
	
    public  String toHex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    public  Color fromHex(String hex) {
        if (hex == null || !hex.matches("#?[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException("Invalid hex color format. Expected #RRGGBB.");
        }

        // Remove the # if present
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        int red = Integer.parseInt(hex.substring(0, 2), 16);
        int green = Integer.parseInt(hex.substring(2, 4), 16);
        int blue = Integer.parseInt(hex.substring(4, 6), 16);

        return new Color(red, green, blue);
    }
}

