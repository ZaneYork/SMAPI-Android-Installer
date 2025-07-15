package com.j256.simplezip.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.RewindableInputStream;
import com.j256.simplezip.ZipFileOutput;
import com.j256.simplezip.format.extra.BaseExtraField;
import com.j256.simplezip.format.extra.ExtraFieldUtil;
import com.j256.simplezip.format.extra.Zip64ExtraField;

/**
 * Header of Zip file entries.
 * 
 * @author graywatson
 */
public class ZipFileHeader {

	private static int EXPECTED_SIGNATURE = 0x4034b50;

	private final int versionNeeded;
	private final int generalPurposeFlags;
	private final int compressionMethod;
	private final int lastModifiedTime;
	private final int lastModifiedDate;
	private final long crc32;
	private final long compressedSize;
	private final long uncompressedSize;
	private final byte[] fileNameBytes;
	private final byte[] extraFieldBytes;
	private final Zip64ExtraField zip64ExtraField;

	public ZipFileHeader(int versionNeeded, int generalPurposeFlags, int compressionMethod, int lastModifiedTime,
			int lastModifiedDate, long crc32, long compressedSize, long uncompressedSize, byte[] fileName,
			byte[] extraFieldBytes, Zip64ExtraField zip64ExtraField) {
		this.versionNeeded = versionNeeded;
		this.generalPurposeFlags = generalPurposeFlags;
		this.compressionMethod = compressionMethod;
		this.lastModifiedTime = lastModifiedTime;
		this.lastModifiedDate = lastModifiedDate;
		this.crc32 = crc32;
		this.compressedSize = compressedSize;
		this.uncompressedSize = uncompressedSize;
		this.fileNameBytes = fileName;
		this.extraFieldBytes = extraFieldBytes;
		this.zip64ExtraField = zip64ExtraField;
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Read from the input stream.
	 */
	public static ZipFileHeader read(RewindableInputStream inputStream) throws IOException {
		/*
		 * When reading a file-header we aren't sure if this is a file-header or the start of the central directory.
		 */
		int first = IoUtils.readInt(inputStream, "ZipFileHeader.signature");
		if (first != EXPECTED_SIGNATURE) {
			inputStream.rewind(4);
			return null;
		}

		Builder builder = new ZipFileHeader.Builder();
		builder.versionNeeded = IoUtils.readShort(inputStream, "ZipFileHeader.versionNeeded");
		builder.generalPurposeFlags = IoUtils.readShort(inputStream, "ZipFileHeader.generalPurposeFlags");
		builder.compressionMethod = IoUtils.readShort(inputStream, "ZipFileHeader.compressionMethod");
		builder.lastModifiedTime = IoUtils.readShort(inputStream, "ZipFileHeader.lastModifiedTime");
		builder.lastModifiedDate = IoUtils.readShort(inputStream, "ZipFileHeader.lastModifiedDate");
		builder.crc32 = IoUtils.readIntAsLong(inputStream, "ZipFileHeader.crc32");
		builder.compressedSize = IoUtils.readInt(inputStream, "ZipFileHeader.compressedSize");
		builder.uncompressedSize = IoUtils.readInt(inputStream, "ZipFileHeader.uncompressedSize");
		int fileNameLength = IoUtils.readShort(inputStream, "ZipFileHeader.fileNameLength");
		int extraLength = IoUtils.readShort(inputStream, "ZipFileHeader.extraLength");
		builder.fileNameBytes = IoUtils.readBytes(inputStream, fileNameLength, "ZipFileHeader.fileName");
		builder.extraFieldBytes =
				IoUtils.readBytes(inputStream, extraLength, "ZipFileHeader.extra");
		return builder.build();
	}

	/**
	 * Write to the input stream.
	 */
	public void write(OutputStream outputStream) throws IOException {
		IoUtils.writeInt(outputStream, EXPECTED_SIGNATURE);
		IoUtils.writeShort(outputStream, versionNeeded);
		int flags = generalPurposeFlags;
		if (needsDataDescriptor()) {
			flags |= GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();
		}
		IoUtils.writeShort(outputStream, flags);
		IoUtils.writeShort(outputStream, compressionMethod);
		IoUtils.writeShort(outputStream, lastModifiedTime);
		IoUtils.writeShort(outputStream, lastModifiedDate);
		IoUtils.writeInt(outputStream, crc32);
		IoUtils.writeInt(outputStream, compressedSize);
		IoUtils.writeInt(outputStream, uncompressedSize);
		IoUtils.writeShortBytesLength(outputStream, fileNameBytes);
		IoUtils.writeShortBytesLength(outputStream, extraFieldBytes);
		IoUtils.writeBytes(outputStream, fileNameBytes);
		IoUtils.writeBytes(outputStream, extraFieldBytes);
	}

	/**
	 * Return whether the header has this flag.
	 */
	public boolean hasFlag(GeneralPurposeFlag flag) {
		return ((generalPurposeFlags & flag.getValue()) == flag.getValue());
	}

	public int getVersionNeeded() {
		return versionNeeded;
	}

	/**
	 * Extract the version portion from the version-made information.
	 */
	public int getVersionNeededMajorMinor() {
		return (versionNeeded & 0xFF);
	}

	/**
	 * Return the version needed in the from "#.#".
	 */
	public String getVersionNeededMajorMinorString() {
		int high = (versionNeeded & 0xFF) / 10;
		int low = (versionNeeded & 0xFF) % 10;
		return high + "." + low;
	}

	public int getGeneralPurposeFlags() {
		return generalPurposeFlags;
	}

	public Set<GeneralPurposeFlag> getGeneralPurposeFlagsAsEnums() {
		return GeneralPurposeFlag.fromInt(generalPurposeFlags);
	}

	/**
	 * Read the compression level from the flags.
	 */
	public int getCompressionLevel() {
		int deflateFlags = (generalPurposeFlags & 06);
		if (deflateFlags == GeneralPurposeFlag.DEFLATING_MAXIMUM.getValue()) {
			return Deflater.BEST_COMPRESSION;
		} else if (deflateFlags == GeneralPurposeFlag.DEFLATING_NORMAL.getValue()) {
			return Deflater.DEFAULT_COMPRESSION;
		} else if (deflateFlags == GeneralPurposeFlag.DEFLATING_FAST.getValue()) {
			// i guess this is right
			return (Deflater.DEFAULT_COMPRESSION + Deflater.BEST_SPEED) / 2;
		} else if (deflateFlags == GeneralPurposeFlag.DEFLATING_SUPER_FAST.getValue()) {
			return Deflater.BEST_SPEED;
		} else {
			// may not get here but let's be careful out there
			return Deflater.DEFAULT_COMPRESSION;
		}
	}

	public int getCompressionMethod() {
		return compressionMethod;
	}

	public CompressionMethod getCompressionMethodAsEnum() {
		return CompressionMethod.fromValue(compressionMethod);
	}

	public int getLastModifiedTime() {
		return lastModifiedTime;
	}

	/**
	 * Return last modified time as a string in 24-hour HH:MM:SS format.
	 */
	public String getLastModifiedTimeString() {
		int hour = (lastModifiedTime >> 11);
		int minute = ((lastModifiedTime >> 5) & 0x3F);
		int second = ((lastModifiedTime & 0x1F) * 2);
		String result = String.format("%d:%02d:%02d", hour, minute, second);
		return result;
	}

	public int getLastModifiedDate() {
		return lastModifiedDate;
	}

	/**
	 * Return last modified date as a string in YYYY.mm.dd format.
	 */
	public String getLastModifiedDateString() {
		int year = ((lastModifiedDate >> 9) & 0x7F) + 1980;
		int month = ((lastModifiedDate >> 5) & 0x0F);
		int day = (lastModifiedDate & 0x1F);
		String result = String.format("%d.%02d.%02d", year, month, day);
		return result;
	}

	/**
	 * Return last modified date and time as a {@link LocalDateTime}.
	 */
	public LocalDateTime getLastModifiedDateTime() {
		int year = ((lastModifiedDate >> 9) & 0x7F) + 1980;
		int month = ((lastModifiedDate >> 5) & 0x0F);
		int day = (lastModifiedDate & 0x1F);
		int hour = (lastModifiedTime >> 11);
		int minute = ((lastModifiedTime >> 5) & 0x3F);
		int second = ((lastModifiedTime & 0x1F) * 2);
		LocalDateTime localDateTime = LocalDateTime.of(year, month, day, hour, minute, second);
		return localDateTime;
	}

	public long getCrc32() {
		return crc32;
	}

	/**
	 * {@link Zip64ExtraField} in the {@link #getExtraFieldBytes()} that has the real compressed size. See
	 * {@link #getZip64CompressedSize()}.
	 */
	public long getCompressedSize() {
		return compressedSize;
	}

	/**
	 * Get the size of the compressed (encoded) bytes as encoded in the {@link Zip64ExtraField} which should be in the
	 * {@link #extraFieldBytes}. If there is no extra field then the value of the {@link #getCompressedSize()} is
	 * returned.
	 */
	public long getZip64CompressedSize() {
		if (zip64ExtraField == null) {
			return compressedSize;
		} else {
			return zip64ExtraField.getCompressedSize();
		}
	}

	/**
	 * Get the 32-bit size of the uncompressed, original (unencoded) bytes. This may return 0xFFFFFFFF to indicate that
	 * there is a {@link Zip64ExtraField} in the {@link #getExtraFieldBytes()} that has the real compressed size. See
	 * {@link #getZip64UncompressedSize()}.
	 */
	public long getUncompressedSize() {
		return uncompressedSize;
	}

	/**
	 * Get the size of the uncompressed (unencoded) bytes as encoded in the {@link Zip64ExtraField} which should be in
	 * the {@link #extraFieldBytes}. If there is no extra field then the value of the {@link #getUncompressedSize()} is
	 * returned.
	 */
	public long getZip64UncompressedSize() {
		if (zip64ExtraField == null) {
			return uncompressedSize;
		} else {
			return zip64ExtraField.getUncompressedSize();
		}
	}

	public byte[] getFileNameBytes() {
		return fileNameBytes;
	}

	public String getFileName() {
		if (fileNameBytes == null) {
			return null;
		} else {
			return new String(fileNameBytes);
		}
	}

	public byte[] getExtraFieldBytes() {
		return extraFieldBytes;
	}

	/**
	 * Returns the Zip64 extra field in the extra-bytes or null if none.
	 */
	public Zip64ExtraField getZip64ExtraField() {
		return zip64ExtraField;
	}

	/**
	 * Does this file header need a data-descriptor.
	 */
	public boolean needsDataDescriptor() {
		return false;
				//(compressionMethod != CompressionMethod.NONE.getValue() && (compressedSize == 0 || crc32 == 0));
	}

	@Override
	public String toString() {
		return "ZipFileHeader [name=" + getFileName() + ", method=" + compressionMethod + ", compSize=" + compressedSize
				+ ", uncompSize=" + uncompressedSize + ", extra-#-bytes="
				+ (extraFieldBytes == null ? "null" : extraFieldBytes.length) + "]";
	}

	/**
	 * Builder for {@link ZipFileHeader}.
	 */
	public static class Builder {
		private int versionNeeded;
		/** NOTE: we turn on the data-descriptor flag by default until a size or crc32 is set */
		private int generalPurposeFlags;
		private int compressionMethod = CompressionMethod.DEFLATED.getValue();
		private int lastModifiedTime;
		private int lastModifiedDate;
		private long crc32;
		private long compressedSize;
		private long uncompressedSize;
		private byte[] fileNameBytes;
		private byte[] extraFieldBytes;
		private ByteArrayOutputStream extraFieldsOutputStream;
		private Zip64ExtraField zip64ExtraField;
		private boolean zip64ExtraFieldInBytes;

		public Builder() {
			setLastModifiedDateTime(LocalDateTime.now());
		}

		/**
		 * Build an instance of the file-header.
		 */
		public ZipFileHeader build() {

			// if we don't have a zip64 field set then check our values and maybe add one
			if (zip64ExtraField == null) {
				if (uncompressedSize >= IoUtils.MAX_UNSIGNED_INT_VALUE //
						|| uncompressedSize < 0 //
						|| compressedSize >= IoUtils.MAX_UNSIGNED_INT_VALUE //
						|| compressedSize < 0) {
					zip64ExtraField = new Zip64ExtraField(uncompressedSize, compressedSize, 0, 0);
					uncompressedSize = IoUtils.MAX_UNSIGNED_INT_VALUE;
					compressedSize = IoUtils.MAX_UNSIGNED_INT_VALUE;
				}
			}

			// build the extra bytes
			byte[] extraBytes;
			if (extraFieldsOutputStream == null && zip64ExtraField == null) {
				extraBytes = extraFieldBytes;
			} else {
				if (extraFieldsOutputStream == null) {
					extraFieldsOutputStream = new ByteArrayOutputStream();
				}
				// we may have extracted the zip64ExtraField from the extraFieldBytes
				if (zip64ExtraField != null && !zip64ExtraFieldInBytes) {
					try {
						zip64ExtraField.write(extraFieldsOutputStream);
						// now it is inside of the bytes
						zip64ExtraFieldInBytes = true;
					} catch (IOException e) {
						// won't happen with ByteArrayOutputStream
					}
				}
				// tack on the extra field bytes if both were set
				if (extraFieldBytes != null) {
					try {
						extraFieldsOutputStream.write(extraFieldBytes);
					} catch (IOException e) {
						// won't happen with ByteArrayOutputStream
					}
				}
				extraBytes = extraFieldsOutputStream.toByteArray();
			}

			// if we are in deflate mode and the compression-size and CRC are 0 then we must have a data-descriptor
			// if (compressionMethod == CompressionMethod.DEFLATED.getValue() && (compressedSize == 0 || crc32 == 0)) generalPurposeFlags |= GeneralPurposeFlag.DATA_DESCRIPTOR.getValue();

			return new ZipFileHeader(versionNeeded, generalPurposeFlags, compressionMethod, lastModifiedTime,
					lastModifiedDate, crc32, compressedSize, uncompressedSize, fileNameBytes, extraBytes,
					zip64ExtraField);
		}

		/**
		 * Start a builder from a previous Zip file-header.
		 */
		public static Builder fromHeader(ZipFileHeader header) {
			Builder builder = new Builder();
			builder.versionNeeded = header.versionNeeded;
			builder.generalPurposeFlags = header.generalPurposeFlags;
			builder.compressionMethod = header.compressionMethod;
			builder.lastModifiedTime = header.lastModifiedTime;
			builder.lastModifiedDate = header.lastModifiedDate;
			builder.crc32 = header.crc32;
			builder.compressedSize = header.compressedSize;
			builder.uncompressedSize = header.uncompressedSize;
			builder.fileNameBytes = header.fileNameBytes;
			builder.extraFieldBytes = header.extraFieldBytes;
			return builder;
		}

		/**
		 * Initialize a builder with the last-modified date/time and file-name from a disk file. It uses the
		 * {@link File#getPath()} as the file-name.
		 */
		public static Builder fromFile(File file) {
			Builder builder = new Builder();
			builder.setLastModifiedDateTime(file.lastModified());
			builder.fileNameBytes = file.getPath().getBytes();
			return builder;
		}

		/**
		 * Clear all fields in the builder. This does set a couple of default fields.
		 */
		public void reset() {
			versionNeeded = 0;
			generalPurposeFlags = 0;
			compressionMethod = CompressionMethod.DEFLATED.getValue();
			lastModifiedTime = 0;
			lastModifiedDate = 0;
			crc32 = 0;
			compressedSize = 0;
			uncompressedSize = 0;
			fileNameBytes = null;
			extraFieldBytes = null;
		}

		public int getVersionNeeded() {
			return versionNeeded;
		}

		public void setVersionNeeded(int versionNeeded) {
			this.versionNeeded = versionNeeded;
		}

		public Builder withVersionNeeded(int versionNeeded) {
			this.versionNeeded = versionNeeded;
			return this;
		}

		public void setVersionNeededMajorMinor(int major, int minor) {
			this.versionNeeded = (major * 10 + minor);
		}

		public Builder withVersionNeededMajorMinor(int major, int minor) {
			setVersionNeededMajorMinor(major, minor);
			return this;
		}

		public int getGeneralPurposeFlags() {
			return generalPurposeFlags;
		}

		/**
		 * Sets the general-purpose-flags as a integer value. This overrides the value set by the add and with flag(s).
		 */
		public void setGeneralPurposeFlags(int generalPurposeFlags) {
			this.generalPurposeFlags = generalPurposeFlags;
		}

		/**
		 * Sets the general-purpose-flags as a integer value. This overrides the value set by the add and with flag(s).
		 */
		public Builder withGeneralPurposeFlags(int generalPurposeFlags) {
			this.generalPurposeFlags = generalPurposeFlags;
			return this;
		}

		/**
		 * Assign a flag via turning on and off. This updates the value set by {@link #setGeneralPurposeFlags(int)}.
		 */
		public void assignGeneralPurposeFlag(GeneralPurposeFlag flag, boolean value) {
			if (value) {
				this.generalPurposeFlags |= flag.getValue();
			} else {
				this.generalPurposeFlags &= ~flag.getValue();
			}
		}

		/**
		 * Return the set of GeneralPurposeFlag enums that make up the general-purpose-flags.
		 */
		public Set<GeneralPurposeFlag> getGeneralPurposeFlagAsEnums() {
			return GeneralPurposeFlag.fromInt(generalPurposeFlags);
		}

		/**
		 * Add a general-purpose-flag as an enum. This adds to the value set by {@link #setGeneralPurposeFlags(int)}.
		 */
		public void addGeneralPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
			generalPurposeFlags |= generalPurposeFlag.getValue();
		}

		/**
		 * Sets the general-purpose-flag as a an enums. This adds to the value set by
		 * {@link #setGeneralPurposeFlags(int)}.
		 */
		public Builder withGeneralPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
			addGeneralPurposeFlag(generalPurposeFlag);
			return this;
		}

