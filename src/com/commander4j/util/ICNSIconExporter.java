package com.commander4j.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.commander4j.dnd.JDragDropAppInfo;

public final class ICNSIconExporter {

	public ICNSIconExporter() {}

	/** Entry point: same signature/behaviour as before. */
	public boolean exportAppIconPng(JDragDropAppInfo info, Path outputFolder, int sizePx) throws IOException {
		boolean result = false;

		// 1) Prefer embedded PNG/JP2 reps from ICNS blocks (most reliable)
		BufferedImage bestInFile = readClosestEncodedFromIcnsBlocks(info.iconIcnsPath.toFile(), sizePx);

		// 2) Fallback to ICNS reader (still skip mask/blackish)
		if (bestInFile == null) {
			bestInFile = readClosestImageFromIcnsSkippingBlack(info.iconIcnsPath.toFile(), sizePx);
		}

		if (bestInFile != null) {
			bestInFile = toSRGB(bestInFile);
			BufferedImage scaled = scaleTo(info, bestInFile, sizePx);

			Files.createDirectories(outputFolder);
			String appName = stripDotApp(info.bundlePath.getFileName().toString());
			Path outPng = outputFolder.resolve(appName + ".png");

			if (ImageIO.write(scaled, "png", outPng.toFile())) {
				result = true;
			}
		}

		return result;
	}

	private String stripDotApp(String name) {
		return name.endsWith(".app") ? name.substring(0, name.length() - 4) : name;
	}

	/* ===================== Path A: Parse ICNS blocks & extract PNG/JP2 ===================== */

	private BufferedImage readClosestEncodedFromIcnsBlocks(File icnsFile, int sizePx) throws IOException {
		byte[] all = Files.readAllBytes(icnsFile.toPath());
		if (all.length < 8) return null;

		// Verify ICNS header
		if (!(all[0] == 'i' && all[1] == 'c' && all[2] == 'n' && all[3] == 's')) return null;

		int fileLen = toIntBE(all, 4);
		int pos = 8;

		List<BufferedImage> candidates = new ArrayList<>();
		@SuppressWarnings("unused")
		int pngCount = 0, jp2Count = 0;

		while (pos + 8 <= all.length && pos < fileLen) {
			int blockStart = pos;
			@SuppressWarnings("unused")
			String type = ascii(all, pos, 4);
			int blockLen = toIntBE(all, pos + 4);
			if (blockLen < 8 || blockStart + blockLen > all.length) break;

			int payloadStart = blockStart + 8;
			int payloadLen   = blockLen - 8;

			// Try PNG
			if (payloadLen >= 8 && isPngSig(all, payloadStart)) {
				BufferedImage img = decodeWithImageIO(all, payloadStart, payloadLen);
				if (img != null && !looksBlackOrTransparent(img)) {
					candidates.add(img);
				}
				pngCount++;
			}
			// Try JPEG2000: either JP2 signature box (12 bytes) or raw codestream (FF 4F FF 51)
			else if (isJp2Sig(all, payloadStart, payloadLen) || isJp2CodestreamSig(all, payloadStart, payloadLen)) {
				BufferedImage img = decodeWithImageIO(all, payloadStart, payloadLen); // requires JP2 plugin (e.g., TwelveMonkeys)
				if (img != null && !looksBlackOrTransparent(img)) {
					candidates.add(img);
				}
				jp2Count++;
			}

			pos += blockLen;
		}

		// DEBUG (optional)
		// System.out.printf("ICNS blocks: PNG=%d JP2=%d candidates=%d%n", pngCount, jp2Count, candidates.size());

		if (candidates.isEmpty()) return null;
		return pickClosestByWidthPreferAlpha(candidates, sizePx);
	}

	/* ===================== Path B: ICNS reader fallback (skip blackish) ===================== */

