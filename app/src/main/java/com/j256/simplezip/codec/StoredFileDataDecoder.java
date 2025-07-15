package com.j256.simplezip.codec;

import java.io.IOException;
import java.io.InputStream;

/**
 * Decoder for the STORED (really raw) Zip file format.
 * 
 * @author graywatson
 */
public class StoredFileDataDecoder implements FileDataDecoder {

	private final long dataSize;
	private final InputStream inputStream;

	private long inputOffset;
	private long bytesRead;
	private long bytesWritten;

	public StoredFileDataDecoder(InputStream inputStream, long dataSize) {
		this.dataSize = dataSize;
		this.inputStream = inputStream;
	}

	@Override
	public int decode(byte[] outputBuffer, int offset, int length) throws IOException {
		int maxLength;
		// we do this instead of Math.min() because one is long but never > than MAXINT
		if ((dataSize - inputOffset) < length) {
			maxLength = (int) (dataSize - inputOffset);
		} else {
			maxLength = length;
		}
		if (maxLength <= 0) {
			// hit the end of the data
			return -1;
		}

		int numRead = inputStream.read(outputBuffer, offset, maxLength);
		if (numRead < 0) {
			// may not be able to get here but let's be careful out there
			return -1;
		}
		inputOffset += numRead;
		bytesRead += numRead;
		bytesWritten += numRead;
		return numRead;
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
}
