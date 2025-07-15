package com.aefyr.pseudoapksigner;


import android.content.Context;

import com.starry.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

    public static void copyFileFromAssets(Context context, String assetFileName, File destination) throws IOException {
        try (InputStream inputStream = context.getAssets().open(assetFileName);
             OutputStream outputStream = FileUtils.getOutputStream(destination)) {
            byte[] buf = new byte[1024 * 1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) outputStream.write(buf, 0, len);
        }
    }
}