	private BufferedImage readClosestImageFromIcnsSkippingBlack(File icnsFile, int sizePx) throws IOException {
		try (ImageInputStream iis = ImageIO.createImageInputStream(icnsFile)) {
			if (iis == null) return null;

			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			if (!readers.hasNext()) return null;

			ImageReader reader = readers.next();
			try {
				reader.setInput(iis, false, false);
				int num = reader.getNumImages(true);

				BufferedImage bestAtLeast = null; int bestAtLeastW = Integer.MAX_VALUE; boolean bestAtLeastAlpha = false;
				BufferedImage bestBelow   = null; int bestBelowW   = -1;                 boolean bestBelowAlpha   = false;

				for (int i = 0; i < num; i++) {
					BufferedImage img = reader.read(i);
					if (img == null) continue;
					if (looksBlackOrTransparent(img)) continue; // skip mask/dud frames

					final int w = img.getWidth();
					final boolean hasAlpha = img.getColorModel().hasAlpha();

					if (w >= sizePx) {
						if (w < bestAtLeastW || (w == bestAtLeastW && hasAlpha && !bestAtLeastAlpha)) {
							bestAtLeast = img; bestAtLeastW = w; bestAtLeastAlpha = hasAlpha;
						}
					} else {
						if (w > bestBelowW || (w == bestBelowW && hasAlpha && !bestBelowAlpha)) {
							bestBelow = img; bestBelowW = w; bestBelowAlpha = hasAlpha;
						}
					}
				}
				return (bestAtLeast != null) ? bestAtLeast : bestBelow;
			}
			finally {
				reader.dispose();
			}
		}
	}

	/* ===================== Scaling (unchanged alpha-safe pipeline) ===================== */

