package com.j256.simplezip.format;

import java.io.IOException;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * Optional data descriptor immediately after the file-data. Only written if {@link GeneralPurposeFlag#DATA_DESCRIPTOR}
 * is set in the {@link ZipFileHeader}. This can be encoded as 32-bit or 64-bit sizes depending on whether or not the
 * sizes overflow the 4-byte integer.
 * 
 * @author graywatson
 */
public class ZipDataDescriptor {

	/** optional signature at the start of the data-descriptor */
	public static final int OPTIONAL_EXPECTED_SIGNATURE = 0x8074b50;

	private final boolean zip64;
	private final long crc32;
	private final long compressedSize;
	private final long uncompressedSize;

	public ZipDataDescriptor(boolean zip64, long crc32, long compressedSize, long uncompressedSize) {
		this.zip64 = zip64;
		this.crc32 = crc32;
		this.compressedSize = compressedSize;
		this.uncompressedSize = uncompressedSize;
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Read from the input-stream.
	 * 
	 * @param compressedSize
	 *            Size of the compressed data which is uses to see if we need to read in a zip64 or zip32 descriptor.
	 * @param uncompressedSize
	 *            Size of the uncompressed data which is uses to see if we need to read in a zip64 or zip32 descriptor.
	 */
	public static ZipDataDescriptor read(RewindableInputStream inputStream, long compressedSize, long uncompressedSize)
			throws IOException {
		Builder builder = new ZipDataDescriptor.Builder();
		/*
		 * This is a little strange since there is an optional magic value according to Wikipedia. If the first value
		 * doesn't match the expected then we assume it is the CRC. If it does match the expected value then check the
		 * expected CRC to see if (by some coincidence) it matches the expected signature. If it does then we read the
		 * next 4 bytes to see if that is also the same CRC value if not then we sort of throw up our hands and assume
		 * that the first 4 bytes is the CRC without a signature and pray.
		 */
		int first = IoUtils.readInt(inputStream, "ZipDataDescriptor.signature-or-crc32");
		if (first == OPTIONAL_EXPECTED_SIGNATURE) {
			builder.crc32 = IoUtils.readIntAsLong(inputStream, "ZipDataDescriptor.crc32");
		} else {
			// guess that we have crc, compressed-size, uncompressed-size with the crc matching the signature
			builder.crc32 = first;
		}

		if (compressedSize >= IoUtils.MAX_UNSIGNED_INT_VALUE || uncompressedSize >= IoUtils.MAX_UNSIGNED_INT_VALUE) {
			builder.compressedSize = IoUtils.readLong(inputStream, "ZipDataDescriptor.compressedSize");
			builder.uncompressedSize = IoUtils.readLong(inputStream, "ZipDataDescriptor.uncompressedSize");
		} else {
			builder.compressedSize = IoUtils.readInt(inputStream, "ZipDataDescriptor.compressedSize");
			builder.uncompressedSize = IoUtils.readInt(inputStream, "ZipDataDescriptor.uncompressedSize");
		}

		return builder.build();
	}

	/**
	 * Write to the output-stream.
	 */
	public void write(OutputStream outputStream) throws IOException {
		IoUtils.writeInt(outputStream, OPTIONAL_EXPECTED_SIGNATURE);
		IoUtils.writeInt(outputStream, crc32);
		if (zip64) {
			IoUtils.writeLong(outputStream, compressedSize);
			IoUtils.writeLong(outputStream, uncompressedSize);
		} else {
			IoUtils.writeInt(outputStream, compressedSize);
			IoUtils.writeInt(outputStream, uncompressedSize);
		}
	}

	public boolean isZip64() {
		return zip64;
	}

	public long getCrc32() {
		return crc32;
	}

	public long getCompressedSize() {
		return compressedSize;
	}

	public long getUncompressedSize() {
		return uncompressedSize;
	}

	/**
	 * Builder for {@link ZipDataDescriptor}.
	 */
	public static class Builder {
		private long crc32;
		private long compressedSize;
		private long uncompressedSize;

		/**
		 * Create a builder from an existing entry.
		 */
		public static Builder fromDescriptor(ZipDataDescriptor dataDescriptor) {
			Builder builder = new Builder();
			builder.crc32 = dataDescriptor.crc32;
			builder.compressedSize = dataDescriptor.compressedSize;
			builder.uncompressedSize = dataDescriptor.uncompressedSize;
			return builder;
		}

		/**
		 * Reset the builder in case you want to reuse.
		 */
		public void reset() {
			crc32 = 0;
			compressedSize = 0;
			uncompressedSize = 0;
		}

		/**
		 * Build an instance of the data-descriptor. 
		 */
		public ZipDataDescriptor build() {
			boolean zip64 = (compressedSize >= IoUtils.MAX_UNSIGNED_INT_VALUE
					|| uncompressedSize >= IoUtils.MAX_UNSIGNED_INT_VALUE);
			return new ZipDataDescriptor(zip64, crc32, compressedSize, uncompressedSize);
		}

		public long getCrc32() {
			return crc32;
		}

		public void setCrc32(long crc32) {
			this.crc32 = crc32;
		}

		public long getCompressedSize() {
			return compressedSize;
		}

		public void setCompressedSize(long compressedSize) {
			this.compressedSize = compressedSize;
		}

		public long getUncompressedSize() {
			return uncompressedSize;
		}

		public void setUncompressedSize(long uncompressedSize) {
			this.uncompressedSize = uncompressedSize;
		}
	}
}
