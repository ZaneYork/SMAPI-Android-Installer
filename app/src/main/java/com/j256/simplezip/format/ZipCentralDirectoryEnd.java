package com.j256.simplezip.format;

import java.io.IOException;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;

/**
 * Standard end segment of the central-directory which is at the very end of the Zip file.
 * 
 * @author graywatson
 */
public class ZipCentralDirectoryEnd {

	/** signature that is expected to be at the start of the central directory */
	private static final int EXPECTED_SIGNATURE = 0x6054b50;
	/** This is the minimum size that this header will take on disk. */
	public static final int MINIMUM_READ_SIZE = 4 * 2 + 2 * 4 + 2;

	private final boolean needsZip64;
	private final int diskNumber;
	private final int diskNumberStart;
	private final int numRecordsOnDisk;
	private final int numRecordsTotal;
	private final long directorySize;
	private final long directoryOffset;
	private final byte[] commentBytes;

	public ZipCentralDirectoryEnd(boolean needsZip64, int diskNumber, int diskNumberStart, int numRecordsOnDisk,
			int numRecordsTotal, long directorySize, long directoryOffset, byte[] commentBytes) {
		this.needsZip64 = needsZip64;
		this.diskNumber = diskNumber;
		this.diskNumberStart = diskNumberStart;
		this.numRecordsOnDisk = numRecordsOnDisk;
		this.numRecordsTotal = numRecordsTotal;
		this.directorySize = directorySize;
		this.directoryOffset = directoryOffset;
		this.commentBytes = commentBytes;
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
	public static ZipCentralDirectoryEnd read(RewindableInputStream inputStream) throws IOException {

		int signature = IoUtils.readInt(inputStream, "ZipCentralDirectoryEnd.signature");
		if (signature != EXPECTED_SIGNATURE) {
			inputStream.rewind(4);
			return null;
		}

		Builder builder = new ZipCentralDirectoryEnd.Builder();
		builder.diskNumber = IoUtils.readShort(inputStream, "ZipCentralDirectoryEnd.diskNumber");
		builder.diskNumberStart = IoUtils.readShort(inputStream, "ZipCentralDirectoryEnd.diskNumberStart");
		builder.numRecordsOnDisk = IoUtils.readShort(inputStream, "ZipCentralDirectoryEnd.numRecordsOnDisk");
		builder.numRecordsTotal = IoUtils.readShort(inputStream, "ZipCentralDirectoryEnd.numRecordsTotal");
		builder.directorySize = IoUtils.readInt(inputStream, "ZipCentralDirectoryEnd.sizeDirectory");
		builder.directoryOffset = IoUtils.readInt(inputStream, "ZipCentralDirectoryEnd.directoryOffset");
		int commentLength = IoUtils.readShort(inputStream, "ZipCentralDirectoryEnd.commentLength");
		builder.commentBytes = IoUtils.readBytes(inputStream, commentLength, "ZipCentralDirectoryEnd.comment");

		return builder.build();
	}

	/**
	 * Write to the output-stream.
	 */
	public void write(OutputStream outputStream) throws IOException {
		IoUtils.writeInt(outputStream, EXPECTED_SIGNATURE);
		IoUtils.writeShort(outputStream, diskNumber);
		IoUtils.writeShort(outputStream, diskNumberStart);
		IoUtils.writeShort(outputStream, numRecordsOnDisk);
		IoUtils.writeShort(outputStream, numRecordsTotal);
		IoUtils.writeInt(outputStream, directorySize);
		IoUtils.writeInt(outputStream, directoryOffset);
		if (commentBytes == null) {
			IoUtils.writeShort(outputStream, 0);
			// no comment-bytes
		} else {
			IoUtils.writeShort(outputStream, commentBytes.length);
			IoUtils.writeBytes(outputStream, commentBytes);
		}
	}

	/**
	 * Return whether or not the values stored in this end record are set to 0xFFFF or 0xFFFFFFFF so need a Zip64 end
	 * written out.
	 */
	public boolean isNeedsZip64() {
		return needsZip64;
	}

	public int getDiskNumber() {
		return diskNumber;
	}

	public int getDiskNumberStart() {
		return diskNumberStart;
	}

	public int getNumRecordsOnDisk() {
		return numRecordsOnDisk;
	}

	public int getNumRecordsTotal() {
		return numRecordsTotal;
	}

	public long getDirectorySize() {
		return directorySize;
	}

	public long getDirectoryOffset() {
		return directoryOffset;
	}

	public byte[] getCommentBytes() {
		return commentBytes;
	}

	public String getComment() {
		if (commentBytes == null) {
			return null;
		} else {
			return new String(commentBytes);
		}
	}

	/**
	 * Builder for {@link ZipCentralDirectoryEnd}.
	 */
	public static class Builder {
		private int diskNumber = ZipCentralDirectoryFileEntry.DEFAULT_DISK_NUMBER;
		private int diskNumberStart = ZipCentralDirectoryFileEntry.DEFAULT_DISK_NUMBER;
		private int numRecordsOnDisk;
		private int numRecordsTotal;
		private long directorySize;
		private long directoryOffset;
		private byte[] commentBytes;

		/**
		 * Build an instance of our directory end.
		 */
		public ZipCentralDirectoryEnd build() {
			boolean needsZip64 = false;
			int correctedDiskNumber = diskNumber;
			if (correctedDiskNumber >= IoUtils.MAX_UNSIGNED_SHORT_VALUE) {
				correctedDiskNumber = IoUtils.MAX_UNSIGNED_SHORT_VALUE;
				needsZip64 = true;
			}
			int correctedDiskNumberStart = diskNumberStart;
			if (correctedDiskNumberStart >= IoUtils.MAX_UNSIGNED_SHORT_VALUE) {
				correctedDiskNumberStart = IoUtils.MAX_UNSIGNED_SHORT_VALUE;
				needsZip64 = true;
			}
			int correctedNumRecordsOnDisk = numRecordsOnDisk;
			if (correctedNumRecordsOnDisk >= IoUtils.MAX_UNSIGNED_SHORT_VALUE) {
				correctedNumRecordsOnDisk = IoUtils.MAX_UNSIGNED_SHORT_VALUE;
				needsZip64 = true;
			}
			int correctedNumRecordsTotal = numRecordsTotal;
			if (correctedNumRecordsTotal >= IoUtils.MAX_UNSIGNED_SHORT_VALUE) {
				correctedNumRecordsTotal = IoUtils.MAX_UNSIGNED_SHORT_VALUE;
				needsZip64 = true;
			}
			long correctedDirectorySize = directorySize;
			if (correctedDirectorySize >= IoUtils.MAX_UNSIGNED_INT_VALUE) {
				correctedDirectorySize = IoUtils.MAX_UNSIGNED_INT_VALUE;
				needsZip64 = true;
			}
			long correctedDirectoryOffset = directoryOffset;
			if (correctedDirectoryOffset >= IoUtils.MAX_UNSIGNED_INT_VALUE) {
				correctedDirectoryOffset = IoUtils.MAX_UNSIGNED_INT_VALUE;
				needsZip64 = true;
			}
			return new ZipCentralDirectoryEnd(needsZip64, correctedDiskNumber, correctedDiskNumberStart,
					correctedNumRecordsOnDisk, correctedNumRecordsTotal, correctedDirectorySize,
					correctedDirectoryOffset, commentBytes);
		}

		/**
		 * Create an end from the directory-end-info.
		 */
		public static Builder fromEndInfo(ZipCentralDirectoryEndInfo endInfo) {
			Builder builder = new Builder();
			builder.diskNumber = endInfo.getDiskNumber();
			builder.diskNumberStart = endInfo.getDiskNumberStart();
			builder.commentBytes = endInfo.getCommentBytes();
			return builder;
		}

		/**
		 * Return whether or not the values stored in this directory end require that we add a
		 * {@link Zip64CentralDirectoryEnd} and {@link Zip64CentralDirectoryEndLocator} before the end.
		 */
		public boolean hasZip64Values() {
			return (diskNumber >= IoUtils.MAX_UNSIGNED_SHORT_VALUE //
					|| diskNumberStart >= IoUtils.MAX_UNSIGNED_SHORT_VALUE //
					|| numRecordsOnDisk >= IoUtils.MAX_UNSIGNED_SHORT_VALUE //
					|| numRecordsTotal >= IoUtils.MAX_UNSIGNED_SHORT_VALUE //
					|| directorySize >= IoUtils.MAX_UNSIGNED_INT_VALUE //
					|| directoryOffset >= IoUtils.MAX_UNSIGNED_INT_VALUE);
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

		public int getNumRecordsOnDisk() {
			return numRecordsOnDisk;
		}

		public void setNumRecordsOnDisk(int numRecordsOnDisk) {
			this.numRecordsOnDisk = numRecordsOnDisk;
		}

		public int getNumRecordsTotal() {
			return numRecordsTotal;
		}

		public void setNumRecordsTotal(int numRecordsTotal) {
			this.numRecordsTotal = numRecordsTotal;
		}

		public long getDirectorySize() {
			return directorySize;
		}

		public void setDirectorySize(long directorySize) {
			this.directorySize = directorySize;
		}

		public long getDirectoryOffset() {
			return directoryOffset;
		}

		public void setDirectoryOffset(long directoryOffset) {
			this.directoryOffset = directoryOffset;
		}

		public byte[] getCommentBytes() {
			return commentBytes;
		}

		public void setCommentBytes(byte[] commentBytes) {
			this.commentBytes = commentBytes;
		}
	}
}
