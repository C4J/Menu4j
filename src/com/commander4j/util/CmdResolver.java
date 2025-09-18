package com.commander4j.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class CmdResolver {

    public static Path findCmdExe() {
        // 1) COMSPEC (best / authoritative)
        String comspec = System.getenv("ComSpec"); // windows env variable (case-insensitive)
        if (comspec != null && !comspec.isBlank()) {
            Path p = Paths.get(comspec);
            if (Files.isRegularFile(p)) return p;
        }

        // 2) SYSTEMROOT\System32\cmd.exe
        String systemRoot = System.getenv("SystemRoot"); // typically C:\Windows
        if (systemRoot != null && !systemRoot.isBlank()) {
            Path candidate = Paths.get(systemRoot, "System32", "cmd.exe");
            if (Files.isRegularFile(candidate)) return candidate;

            // 2a) If running a 32-bit JVM on 64-bit Windows, try Sysnative (gives real 64-bit System32)
            String procArchWow = System.getenv("PROCESSOR_ARCHITEW6432");
            if (procArchWow != null) {
                Path sysnative = Paths.get(systemRoot, "Sysnative", "cmd.exe");
                if (Files.isRegularFile(sysnative)) return sysnative;
            }
        }

        // 3) Fallback: look for "cmd.exe" on PATH entries
        String path = System.getenv("PATH");
        if (path != null) {
            String[] entries = path.split(";");
            for (String entry : entries) {
                if (entry == null || entry.isBlank()) continue;
                Path candidate = Paths.get(entry.trim(), "cmd.exe");
                if (Files.isRegularFile(candidate)) return candidate;
            }
        }

        // 4) Last resort: return just "cmd" (OS will resolve via PATH when executing)
        return Paths.get("cmd");
    }

    public static void main(String[] args) {
        Path cmd = findCmdExe();
        System.out.println("cmd.exe resolved to: " + cmd.toAbsolutePath());
    }
}
