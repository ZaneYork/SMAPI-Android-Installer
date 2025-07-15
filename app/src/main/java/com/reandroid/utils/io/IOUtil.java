/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.utils.io;

import com.reandroid.common.FileChannelInputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;

@SuppressWarnings({"ResultOfMethodCallIgnored"})
public class IOUtil {

    public static String readUtf8(File file) throws IOException {
        return new String(readFully(file), StandardCharsets.UTF_8);
    }
    public static String readUtf8(InputStream inputStream) throws IOException {
        return new String(readFully(inputStream), StandardCharsets.UTF_8);
    }
    public static void writeUtf8(String content, File file) throws IOException {
        File tmp = file;
        if(file.isFile()) {
            tmp = FileUtil.toTmpName(file);
        }
        writeUtf8(content, FileUtil.outputStream(tmp));
        if(!tmp.equals(file)) {
            file.delete();
            tmp.renameTo(file);
        }
    }
    public static void writeUtf8(String content, OutputStream outputStream) throws IOException {
        byte[] bytes = content.getBytes();
        outputStream.write(bytes, 0, bytes.length);
        outputStream.close();
    }
    public static void writeAll(InputStream inputStream, File file) throws IOException {
        FileUtil.ensureParentDirectory(file);
        File tmp = file;
        if(tmp.isFile()) {
            tmp = FileUtil.toTmpName(tmp);
        }
        writeAll(inputStream, FileUtil.outputStream(tmp), true);
        if(!tmp.equals(file)) {
            file.delete();
            tmp.renameTo(file);
        }
    }
    public static void writeAll(InputStream inputStream, OutputStream outputStream) throws IOException {
        writeAll(inputStream, outputStream, true);
    }
    public static void writeAll(InputStream inputStream, OutputStream outputStream, boolean close) throws IOException {
        int bufferStep = 1024 * 1000;
        int bufferLength = bufferStep;
        int maxBuffer = bufferLength * 10;

        byte[] buffer = new byte[bufferLength];
        int read;
        while ((read = inputStream.read(buffer, 0, buffer.length)) >= 0){
            outputStream.write(buffer, 0, read);
            bufferLength = buffer.length;
            if(read == bufferLength && bufferLength < maxBuffer){
                bufferLength = bufferLength + bufferStep;
                buffer = new byte[bufferLength];
            }
        }
        if(close) {
            inputStream.close();
            outputStream.close();
        }
    }
    public static byte[] readFully(File file) throws IOException {
        return FileChannelInputStream.read(file, (int) file.length());
    }
    public static byte[] readFully(InputStream inputStream) throws IOException{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writeAll(inputStream, outputStream, true);
        return outputStream.toByteArray();
    }
    @Deprecated
    public static String shortPath(File file, int depth){
        return FileUtil.shortPath(file, depth);
    }
    public static void close(Object obj) throws IOException {
        if(obj instanceof Closeable){
            ((Closeable)obj).close();
        }
    }
}
