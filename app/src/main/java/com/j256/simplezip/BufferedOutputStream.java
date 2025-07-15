package com.j256.simplezip;

import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.ZipDataDescriptor;
import com.j256.simplezip.format.ZipFileHeader;
import com.j256.simplezip.format.ZipFileHeader.Builder;
import com.starry.FileUtils;

/**
 * Class which helps the writer by absorbing the encoded (compressed) stream information so that we can annotate the
 * {@link ZipFileHeader} with the encoded size and checksums and not have to use the {@link ZipDataDescriptor} which is
 * written after the data.
 * 
 * @author graywatson
 */
public class BufferedOutputStream extends OutputStream {

	private final CountingOutputStream delegate;
	private final byte[] singleByteBuffer = new byte[1];
	private final byte[] tmpBuffer = new byte[IoUtils.STANDARD_BUFFER_SIZE];

	private long maxSizeBuffered;
	private int maxSizeInMemory;
	private byte[] memoryBuffer = new byte[0];
	private int memoryOffset;
	private long encodedSize;
	private boolean buffered;
	private File tmpFile;
	private FileOutputStream tmpFileOutputStream;
	private ZipFileHeader fileHeader;

	public BufferedOutputStream(OutputStream outputStream) {
		this.delegate = new CountingOutputStream(outputStream);
	}

	/**
	 * Set our buffer limits.
	 */
	public void enableBuffer(long maxSizeBuffered, int maxSizeInMemory) {
		this.maxSizeBuffered = maxSizeBuffered;
		this.maxSizeInMemory = maxSizeInMemory;
	}

	@Override
	public void write(int b) throws IOException {
		singleByteBuffer[0] = (byte) b;
		write(singleByteBuffer, 0, 1);
	}

	@Override
	public void write(byte[] buffer, int offset, int length) throws IOException {
		if (fileHeader == null) {
			// we have already given up and written the header
			delegate.write(buffer, offset, length);
			encodedSize += length;
			return;
		}
		ensureMemoryBufferMaxSpace(length);
		if (memoryOffset + length <= memoryBuffer.length) {
			// store this in the memory buffer
			System.arraycopy(buffer, offset, memoryBuffer, memoryOffset, length);
			memoryOffset += length;
			encodedSize += length;
			return;
		}
		if (memoryOffset < memoryBuffer.length) {
			// room for _some_ to go into the memory buffer
			int memLen = memoryBuffer.length - memoryOffset;
			System.arraycopy(buffer, offset, memoryBuffer, memoryOffset, memLen);
			offset += memLen;
			length -= memLen;
			memoryOffset += length;
			encodedSize += length;
		}
		if (encodedSize + length > maxSizeBuffered) {
			// need to give up and write out to the delegate
			giveUp(buffer, offset, length);
			return;
		}
		// write the rest to disk
		if (tmpFile == null) {
			tmpFile = File.createTempFile(getClass().getSimpleName(), ".ztf");
			tmpFile.deleteOnExit();
			// NOTE: no buffered output stream here because we are dealing with buffers externally
			tmpFileOutputStream = (FileOutputStream) FileUtils.getOutputStream(tmpFile);
		}
		tmpFileOutputStream.write(buffer, offset, length);
		encodedSize += length;
	}

	/**
	 * We are done with the writing of the file data so dump everything if the buffer hasn't given up.
	 * 
	 * @return Returned the file-header that was written to the stream if it is different from the one set.
	 */
	public ZipFileHeader finishFileData(long crc32, long uncompressedSize) throws IOException {
		if (!buffered) {
			// we would have written everything already
			return null;
		}

		// need to build a new file-header from the newly calculated information
		Builder fullHeaderBuilder = ZipFileHeader.Builder.fromHeader(fileHeader);
		/*
		 * We need to clear the data-descriptor flag here because we are writing enough information into the header so
		 * we don't need one.
		 */
		fullHeaderBuilder.clearGeneralPurposeFlag(GeneralPurposeFlag.DATA_DESCRIPTOR);
		fullHeaderBuilder.setCrc32(crc32);
		fullHeaderBuilder.setCompressedSize(encodedSize);
		fullHeaderBuilder.setUncompressedSize(uncompressedSize);
		ZipFileHeader writtenFileHeader = fullHeaderBuilder.build();
		writtenFileHeader.write(delegate);

		// first write the memory buffer to the delegate
		delegate.write(memoryBuffer, 0, memoryBuffer.length);
		writeAnyTmpFileToDelegate();

		fileHeader = null;
		return writtenFileHeader;
	}

