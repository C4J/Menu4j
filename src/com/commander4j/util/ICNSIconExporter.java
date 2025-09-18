package com.commander4j.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.commander4j.dnd.JDragDropAppInfo;
import com.twelvemonkeys.image.AffineTransformOp;

public final class ICNSIconExporter
{

	public ICNSIconExporter()
	{
	}

	/**
	 * Exports the icon from a macOS .app bundle as a PNG of the given size into
	 * the target folder. Output filename is the app bundle name (without .app)
	 * plus ".png".
	 *
	 * @param appBundle
	 *            Path to the .app bundle (e.g., /Applications/Safari.app)
	 * @param outputFolder
	 *            Path to a folder where the PNG should be saved (created if
	 *            missing)
	 * @param sizePx
	 *            target edge size in pixels (e.g., 24 for 24x24)
	 * @return the path to the written PNG
	 * @throws IOException
	 *             if anything goes wrong (no icon found, read/write issues,
	 *             etc.)
	 */
	public boolean exportAppIconPng(JDragDropAppInfo info, Path outputFolder, int sizePx) throws IOException
	{
		boolean result = false;

		Path outPng;

		BufferedImage bestInFile = readClosestImageFromIcns(info.iconIcnsPath.toFile(), sizePx);

		if (bestInFile != null)
		{
			BufferedImage scaled = scaleTo(info, bestInFile, sizePx);

			Files.createDirectories(outputFolder);

			String appName = stripDotApp(info.bundlePath.getFileName().toString());
			outPng = outputFolder.resolve(appName + ".png");

			if (ImageIO.write(scaled, "png", outPng.toFile()))
			{
				result = true;
			}
		}

		return result;
	}

	private String stripDotApp(String name)
	{
		return name.endsWith(".app") ? name.substring(0, name.length() - 4) : name;
	}


	/**
	 * Reads every image rep from an .icns and returns the largest one. Requires
	 * TwelveMonkeys ImageIO.
	 */
	private BufferedImage readClosestImageFromIcns(File icnsFile, int sizePx) throws IOException
	{
		try (ImageInputStream iis = ImageIO.createImageInputStream(icnsFile))
		{
			if (iis == null)
				return null;
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			if (!readers.hasNext())
				return null;
			ImageReader reader = readers.next();
			try
			{
				reader.setInput(iis, false, false);
				int num = reader.getNumImages(true);
				BufferedImage best = null;
				int over = 0;
				int closest = -1;
				for (int i = 0; i < num; i++)
				{
					BufferedImage img = reader.read(i);

					if (img != null)
					{
						System.out.println(img.getWidth());

						over = img.getWidth() - sizePx;

						if (((over > 0) && (over < closest)) || (closest < 0))
						{
							best = img;
							closest = over;
						}
					}
				}
				return best;
			}
			finally
			{
				reader.dispose();
			}
		}
	}

	/** High-quality ARGB scaling. */
	private BufferedImage scaleTo(JDragDropAppInfo info, BufferedImage src, int maxSize)
	{
		// --- 1) Compute target size (preserve aspect ratio, never exceed max)
		// ---
		final int w = src.getWidth();
		final int h = src.getHeight();
		final double s = Math.min(maxSize / (double) w, maxSize / (double) h);
		final int targetW = Math.max(1, (int) Math.round(w * s));
		final int targetH = Math.max(1, (int) Math.round(h * s));

		if (targetW == w && targetH == h)
		{
			return toARGB(src); // nothing to do
		}

		// --- 2) Convert to premultiplied ARGB to avoid edge halos on
		// transparency ---
		BufferedImage work = toARGBPre(src);

		// --- 3) Progressive downscale by ~2x steps (much cleaner than 1 big
		// jump) ---
		int curW = work.getWidth();
		int curH = work.getHeight();

		while (curW / 2 >= targetW && curH / 2 >= targetH)
		{
			curW = Math.max(targetW, curW / 2);
			curH = Math.max(targetH, curH / 2);

			BufferedImage down = new BufferedImage(curW, curH, BufferedImage.TYPE_INT_ARGB_PRE);
			Graphics2D g2 = down.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setComposite(AlphaComposite.Src); // copy alpha exactly
			g2.drawImage(work, 0, 0, curW, curH, null);
			g2.dispose();

			if (work != src)
				work.flush();
			work = down;
		}

		// --- 4) Final exact scale using AffineTransformOp (bicubic) ---
		if (work.getWidth() != targetW || work.getHeight() != targetH)
		{
			AffineTransform at = AffineTransform.getScaleInstance(targetW / (double) work.getWidth(), targetH / (double) work.getHeight());
			AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
			BufferedImage exact = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB_PRE);
			op.filter(work, exact);
			work.flush();
			work = exact;
		}

		// --- 5) Optional: light unsharp mask after big reductions to restore
		// micro-contrast ---
		if (s < 0.5)
		{ // only if we scaled down a lot
			float[] kernel =
			{ 0f, -1f, 0f, -1f, 5f, -1f, 0f, -1f, 0f };
			ConvolveOp sharpen = new ConvolveOp(new Kernel(3, 3, kernel), ConvolveOp.EDGE_NO_OP, null);
			BufferedImage sharp = new BufferedImage(work.getWidth(), work.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
			sharpen.filter(work, sharp);
			work.flush();
			work = sharp;
		}

		// --- 6) Convert back to non-premultiplied ARGB (nicer for ImageIO
		// PNGs) ---
		BufferedImage out = new BufferedImage(work.getWidth(), work.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = out.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(work, 0, 0, null);
		g.dispose();
		work.flush();

		// --- 7) Save (you can keep or move this out if you want pure “scale
		// only”) ---
		Path outputPath = Paths.get("./images/appIcons/" + info.bundleName + ".png");
		try
		{
			ImageIO.write(out, "png", outputPath.toFile());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return out;
	}

	// Helpers
	private BufferedImage toARGB(BufferedImage src)
	{
		if (src.getType() == BufferedImage.TYPE_INT_ARGB)
			return src;
		BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dst.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(src, 0, 0, null);
		g.dispose();
		return dst;
	}

	private BufferedImage toARGBPre(BufferedImage src)
	{
		if (src.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
			return src;
		BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g = dst.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(src, 0, 0, null);
		g.dispose();
		return dst;
	}

}
