package com.commander4j.dnd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;



/** Parsed metadata for a .app bundle (macOS). */
public final class JDragDropAppInfo
{
	public static String Type_appBundle  = "macosBundle";
	public static String Type_bashScript = "bashScript";
	public static String Type_windowsEXE = "windowsEXE";

	public enum IconSource
	{
		CF_BUNDLE_ICON_FILE, HEURISTIC_RESOURCES_SCAN, NONE
	}

	public final String bundleType;
	public final Path bundlePath; // /Applications/Safari.app
	public final String bundleId; // e.g., com.apple.Safari
	public final String bundleName; // display name
	public final String executableName; // from CFBundleExecutable
	public final Path executablePath; // Contents/MacOS/<exec>

	/** Chosen .icns path for the app icon (may be null). */
	public final Path iconIcnsPath;

	/** Where the icon decision came from. */
	public final IconSource iconSource;

	private JDragDropAppInfo(String bundleType, Path bundlePath, String bundleId, String bundleName, String executableName, Path executablePath, Path iconIcnsPath, IconSource iconSource)
	{
		this.bundleType = bundleType;
		this.bundlePath = bundlePath;
		this.bundleId = bundleId;
		this.bundleName = bundleName;
		this.executableName = executableName;
		this.executablePath = executablePath;
		this.iconIcnsPath = iconIcnsPath;
		this.iconSource = iconSource;

	}

	public static JDragDropAppInfo fromBundle(Path bundle) throws Exception
	{
		Objects.requireNonNull(bundle, "bundle");
		
		if (!Files.isDirectory(bundle) || !bundle.toString().toLowerCase(Locale.ROOT).endsWith(".app"))
		{
			throw new IllegalArgumentException("Not an .app bundle: " + bundle);
		}

		Path contents = bundle.resolve("Contents");
		Path info = contents.resolve("Info.plist");
		if (!Files.isRegularFile(info))
		{
			throw new IOException("Missing Info.plist in " + bundle);
		}

		NSDictionary dict = (NSDictionary) PropertyListParser.parse(info.toFile());

		String bundleId = str(dict, "CFBundleIdentifier", "");
		
		String name = firstNonEmpty(str(dict, "CFBundleDisplayName", null), str(dict, "CFBundleName", null), trimApp(bundle.getFileName().toString()));

		String execName = str(dict, "CFBundleExecutable", null);
		
		Path execPath = execName != null ? contents.resolve("MacOS").resolve(execName) : null;

		Path resources = contents.resolve("Resources");

		// === STRICT CFBundleIconFile handling ===
		String iconFile = str(dict, "CFBundleIconFile", null);
		
		if (iconFile != null && !iconFile.isBlank())
		{
			if (!iconFile.toLowerCase(Locale.ROOT).endsWith(".icns"))
			{
				iconFile = iconFile + ".icns";
			}
			
			Path candidate = resources.resolve(iconFile);
			
			if (Files.isRegularFile(candidate))
			{

				return new JDragDropAppInfo(Type_appBundle, bundle, bundleId, name, execName, execPath, candidate, IconSource.CF_BUNDLE_ICON_FILE);
			}
		}

		// === Fallback: scan Resources/*.icns and pick best by heuristic +
		// largest rendition ===

		Path iconPath = Paths.get(iconFile);

		return new JDragDropAppInfo(Type_appBundle, bundle, bundleId, name, execName, execPath, iconPath, iconPath != null ? IconSource.HEURISTIC_RESOURCES_SCAN : IconSource.NONE);
	}

	public static JDragDropAppInfo fromScript(Path bundle) throws Exception
	{
		Objects.requireNonNull(bundle, "script");
		
		if (Files.isDirectory(bundle) || !bundle.toString().toLowerCase(Locale.ROOT).endsWith(".sh"))
		{
			throw new IllegalArgumentException("Not an bash script: " + bundle);
		}

		String bundleId = bundle.getFileName().toString();
		String name = bundle.getFileName().toString();

		String execName = bundle.getFileName().toString();
		Path execPath = bundle.getParent();

		Path iconPath = Paths.get("."+File.separator+"images"+File.separator+"appIcons"+File.separator+"terminal_24x24.png");

		return new JDragDropAppInfo(Type_bashScript, bundle, bundleId, name, execName, execPath, iconPath, iconPath != null ? IconSource.HEURISTIC_RESOURCES_SCAN : IconSource.NONE);
	}
	
	public static JDragDropAppInfo fromEXE(Path bundle) throws Exception
	{
		Objects.requireNonNull(bundle, "exe");
		
		if (Files.isDirectory(bundle) || !bundle.toString().toLowerCase(Locale.ROOT).endsWith(".exe"))
		{
			throw new IllegalArgumentException("Not an windows executable : " + bundle);
		}

		String bundleId = bundle.getFileName().toString();
		String name = bundle.getFileName().toString();

		String execName = bundle.getFileName().toString();
		Path execPath = bundle.getParent();

		Path iconPath = Paths.get("."+File.separator+"images"+File.separator+"appIcons"+File.separator+execName+".png");

		return new JDragDropAppInfo(Type_windowsEXE, bundle, bundleId, name, execName, execPath, iconPath, iconPath != null ? IconSource.HEURISTIC_RESOURCES_SCAN : IconSource.NONE);
	}

	private static String str(NSDictionary d, String key, String def)
	{
		NSObject v = d.objectForKey(key);
		return v == null ? def : v.toString();
	}

	private static String firstNonEmpty(String... vals)
	{
		for (String v : vals)
			if (v != null && !v.isBlank())
				return v;
		return "";
	}

	private static String trimApp(String s)
	{
		return s.endsWith(".app") ? s.substring(0, s.length() - 4) : s;
	}

}
