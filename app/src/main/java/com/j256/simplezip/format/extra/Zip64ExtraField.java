package com.j256.simplezip.format.extra;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;

/**
 * Zip64 extra-field information.
 * 
 * @author graywatson
 */
public class Zip64ExtraField extends BaseExtraField {

	public static final int EXPECTED_ID = 0x0001;
	public static final int EXTRA_SIZE = 8 + 8 + 8 + 4;

	private final long uncompressedSize;
	private final long compressedSize;
	private final long offset;
	private final int diskNumber;

	public Zip64ExtraField(long uncompressedSize, long compressedSize, long offset, int diskNumber) {
		super(EXPECTED_ID, EXTRA_SIZE);
		this.uncompressedSize = uncompressedSize;
		this.compressedSize = compressedSize;
		this.offset = offset;
		this.diskNumber = diskNumber;
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Read in the rest of the Zip64ExtraField after the id is read.
	 */
	public static Zip64ExtraField read(InputStream inputStream, int id, int size) throws IOException {
		Builder builder = new Zip64ExtraField.Builder();
		builder.uncompressedSize = IoUtils.readLong(inputStream, "Zip64ExtraField.uncompressedSize");
		builder.compressedSize = IoUtils.readLong(inputStream, "Zip64ExtraField.compressedSize");
		builder.offset = IoUtils.readLong(inputStream, "Zip64ExtraField.offset");
		builder.diskNumber = IoUtils.readInt(inputStream, "Zip64ExtraField.diskNumber");
		return builder.build();
	}

	/**
	 * Write extra-field to the output-stream.
	 */
	@Override
	public void write(OutputStream outputStream) throws IOException {
		super.write(outputStream);
		IoUtils.writeLong(outputStream, uncompressedSize);
		IoUtils.writeLong(outputStream, compressedSize);
		IoUtils.writeLong(outputStream, offset);
		IoUtils.writeInt(outputStream, diskNumber);
	}

	public long getUncompressedSize() {
		return uncompressedSize;
	}

	public long getCompressedSize() {
		return compressedSize;
	}

	public long getOffset() {
		return offset;
	}

	public int getDiskNumber() {
		return diskNumber;
	}

	/**
	 * Builder for {@link Zip64ExtraField}.
	 */
	public static class Builder {
		private long uncompressedSize;
		private long compressedSize;
		private long offset;
		private int diskNumber;

		/**
		 * Build and return the extra field. 
		 */
		public Zip64ExtraField build() {
			return new Zip64ExtraField(uncompressedSize, compressedSize, offset, diskNumber);
		}

		public long getUncompressedSize() {
			return uncompressedSize;
		}

		public void setUncompressedSize(long uncompressedSize) {
			this.uncompressedSize = uncompressedSize;
		}

		public Builder withUncompressedSize(long uncompressedSize) {
			this.uncompressedSize = uncompressedSize;
			return this;
		}

		public long getCompressedSize() {
			return compressedSize;
		}

		public void setCompressedSize(long compressedSize) {
			this.compressedSize = compressedSize;
		}

		public Builder withCompressedSize(long compressedSize) {
			this.compressedSize = compressedSize;
			return this;
		}

		public long getOffset() {
			return offset;
		}

		public void setOffset(long offset) {
			this.offset = offset;
		}

		public Builder withOffset(long offset) {
			this.offset = offset;
			return this;
		}

		public int getDiskNumber() {
			return diskNumber;
		}

		public void setDiskNumber(int diskNumber) {
			this.diskNumber = diskNumber;
		}

		public Builder withDiskNumber(int diskNumber) {
			this.diskNumber = diskNumber;
			return this;
		}
	}
}
