package com.j256.simplezip.codec;

import java.io.IOException;
import java.io.InputStream;

import com.j256.simplezip.IoUtils;

/**
 * Decoder for testing purposes only that allows us to write huge files to test Zip64 encoding without having to pay for
 * the inflate performance.
 * 
 * @author graywatson
 */
public class SimpleZipFileDataDecoder implements FileDataDecoder {

	private final InputStream inputStream;
	private final String label = getClass().getSimpleName();

	private long bytesRead;
	private long bytesWritten;
	private byte[] readBuffer;
	private int readBufferOffset;
	private int readBufferLength;
	private boolean eof;

	public SimpleZipFileDataDecoder(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public int decode(byte[] outputBuffer, int offset, int length) throws IOException {
		if (eof) {
			return -1;
		}
		if (readBufferOffset >= readBufferLength) {
			if (fillBuffer() < 0) {
				return -1;
			}
		}

		int copyLength = Math.min(readBufferLength - readBufferOffset, length);
		System.arraycopy(readBuffer, readBufferOffset, outputBuffer, offset, copyLength);

		readBufferOffset += copyLength;
		bytesWritten += copyLength;
		return copyLength;
	}

	@Override
	public void close() {
		// no-op
	}

	@Override
	public long getBytesRead() {
		return bytesRead;
	}

	@Override
	public long getBytesWritten() {
		return bytesWritten;
	}

	/**
	 * Fill the buffer with decoded bytes.
	 * 
	 * @return >0 if ok or -1 if EOF.
	 */
	private int fillBuffer() throws IOException {
		int bufLength = IoUtils.readInt(inputStream, label);
		bytesRead += 4;
		if (bufLength == SimpleZipFileDataEncoder.EOF_MARKER) {
			eof = true;
			return -1;
		}
		if (bufLength < 0) {
			throw new IOException("invalid buffer length of " + bufLength + ", corrupted data");
		}
		if (readBuffer == null || readBuffer.length < bufLength) {
			readBuffer = new byte[bufLength];
		}
		IoUtils.readFully(inputStream, readBuffer, bufLength, label);
		readBufferOffset = 0;
		readBufferLength = bufLength;
		bytesRead += bufLength;
		return bufLength;
	}
}
