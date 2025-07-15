package com.j256.simplezip.format;

import java.io.IOException;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * Locator for the Zip64 format {@link ZipCentralDirectoryEnd} information since it has an extensible block of bytes of
 * unknown length.
 * 
 * @author graywatson
 */
public class Zip64CentralDirectoryEndLocator {

	/** signature that is expected to be at the start of the central directory */
	private static final int EXPECTED_SIGNATURE = 0x7064b50;

	private final int diskNumber;
	private final int diskNumberStart;
	private final long endOffset;
	private final int numberDisks;

	public Zip64CentralDirectoryEndLocator(int diskNumber, int diskNumberStart, long endOffset, int numberDisks) {
		this.diskNumber = diskNumber;
		this.diskNumberStart = diskNumberStart;
		this.endOffset = endOffset;
		this.numberDisks = numberDisks;
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Read one from the input-stream.
	 */
	public static Zip64CentralDirectoryEndLocator read(RewindableInputStream inputStream) throws IOException {
		int signature = IoUtils.readInt(inputStream, "Zip64CentralDirectoryEndLocator.signature");
		if (signature != EXPECTED_SIGNATURE) {
			inputStream.rewind(4);
			return null;
		}

		Builder builder = new Zip64CentralDirectoryEndLocator.Builder();
		builder.diskNumber = IoUtils.readInt(inputStream, "Zip64CentralDirectoryEndLocator.diskNumber");
		builder.diskNumberStart = IoUtils.readInt(inputStream, "Zip64CentralDirectoryEndLocator.diskNumberStart");
		builder.endOffset = IoUtils.readLong(inputStream, "Zip64CentralDirectoryEndLocator.endOffset");
		builder.numberDisks = IoUtils.readInt(inputStream, "Zip64CentralDirectoryEndLocator.numberDisks");
		return builder.build();
	}

	/**
	 * Write to the output-stream.
	 */
	public void write(OutputStream outputStream) throws IOException {
		IoUtils.writeInt(outputStream, EXPECTED_SIGNATURE);
		IoUtils.writeInt(outputStream, diskNumber);
		IoUtils.writeInt(outputStream, diskNumberStart);
		IoUtils.writeLong(outputStream, endOffset);
		IoUtils.writeInt(outputStream, numberDisks);
	}

	public int getDiskNumber() {
		return diskNumber;
	}

	public int getDiskNumberStart() {
		return diskNumberStart;
	}

	public long getEndOffset() {
		return endOffset;
	}

	public int getNumberDisks() {
		return numberDisks;
	}

	/**
	 * Builder for the {@link Zip64CentralDirectoryEndLocator}.
	 */
	public static class Builder {
		private int diskNumber = ZipCentralDirectoryFileEntry.DEFAULT_DISK_NUMBER;
		private int diskNumberStart = ZipCentralDirectoryFileEntry.DEFAULT_DISK_NUMBER;
		private long endOffset;
		private int numberDisks = 1;

		/**
		 * Build an instance of our directory end.
		 */
		public Zip64CentralDirectoryEndLocator build() {
			return new Zip64CentralDirectoryEndLocator(diskNumber, diskNumberStart, endOffset, numberDisks);
		}

		/**
		 * Create an end from the directory-end-info.
		 */
		public static Builder fromEndInfo(ZipCentralDirectoryEndInfo endInfo) {
			Builder builder = new Builder();
			builder.diskNumber = endInfo.getDiskNumber();
			builder.diskNumberStart = endInfo.getDiskNumberStart();
			builder.numberDisks = endInfo.getNumberDisks();
			return builder;
		}

		public int getDiskNumber() {
			return diskNumber;
		}

		public void setDiskNumber(int diskNumber) {
			this.diskNumber = diskNumber;
		}

		public int getDiskNumberStart() {
			return diskNumberStart;
		}

		public void setDiskNumberStart(int diskNumberStart) {
			this.diskNumberStart = diskNumberStart;
		}

		public long getEndOffset() {
			return endOffset;
		}

		public void setEndOffset(long endOffset) {
			this.endOffset = endOffset;
		}

		public int getNumberDisks() {
			return numberDisks;
		}

		public void setNumberDisks(int numberDisks) {
			this.numberDisks = numberDisks;
		}
	}
}
