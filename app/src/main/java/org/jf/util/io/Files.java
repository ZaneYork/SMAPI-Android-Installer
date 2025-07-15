package org.jf.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class Files {

    public static File createTempDir() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = System.currentTimeMillis() + "-";

        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IllegalStateException(
                "Failed to create directory within "
                        + TEMP_DIR_ATTEMPTS
                        + " attempts (tried "
                        + baseName
                        + "0 to "
                        + baseName
                        + (TEMP_DIR_ATTEMPTS - 1)
                        + ')');
    }
    public static String readContent(File file) throws IOException{
        byte[] bytes = ByteStreams.toByteArray(new FileInputStream(file));
        return new String(bytes, 0, bytes.length, StandardCharsets.UTF_8);
    }
    public static String getNameWithoutExtension(String file) {
        String fileName = new File(file).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
    public static String getNameWithoutExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
    private static final int TEMP_DIR_ATTEMPTS = 10000;
}
