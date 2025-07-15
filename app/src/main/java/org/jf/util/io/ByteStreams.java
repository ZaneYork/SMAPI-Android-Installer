package org.jf.util.io;

import java.io.*;


public class ByteStreams {

    public static long copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[4096];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }
    public static void skipFully(InputStream in, long n) throws IOException {
        long skipped = skipUpTo(in, n);
        if (skipped < n) {
            throw new EOFException(
                    "reached end of stream after skipping " + skipped + " bytes; " + n + " bytes expected");
        }
    }
    static long skipUpTo(InputStream in, final long n) throws IOException {
        long totalSkipped = 0;
        byte[] buf = DUMMY_BUFFER;

        while (totalSkipped < n) {
            long remaining = n - totalSkipped;
            long skipped;

            int skip = (int) Math.min(remaining, buf.length);
            if ((skipped = in.read(buf, 0, skip)) == -1) {
                // Reached EOF
                break;
            }

            totalSkipped += skipped;
        }

        return totalSkipped;
    }
    public static void readFully(InputStream in, byte[] b) throws IOException {
        readFully(in, b, 0, b.length);
    }
    public static void readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        int read = in.read(b, off, len);
        if (read != len) {
            throw new EOFException(
                    "reached end of stream after reading " + read + " bytes; " + len + " bytes expected");
        }
    }
    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[40960];
        int length;
        while((length = in.read(buffer))>0){
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        in.close();
        return outputStream.toByteArray();
    }
    public static byte[] toByteArray(File file) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) file.length());
        InputStream inputStream = new FileInputStream(file);
        byte[] buffer = new byte[8192];
        int length;
        while((length = inputStream.read(buffer))>0){
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        inputStream.close();
        return outputStream.toByteArray();
    }

    private static final byte[] DUMMY_BUFFER = new byte[4096];
}
