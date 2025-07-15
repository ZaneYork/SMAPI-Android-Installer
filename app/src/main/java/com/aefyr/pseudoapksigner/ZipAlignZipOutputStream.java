package com.aefyr.pseudoapksigner;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipAlignZipOutputStream extends ZipOutputStream {

    private BytesCounterOutputStream mBytesCounter;
    private int mAlignment;

    public static ZipAlignZipOutputStream create(OutputStream outputStream, int alignment) {
        BytesCounterOutputStream bytesCounterOutputStream = new BytesCounterOutputStream(outputStream);
        ZipAlignZipOutputStream zipAlignZipOutputStream = new ZipAlignZipOutputStream(bytesCounterOutputStream, alignment);
        zipAlignZipOutputStream.mBytesCounter = bytesCounterOutputStream;
        return zipAlignZipOutputStream;
    }

    private ZipAlignZipOutputStream(BytesCounterOutputStream outputStream, int alignment) {
        super(outputStream);
        mAlignment = alignment;
    }

    public void setAlignment(int alignment) {
        mAlignment = alignment;
    }

    public int getAlignment() {
        return mAlignment;
    }

    @Override
    public void putNextEntry(ZipEntry zipEntry) throws IOException {
        if (zipEntry.getMethod() == ZipEntry.STORED) {
            int headerSize = 30;
            headerSize += zipEntry.getName().getBytes().length;

            int requiredPadding = (int) (mAlignment - ((mBytesCounter.getBytesWritten() + headerSize) % mAlignment));
            zipEntry.setExtra(new byte[requiredPadding]);
        }

        super.putNextEntry(zipEntry);
    }

    private static class BytesCounterOutputStream extends OutputStream {

        private OutputStream mWrappedOutputStream;
        private long mBytesWritten = 0;

        private BytesCounterOutputStream(OutputStream outputStream) {
            mWrappedOutputStream = outputStream;
        }

        @Override
        public void write(byte[] b) throws IOException {
            mWrappedOutputStream.write(b);
            mBytesWritten += b.length;
        }

        @Override
        public void write(int b) throws IOException {
            mWrappedOutputStream.write(b);
            mBytesWritten++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            mWrappedOutputStream.write(b, off, len);
            mBytesWritten += len;
        }

        private long getBytesWritten() {
            return mBytesWritten;
        }
    }
}
