package com.commander4j.util;


import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/** Utilities for reading ICNS files (largest frame). */
public final class ICNSUtils {
    public ICNSUtils() {}

    public static BufferedImage readLargestIcon(File icns) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(icns)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) return null;
            ImageReader r = readers.next();
            r.setInput(iis, true, true);
            int count = r.getNumImages(true);
            BufferedImage best = null;
            int bestArea = -1;
            for (int i = 0; i < count; i++) {
                BufferedImage img = r.read(i);
                int area = img.getWidth() * img.getHeight();
                if (area > bestArea) {
                    bestArea = area;
                    best = img;
                }
            }
            r.dispose();
            return best;
        }
    }
}
