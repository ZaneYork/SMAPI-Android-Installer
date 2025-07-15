package com.j256.simplezip;

import java.io.IOException;
import java.io.InputStream;

/**
 * Buffer that keeps around the last read bytes, allowing us to rewind the stream for a certain number of bytes. This is
 * necessary because there are a couple of places where the stream is read ahead because of buffering and we need to
 * rewind when done.
 * 
 * @author graywatson
 */
public class RewindableInputStream extends InputStream {

	private final InputStream delegate;
	private byte[] buffer;
	private int offset;
	private int extraOffset;
	private long byteCount;

	public RewindableInputStream(InputStream delegate, int initialBufferSize) {
		this.delegate = delegate;
		this.buffer = new byte[initialBufferSize];
	}

	@Override
	public int read() throws IOException {
		if (extraOffset < offset) {
			int ret = (int) (buffer[extraOffset++] & 0xff);
			byteCount++;
			return ret;
		}
		ensureSpace(1);
		int ret = delegate.read(buffer, offset, 1);
		if (ret < 0) {
			return -1;
		}
		ret = (int) (buffer[offset++] & 0xff);
		extraOffset++;
		byteCount++;
		return ret;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}

	@Override
	public int read(byte[] outBuffer, int outOffset, int length) throws IOException {
		if (length == 0) {
			return 0;
		}
		int extraRead = 0;
		for (; extraOffset < offset && length > 0; length--) {
			outBuffer[outOffset++] = buffer[extraOffset++];
			extraRead++;
			byteCount++;
		}
		if (length == 0) {
			return extraRead;
		}
		ensureSpace(length);
		int numRead = delegate.read(buffer, offset, length);
		if (numRead < 0) {
			if (extraRead == 0) {
				return -1;
			} else {
				return extraRead;
			}
		}
		System.arraycopy(buffer, offset, outBuffer, outOffset, numRead);
		offset += numRead;
		extraOffset += numRead;
		byteCount += numRead;
		return extraRead + numRead;
	}

	/**
	 * Rewind the buffer a certain number of bytes.
	 */
	public void rewind(int numBytes) throws IOException {
		if (numBytes > extraOffset) {
			throw new IOException("Trying to rewind " + numBytes + " but buffer only has " + extraOffset);
		}
		extraOffset -= numBytes;
		byteCount -= numBytes;
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	/**
	 * Return the number of bytes read using this stream.
	 */
	public long getByteCount() {
		return byteCount;
	}

	/**
	 * Ensure we have these many bytes left in the buffer.
	 * 
	 * NOTE: we wouldn't be here if there were extra bytes so we don't have to copy anything around
	 */
	private void ensureSpace(int numBytes) {
		if (offset + numBytes <= buffer.length) {
			return;
		}
		// only grow the buffer if the read is more than the buffer size
		if (numBytes > buffer.length) {
			int newLength = Math.max(buffer.length * 2, numBytes * 2);
			buffer = new byte[newLength];
		}
		offset = 0;
		extraOffset = 0;
	}
}
