package com.j256.simplezip.codec;

import java.io.IOException;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;

/**
 * Encoder for testing purposes only that allows us to write huge files to test Zip64 encoding without having to pay for
 * the deflate performance.
 * 
 * @author graywatson
 */
public class SimpleZipFileDataEncoder implements FileDataEncoder {

	public static final int EOF_MARKER = 0;
	private final OutputStream outputStream;
	private long bytesWritten;

	public SimpleZipFileDataEncoder(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void encode(byte[] inputBuffer, int offset, int length) throws IOException {
		if (length > 0) {
			IoUtils.writeInt(outputStream, length);
			bytesWritten += 4;
			outputStream.write(inputBuffer, offset, length);
			bytesWritten += length;
		}
	}

	@Override
	public void close() throws IOException {
		// write our EOF marker
		IoUtils.writeInt(outputStream, EOF_MARKER);
		bytesWritten += 4;
	}

	public long getBytesWritten() {
		return bytesWritten;
	}
}
