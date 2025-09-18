package com.commander4j.util;


import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * Reads VERSIONINFO string fields (e.g., FileDescription) from a PE (EXE/DLL)
 * using Unicode-safe Win32 APIs via JNA.
 */
public final class WinExeMetadata {

    private WinExeMetadata() {}

    /** Returns Explorer's friendly name (File description) or null. */
    public static String getFileDescription(Path exe) { return queryStringField(exe, "FileDescription"); }

    public static String getProductName(Path exe)     { return queryStringField(exe, "ProductName"); }
    public static String getCompanyName(Path exe)     { return queryStringField(exe, "CompanyName"); }
    public static String getProductVersion(Path exe)  { return queryStringField(exe, "ProductVersion"); }
    public static String getFileVersion(Path exe)     { return queryStringField(exe, "FileVersion"); }
    public static String getOriginalFilename(Path exe){ return queryStringField(exe, "OriginalFilename"); }
    public static String getInternalName(Path exe)    { return queryStringField(exe, "InternalName"); }
    public static String getLegalCopyright(Path exe)  { return queryStringField(exe, "LegalCopyright"); }

    /** Core query that is fully Unicode-safe. */
    public static String queryStringField(Path exe, String fieldName) {
        Objects.requireNonNull(exe, "exe path");
        Objects.requireNonNull(fieldName, "fieldName");
        final String path = exe.toAbsolutePath().toString();

        // Get buffer size
        IntByReference dummyHandle = new IntByReference();
        int size = VersionW.INSTANCE.GetFileVersionInfoSizeW(new WString(path), dummyHandle);
        if (size <= 0) return null;

        // Load version resource into memory
        Memory buffer = new Memory(size);
        boolean ok = VersionW.INSTANCE.GetFileVersionInfoW(new WString(path), 0, size, buffer);
        if (!ok) return null;

        // Try to read the translation table: pairs of (LANGID, CodePage) as WORDs
        PointerByReference pTrans = new PointerByReference();
        IntByReference cbTrans = new IntByReference();
        boolean haveTrans = VersionW.INSTANCE.VerQueryValueW(
                buffer, new WString("\\VarFileInfo\\Translation"), pTrans, cbTrans);

        // Candidates to try: discovered lang/codepage + sensible fallbacks
        int[][] candidates;

        if (haveTrans && pTrans.getValue() != null && cbTrans.getValue() >= 4) {
            Pointer p = pTrans.getValue();
            @SuppressWarnings("unused")
			int count = cbTrans.getValue() / 4; // 4 bytes per entry (2 WORDs)
            // Take the first entry, but we’ll also add common fallbacks
            int lang0 = p.getShort(0) & 0xFFFF;
            int cp0   = p.getShort(2) & 0xFFFF;

            candidates = new int[][]{
                {lang0,   cp0},
                {lang0,   0x04B0},   // same language, Unicode
                {0x0409,  0x04B0},   // en-US, Unicode (common)
                {0x0409,  0x04E4}    // en-US, Windows (legacy)
            };
        } else {
            // No translation info -> use well-known fallbacks
            candidates = new int[][]{
                {0x0409,  0x04B0},   // en-US, Unicode
                {0x0409,  0x04E4}    // en-US, Windows
            };
        }

        for (int[] lc : candidates) {
            String v = queryWithLangCp(buffer, fieldName, lc[0], lc[1]);
            if (v != null && !v.isBlank()) return v.trim();
        }

        // As a last resort, try any present translation entries if there were multiple
        if (haveTrans && pTrans.getValue() != null && cbTrans.getValue() >= 8) {
            Pointer p = pTrans.getValue();
            int count = cbTrans.getValue() / 4;
            for (int i = 1; i < count; i++) {
                int lang = p.getShort(i * 4) & 0xFFFF;
                int cp   = p.getShort(i * 4 + 2) & 0xFFFF;
                String v = queryWithLangCp(buffer, fieldName, lang, cp);
                if (v != null && !v.isBlank()) return v.trim();
            }
        }

        return null;
    }

    private static String queryWithLangCp(Pointer buffer, String field, int lang, int cp) {
        String subBlock = String.format(Locale.ROOT, "\\StringFileInfo\\%04x%04x\\%s", lang, cp, field);
        PointerByReference pVal = new PointerByReference();
        IntByReference len = new IntByReference();

        boolean ok = VersionW.INSTANCE.VerQueryValueW(buffer, new WString(subBlock), pVal, len);
        if (!ok || pVal.getValue() == null || len.getValue() <= 1) return null;

        // Returned pointer is a wide string inside the version resource block.
        return pVal.getValue().getWideString(0);
    }

    /** Tiny CLI demo for quick testing. */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: WinExeMetadata <path-to-exe-or-dll>");
            return;
        }
        Path exe = Path.of(args[0]);
        System.out.println("Path            : " + exe.toAbsolutePath());
        System.out.println("FileDescription : " + getFileDescription(exe));
        System.out.println("ProductName     : " + getProductName(exe));
        System.out.println("CompanyName     : " + getCompanyName(exe));
        System.out.println("ProductVersion  : " + getProductVersion(exe));
        System.out.println("FileVersion     : " + getFileVersion(exe));
        System.out.println("OriginalFilename: " + getOriginalFilename(exe));
    }
}
