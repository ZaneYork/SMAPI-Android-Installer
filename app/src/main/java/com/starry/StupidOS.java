package com.starry;

import android.annotation.TargetApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@TargetApi(26)
public class StupidOS {
    public static OutputStream getOutputStream(File file) throws IOException {
        try {
            return Files.newOutputStream(file.toPath(), java.nio.file.StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (java.nio.file.FileSystemException ignored) {
            return new FileOutputStream(file);
        }
    }
    public static InputStream getInputStream(File file) throws IOException {
        try {
            return Files.newInputStream(file.toPath(), StandardOpenOption.READ);
        }  catch (java.nio.file.FileSystemException ignored) {
            return new FileInputStream(file);
        }
    }
}