		/**
		 * Clear a general-purpose-flag as an enum. This changes the value set by {@link #setGeneralPurposeFlags(int)}.
		 */
		public void clearGeneralPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
			generalPurposeFlags &= ~generalPurposeFlag.getValue();
		}

		/**
		 * Sets the general-purpose-flags as a set of enums. This adds to the value set by
		 * {@link #setGeneralPurposeFlags(int)}.
		 */
		public void addGeneralPurposeFlags(Collection<GeneralPurposeFlag> generalPurposeFlagSet) {
			for (GeneralPurposeFlag flag : generalPurposeFlagSet) {
				generalPurposeFlags |= flag.getValue();
			}
		}

		/**
		 * Sets the general-purpose-flags as a set of enums. This adds to the value set by
		 * {@link #setGeneralPurposeFlags(int)}.
		 */
		public Builder withGeneralPurposeFlags(Collection<GeneralPurposeFlag> generalPurposeFlagSet) {
			addGeneralPurposeFlags(generalPurposeFlagSet);
			return this;
		}

		/**
		 * Sets the general-purpose-flags as an array of enums. This adds to the value set by
		 * {@link #setGeneralPurposeFlags(int)}.
		 */
		public void addGeneralPurposeFlags(GeneralPurposeFlag... generalPurposeFlagEnums) {
			for (GeneralPurposeFlag flag : generalPurposeFlagEnums) {
				if (flag == GeneralPurposeFlag.DEFLATING_NORMAL //
						|| flag == GeneralPurposeFlag.DEFLATING_MAXIMUM //
						|| flag == GeneralPurposeFlag.DEFLATING_FAST //
						|| flag == GeneralPurposeFlag.DEFLATING_SUPER_FAST) {
					generalPurposeFlags = (generalPurposeFlags & ~06) | flag.getValue();
				} else {
					generalPurposeFlags |= flag.getValue();
				}
			}
		}

		/**
		 * Sets the general-purpose-flags as an array of enums. This adds to the value set by
		 * {@link #setGeneralPurposeFlags(int)}.
		 */
		public Builder withGeneralPurposeFlags(GeneralPurposeFlag... generalPurposeFlagEnums) {
			addGeneralPurposeFlags(generalPurposeFlagEnums);
			return this;
		}

		public int getCompressionMethod() {
			return compressionMethod;
		}

		public void setCompressionMethod(int compressionMethod) {
			this.compressionMethod = compressionMethod;
		}

		public Builder withCompressionMethod(int compressionMethod) {
			this.compressionMethod = compressionMethod;
			return this;
		}

		public CompressionMethod getCompressionMethodAsEnum() {
			return CompressionMethod.fromValue(compressionMethod);
		}

		public void setCompressionMethod(CompressionMethod method) {
			this.compressionMethod = method.getValue();
		}

		public Builder withCompressionMethod(CompressionMethod method) {
			this.compressionMethod = method.getValue();
			return this;
		}

		/**
		 * Last modified time in the MS-DOS time format.
		 */
		public int getLastModifiedTime() {
			return lastModifiedTime;
		}

		/**
		 * Last modified time in the MS-DOS time format.
		 */
		public void setLastModifiedTime(int lastModifiedTime) {
			this.lastModifiedTime = lastModifiedTime;
		}

		/**
		 * Last modified time in the MS-DOS time format.
		 */
		public Builder withLastModifiedTime(int lastModifiedTime) {
			this.lastModifiedTime = lastModifiedTime;
			return this;
		}

		/**
		 * Last modified date in the MS-DOS date format.
		 */
		public int getLastModifiedDate() {
			return lastModifiedDate;
		}

		/**
		 * Last modified date in the MS-DOS date format.
		 */
		public void setLastModifiedDate(int lastModifiedDate) {
			this.lastModifiedDate = lastModifiedDate;
		}

		/**
		 * Last modified date in the MS-DOS date format.
		 */
		public Builder withLastModifiedDate(int lastModifiedDate) {
			this.lastModifiedDate = lastModifiedDate;
			return this;
		}

		/**
		 * Set the lastModFileDate and lastModFileTime as a {@link LocalDateTime}. Warning, the time has a 2 second
		 * resolution so some normalization will occur.
		 */
		public void setLastModifiedDateTime(LocalDateTime lastModifiedDateTime) {
			int yearPart = ((lastModifiedDateTime.getYear() - 1980) << 9);
			int monthPart = (lastModifiedDateTime.getMonthValue() << 5);
			int dayPart = lastModifiedDateTime.getDayOfMonth();
			this.lastModifiedDate = (yearPart | monthPart | dayPart);
			int hourPart = (lastModifiedDateTime.getHour() << 11);
			int minutePart = (lastModifiedDateTime.getMinute() << 5);
			int secondPart = (lastModifiedDateTime.getSecond() / 2);
			this.lastModifiedTime = (hourPart | minutePart | secondPart);
		}

		/**
		 * Set the lastModFileDate and lastModFileTime as a {@link LocalDateTime}. Warning, the time has a 2 second
		 * resolution so some normalization will occur.
		 */
		public Builder withLastModifiedDateTime(LocalDateTime lastModifiedDateTime) {
			setLastModifiedDateTime(lastModifiedDateTime);
			return this;
		}

		/**
		 * Set the lastModFileDate and lastModFileTime as an epoch milliseconds. Warning, the time has a 2 second
		 * resolution so some normalization will occur.
		 */
		public void setLastModifiedDateTime(long dateTimeMillis) {
			LocalDateTime localDateTime =
					Instant.ofEpochMilli(dateTimeMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
			setLastModifiedDateTime(localDateTime);
		}

		/**
		 * Set the lastModFileDate and lastModFileTime as an epoch milliseconds. Warning, the time has a 2 second
		 * resolution so some normalization will occur.
		 */
		public Builder withLastModifiedDateTime(long dateTimeMillis) {
			setLastModifiedDateTime(dateTimeMillis);
			return this;
		}

		/**
		 * Set the lastModFileDate and lastModFileTime from epoch milliseconds last-modified value from the File.
		 * Warning, the time has a 2 second resolution so some normalization will occur.
		 */
		public void setLastModifiedDateTime(File file) {
			setLastModifiedDateTime(file.lastModified());
		}

		/**
		 * Set the lastModFileDate and lastModFileTime from epoch milliseconds last-modified value from the File.
		 * Warning, the time has a 2 second resolution so some normalization will occur.
		 */
		public Builder withLastModifiedDateTime(File file) {
			setLastModifiedDateTime(file);
			return this;
		}

		public long getCrc32() {
			return crc32;
		}

		/**
		 * Set to the crc checksum of the uncompressed bytes. This can be left as 0 if you want a
		 * {@link ZipDataDescriptor} written after the file data or if you have buffered enabled via
		 * {@link ZipFileOutput#enableFileBuffering(int, int)}.
		 */
		public void setCrc32(long crc32) {
			this.crc32 = crc32;
		}

		public Builder withCrc32(long crc32) {
			this.crc32 = crc32;
			return this;
		}

		public void setCrc32Value(CRC32 crc32) {
			this.crc32 = crc32.getValue();
		}

		public long getCompressedSize() {
			return compressedSize;
		}

		/**
		 * Set to the compressed (encoded) size of the bytes. This can be left as 0 if you want a
		 * {@link ZipDataDescriptor} written after the file data or if you have buffered enabled via
		 * {@link ZipFileOutput#enableFileBuffering(int, int)}. If this value is less than 0 or more than 0xFFFFFFFF
		 * then a Zip64 extra field will be written into the extra bytes if not otherwise specified. You can also set
		 * this to 0xFFFFFFFF and add a {@link Zip64ExtraField} to the {@link #setExtraFieldBytes(byte[])} or
		 * {@link #setZip64ExtraField(Zip64ExtraField)}.
		 */
		public void setCompressedSize(long compressedSize) {
			this.compressedSize = compressedSize;
		}

		/**
		 * Set to the compressed (encoded) size of the bytes. See {@link #setCompressedSize(long)} for more details.
		 */
		public Builder withCompressedSize(long compressedSize) {
			this.compressedSize = compressedSize;
			return this;
		}

		public long getUncompressedSize() {
			return uncompressedSize;
		}

		/**
		 * Set to the uncompressed (unencoded) size of the bytes. If this value is less than 0 or more than 0xFFFFFFFF
		 * then a Zip64 extra field will be written into the extra bytes if not otherwise specified. You can also set
		 * this to 0xFFFFFFFF and add a {@link Zip64ExtraField} to the {@link #setExtraFieldBytes(byte[])} or
		 * {@link #setZip64ExtraField(Zip64ExtraField)}.
		 */
		public void setUncompressedSize(long uncompressedSize) {
			this.uncompressedSize = uncompressedSize;
		}

		/**
		 * Set to the uncompressed (unencoded) size of the bytes. See {@link #setUncompressedSize(long)} for more
		 * details.
		 */
		public Builder withUncompressedSize(long uncompressedSize) {
			this.uncompressedSize = uncompressedSize;
			return this;
		}

		public byte[] getFileNameBytes() {
			return fileNameBytes;
		}

		public void setFileNameBytes(byte[] fileName) {
			this.fileNameBytes = fileName;
		}

		public Builder withFileNameBytes(byte[] fileName) {
			this.fileNameBytes = fileName;
			return this;
		}

		public String getFileName() {
			if (fileNameBytes == null) {
				return null;
			} else {
				return new String(fileNameBytes);
			}
		}

		public void setFileName(String fileName) {
			this.fileNameBytes = fileName.getBytes();
		}

		public Builder withFileName(String fileName) {
			this.fileNameBytes = fileName.getBytes();
			return this;
		}

		public byte[] getExtraFieldBytes() {
			return extraFieldBytes;
		}

		/**
		 * Set the extra-field-bytes.
		 * 
		 * NOTE: This will interrogate the array looking for a {@link Zip64ExtraField}.
		 */
		public void setExtraFieldBytes(byte[] extraFieldBytes) {
			this.extraFieldBytes = extraFieldBytes;
			if (zip64ExtraField == null) {
				// process the extra bytes looking for an zip64 extra field
				ByteArrayInputStream bais = new ByteArrayInputStream(extraFieldBytes);
				try {
					while (true) {
						BaseExtraField extraField = ExtraFieldUtil.readExtraField(bais, true);
						if (extraField == null) {
							break;
						}
						if (extraField instanceof Zip64ExtraField) {
							zip64ExtraField = (Zip64ExtraField) extraField;
							zip64ExtraFieldInBytes = true;
							break;
						}
					}
				} catch (IOException e) {
					// could happen with EOF which prolly means an invalid header but oh well
				}
			}
		}

		/**
		 * Set the extra-field-bytes.
		 * 
		 * NOTE: This will interrogate the array looking for a {@link Zip64ExtraField}.
		 */
		public Builder withExtraFieldBytes(byte[] extraFieldBytes) {
			setExtraFieldBytes(extraFieldBytes);
			return this;
		}

		/**
		 * Add an extra field to the header other than the {@link #setZip64ExtraField(Zip64ExtraField)}. You should most
		 * likely either call {@link #setExtraFieldBytes(byte[])} or this method.
		 */
		public Builder addExtraField(BaseExtraField extraField) {
			// is this a zip64extra field? someone didn't read the javadocs.
			if (extraField instanceof Zip64ExtraField) {
				return withZip64ExtraField((Zip64ExtraField) extraField);
			}
			if (extraField.getId() == Zip64ExtraField.EXPECTED_ID) {
				throw new IllegalArgumentException("You cannot add an extra field with id "
						+ Zip64ExtraField.EXPECTED_ID + " and should be using setZip64ExtraField(...)");
			}
			if (extraFieldsOutputStream == null) {
				extraFieldsOutputStream = new ByteArrayOutputStream();
			}
			try {
				extraField.write(extraFieldsOutputStream);
			} catch (IOException e) {
				// won't happen with byte array output stream
			}
			return this;
		}

		public Zip64ExtraField getZip64ExtraField() {
			return zip64ExtraField;
		}

		public void setZip64ExtraField(Zip64ExtraField zip64ExtraField) {
			this.zip64ExtraField = zip64ExtraField;
		}

		public Builder withZip64ExtraField(Zip64ExtraField zip64ExtraField) {
			this.zip64ExtraField = zip64ExtraField;
			return this;
		}
	}
}
