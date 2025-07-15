package com.j256.simplezip.codec;

import java.io.Closeable;
import java.io.IOException;

/**
 * For decoding the zip file data.
 * 
 * @author graywatson
 */
public interface FileDataDecoder extends Closeable {

	/**
	 * Decode a buffer bytes from a Zip-file.
	 * 
	 * @param outputBuffer
	 *            Buffer to write decoded bytes into.
	 * @param offset
	 *            offset in the buffer to write the decoded bytes
	 * @param length
	 *            number of bytes that can be read into the buffer
	 * @return The number of bytes written into the output-buffer or -1 on EOF.
	 */
	public int decode(byte[] outputBuffer, int offset, int length) throws IOException;

	/**
	 * Return the number of encoded bytes read in.
	 */
	public long getBytesRead();

	/**
	 * Return the number of decoded bytes written out.
	 */
	public long getBytesWritten();
}
