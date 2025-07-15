package com.j256.simplezip;

import java.util.zip.CRC32;

/**
 * Storage for the count and {@link CRC32} of a stream bytes.
 * 
 * @author graywatson
 */
public class ZipFileDataInfo {

	private long byteCount;
	private final CRC32 crc32 = new CRC32();

	/**
	 * Update the count with a buffer of bytes.
	 */
	public void update(byte[] buffer, int offset, int length) {
		byteCount += length;
		crc32.update(buffer, offset, length);
	}

	/**
	 * Reset the count so we can count something else.
	 */
	public void reset() {
		byteCount = 0;
		crc32.reset();
	}

	/**
	 * Return how many bytes were counted.
	 */
	public long getByteCount() {
		return byteCount;
	}

	/**
	 * Return the crc of the bytes.
	 */
	public long getCrc32() {
		return crc32.getValue();
	}
}
