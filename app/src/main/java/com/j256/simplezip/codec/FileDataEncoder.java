package com.j256.simplezip.codec;

import java.io.Closeable;
import java.io.IOException;

/**
 * For encoding the zip file data.
 * 
 * @author graywatson
 */
public interface FileDataEncoder extends Closeable {

	/**
	 * Encode a buffer bytes from a Zip-file.
	 * 
	 * @param inputBuffer
	 *            Bytes to be encoded.
	 * @param offset
	 *            Offset in the buffer of the bytes to be encoded.
	 * @param length
	 *            Number of bytes to be encoded.
	 */
	public void encode(byte[] inputBuffer, int offset, int length) throws IOException;
}
