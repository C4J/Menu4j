package com.commander4j.util;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.Native;

/** Wide-char (Unicode) bindings for the Win32 Version APIs. */
public interface VersionW extends StdCallLibrary {
    VersionW INSTANCE = Native.load("Version", VersionW.class);

    int  GetFileVersionInfoSizeW(WString lptstrFilename, IntByReference lpdwHandle);
    boolean GetFileVersionInfoW(WString lptstrFilename, int dwHandle, int dwLen, Pointer lpData);
    boolean VerQueryValueW(Pointer pBlock, WString lpSubBlock, PointerByReference lplpBuffer, IntByReference puLen);
}