	private BufferedImage scaleTo(JDragDropAppInfo info, BufferedImage srcIn, int maxSize) {
		BufferedImage src = toARGB(srcIn);

		final int w = src.getWidth();
		final int h = src.getHeight();
		final double s = Math.min(maxSize / (double) w, maxSize / (double) h);
		final int targetW = Math.max(1, (int) Math.round(w * s));
		final int targetH = Math.max(1, (int) Math.round(h * s));

		if (targetW == w && targetH == h) {
			return toARGB(src);
		}

		BufferedImage work = src;
		int curW = w, curH = h;

		while (curW / 2 >= targetW && curH / 2 >= targetH) {
			int nextW = Math.max(targetW, curW / 2);
			int nextH = Math.max(targetH, curH / 2);

			BufferedImage down = new BufferedImage(nextW, nextH, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = down.createGraphics();
			g2.setComposite(AlphaComposite.Src);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.drawImage(work, 0, 0, nextW, nextH, null);
			g2.dispose();

			if (work != src) work.flush();
			work = down;
			curW = nextW; curH = nextH;
		}

		if (work.getWidth() != targetW || work.getHeight() != targetH) {
			BufferedImage exact = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = exact.createGraphics();
			g.setComposite(AlphaComposite.Src);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			AffineTransform at = AffineTransform.getScaleInstance(
					targetW / (double) work.getWidth(),
					targetH / (double) work.getHeight());
			g.drawRenderedImage(work, at);
			g.dispose();

			work.flush();
			work = exact;
		}

		// Optional sharpen only if fully opaque
		double overallScale = Math.min((double) targetW / w, (double) targetH / h);
		if (overallScale < 0.5 && isOpaque(work)) {
			float[] kernel = { 0f, -1f, 0f, -1f, 5f, -1f, 0f, -1f, 0f };
			ConvolveOp sharpen = new ConvolveOp(new Kernel(3, 3, kernel), ConvolveOp.EDGE_NO_OP, null);
			BufferedImage sharp = new BufferedImage(work.getWidth(), work.getHeight(), BufferedImage.TYPE_INT_ARGB);
			sharpen.filter(work, sharp);
			work.flush();
			work = sharp;
		}

		// Mirror write to your cache path (same behaviour as before)
		Path outputPath = Paths.get("./images/appIcons/" + info.bundleName + ".png");
		try { ImageIO.write(work, "png", outputPath.toFile()); } catch (IOException e) { e.printStackTrace(); }

		return work;
	}

	/* ===================== Utilities ===================== */

	private static BufferedImage pickClosestByWidthPreferAlpha(List<BufferedImage> imgs, int sizePx) {
		BufferedImage bestAtLeast = null; int bestAtLeastW = Integer.MAX_VALUE; boolean bestAtLeastAlpha = false;
		BufferedImage bestBelow = null;   int bestBelowW   = -1;                 boolean bestBelowAlpha   = false;

		for (BufferedImage img : imgs) {
			if (img == null) continue;
			if (looksBlackOrTransparent(img)) continue;

			int w = img.getWidth();
			boolean alpha = img.getColorModel().hasAlpha();

			if (w >= sizePx) {
				if (w < bestAtLeastW || (w == bestAtLeastW && alpha && !bestAtLeastAlpha)) {
					bestAtLeast = img; bestAtLeastW = w; bestAtLeastAlpha = alpha;
				}
			} else {
				if (w > bestBelowW || (w == bestBelowW && alpha && !bestBelowAlpha)) {
					bestBelow = img; bestBelowW = w; bestBelowAlpha = alpha;
				}
			}
		}
		return (bestAtLeast != null) ? bestAtLeast : bestBelow;
	}

	private static boolean looksBlackOrTransparent(BufferedImage img) {
		int w = img.getWidth(), h = img.getHeight();
		int stepX = Math.max(1, w / 16), stepY = Math.max(1, h / 16);
		int total = 0, dud = 0;
		for (int y = 0; y < h; y += stepY) {
			for (int x = 0; x < w; x += stepX) {
				int argb = img.getRGB(x, y);
				int a = (argb >>> 24) & 0xFF;
				int r = (argb >>> 16) & 0xFF;
				int g = (argb >>> 8) & 0xFF;
				int b = (argb) & 0xFF;
				total++;
				if (a < 4 || (r < 6 && g < 6 && b < 6)) dud++;
			}
		}
		return total > 0 && dud * 100 / total >= 95;
	}

	private static boolean isOpaque(BufferedImage img) {
		return !img.getColorModel().hasAlpha();
	}

	private static BufferedImage toARGB(BufferedImage src) {
		if (src == null) return null;
		if (src.getType() == BufferedImage.TYPE_INT_ARGB) return src;
		BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dst.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(src, 0, 0, null);
		g.dispose();
		return dst;
	}

	private static BufferedImage toSRGB(BufferedImage src) {
		if (src == null) return null;
		BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dst.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(src, 0, 0, null); // convert to default sRGB
		g.dispose();
		return dst;
	}

	private static boolean isPngSig(byte[] a, int off) {
		return off + 8 <= a.length &&
				(a[off] & 0xFF) == 0x89 && a[off + 1] == 0x50 && a[off + 2] == 0x4E && a[off + 3] == 0x47 &&
				a[off + 4] == 0x0D && a[off + 5] == 0x0A && a[off + 6] == 0x1A && a[off + 7] == 0x0A;
	}

	private static boolean isJp2Sig(byte[] a, int off, int len) {
		// JP2 signature box: 00 00 00 0C 6A 50 20 20 0D 0A 87 0A
		return len >= 12 && off + 12 <= a.length &&
				a[off] == 0x00 && a[off + 1] == 0x00 && a[off + 2] == 0x00 && a[off + 3] == 0x0C &&
				a[off + 4] == 0x6A && a[off + 5] == 0x50 && a[off + 6] == 0x20 && a[off + 7] == 0x20 &&
				a[off + 8] == 0x0D && a[off + 9] == 0x0A && (a[off + 10] & 0xFF) == 0x87 && a[off + 11] == 0x0A;
	}

	private static boolean isJp2CodestreamSig(byte[] a, int off, int len) {
		// raw codestream: FF 4F FF 51
		return len >= 4 && off + 4 <= a.length &&
				(a[off] & 0xFF) == 0xFF && (a[off + 1] & 0xFF) == 0x4F &&
				(a[off + 2] & 0xFF) == 0xFF && (a[off + 3] & 0xFF) == 0x51;
	}

	private static BufferedImage decodeWithImageIO(byte[] a, int off, int len) {
		try (ByteArrayInputStream bin = new ByteArrayInputStream(a, off, len)) {
			return ImageIO.read(bin); // decodes PNG and (if plugin present) JPEG 2000
		} catch (IOException e) {
			return null;
		}
	}

	private static int toIntBE(byte[] a, int off) {
		if (off + 4 > a.length) return 0;
		return ByteBuffer.wrap(a, off, 4).order(ByteOrder.BIG_ENDIAN).getInt();
	}

	private static String ascii(byte[] a, int off, int len) {
		if (off < 0 || off + len > a.length) return "";
		StringBuilder sb = new StringBuilder(len);
		for (int i = off; i < off + len; i++) sb.append((char) (a[i] & 0xFF));
		return sb.toString();
	}
}
