package com.j256.simplezip.codec;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Encoder for the STORED (really raw) Zip file format.
 * 
 * @author graywatson
 */
public class StoredFileDataEncoder implements FileDataEncoder {

	private final OutputStream outputStream;

	public StoredFileDataEncoder(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void encode(byte[] inputBuffer, int offset, int length) throws IOException {
		outputStream.write(inputBuffer, offset, length);
	}

	@Override
	public void close() {
		// no-op
	}
}