	/**
	 * Number of byte written to the output-stream.
	 */
	public long getWriteCount() {
		return delegate.getWriteCount();
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
		if (tmpFileOutputStream != null) {
			// might as well
			tmpFileOutputStream.flush();
		}
	}

	@Override
	public void close() throws IOException {
		delegate.close();
		// tmpFileOutputStream should have been closed already
		if (tmpFile != null) {
			tmpFile.delete();
			tmpFile = null;
		}
	}

	/**
	 * Exposed for testing purposes.
	 */
	public File getTmpFile() {
		return tmpFile;
	}

	/**
	 * Encoded bytes either buffered or written of the latest file written by {@link #finishFileData(long, long)}..
	 */
	public long getEncodedSize() {
		return encodedSize;
	}

	/**
	 * This saves the file-header and effectively turns on the memory buffer.
	 */
	public void setFileHeader(ZipFileHeader fileHeader) {
		this.fileHeader = fileHeader;
		this.memoryOffset = 0;
		this.encodedSize = 0;
		this.tmpFile = null;
		this.tmpFileOutputStream = null;
		this.buffered = true;
	}

	private void giveUp(byte[] buffer, int offset, int length) throws IOException {
		fileHeader.write(delegate);
		long start = delegate.getWriteCount();
		delegate.write(memoryBuffer, 0, memoryBuffer.length);
		writeAnyTmpFileToDelegate();
		// write the rest of the current buffer to the delegate
		delegate.write(buffer, offset, length);
		// this is now our encoded size
		encodedSize = delegate.getWriteCount() - start;
		buffered = false;
		fileHeader = null;
	}

	/**
	 * Write the temp file if any to disk.
	 */
	private void writeAnyTmpFileToDelegate() throws IOException {
		if (tmpFileOutputStream == null) {
			return;
		}
		tmpFileOutputStream.close();
		try (FileInputStream fileInput = new FileInputStream(tmpFile)) {
			while (true) {
				int numRead = fileInput.read(tmpBuffer);
				if (numRead < 0) {
					break;
				}
				delegate.write(tmpBuffer, 0, numRead);
			}
		}
		tmpFile.delete();
		tmpFile = null;
	}

	private void ensureMemoryBufferMaxSpace(int length) {
		int needed = memoryOffset + length;
		if (needed <= memoryBuffer.length || memoryBuffer.length == maxSizeInMemory) {
			// nothing to do
			return;
		}
		// maybe extend the buffer if possible
		length = Math.min(needed, maxSizeInMemory);
		if (length > memoryBuffer.length) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				memoryBuffer = Arrays.copyOf(memoryBuffer, length);
			} else {
				byte[] copy = new byte[length];
				System.arraycopy(memoryBuffer, 0, copy, 0,
						Math.min(memoryBuffer.length, length));
				memoryBuffer = copy;
			}
		}
	}

	/**
	 * Output stream which counts the bytes written to it.
	 */
	private static class CountingOutputStream extends OutputStream {

		private final OutputStream delegate;
		private long writeCount;

		public CountingOutputStream(OutputStream delegate) {
			this.delegate = delegate;
		}

		@Override
		public void write(int b) throws IOException {
			// may not get here
			delegate.write(b);
			writeCount++;
		}

		@Override
		public void write(byte[] buffer, int offset, int length) throws IOException {
			delegate.write(buffer, offset, length);
			writeCount += length;
		}

		/**
		 * Get the total counts.
		 */
		public long getWriteCount() {
			return writeCount;
		}

		@Override
		public void flush() throws IOException {
			delegate.flush();
		}

		@Override
		public void close() throws IOException {
			delegate.close();
		}
	}
}
