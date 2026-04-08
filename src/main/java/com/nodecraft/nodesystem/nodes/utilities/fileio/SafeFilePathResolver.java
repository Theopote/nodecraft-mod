package com.nodecraft.nodesystem.nodes.utilities.fileio;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Restricts file I/O paths to the current workspace directory.
 */
final class SafeFilePathResolver {

    private static final Path ALLOWED_BASE_DIRECTORY = Paths.get("").toAbsolutePath().normalize();

    private SafeFilePathResolver() {
        // Utility class.
    }

    static Path resolveInAllowedDirectory(String rawPath) {
        Path candidate = Paths.get(rawPath);
        Path normalized = candidate.isAbsolute()
                ? candidate.toAbsolutePath().normalize()
                : ALLOWED_BASE_DIRECTORY.resolve(candidate).normalize();

        if (!normalized.startsWith(ALLOWED_BASE_DIRECTORY)) {
            throw new IllegalArgumentException("文件路径超出允许目录: " + rawPath);
        }

        return normalized;
    }
}