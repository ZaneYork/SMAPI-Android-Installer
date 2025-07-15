package com.j256.simplezip.format.extra;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;

/**
 * Local file header extended timestamp information with last-modified, accessed, and created time stamps.
 * 
 * @author graywatson
 */
public class ExtendedTimestampLocalExtraField extends BaseExtraField {

	public static final int EXPECTED_ID = 0x5455;
	public static final int EXTRA_SIZE = 1 + 8 + 8 + 8;

	private final int flags;
	private final long timeLastModified;;
	private final long timeLastAccessed;
	private final long timeCreation;

	public ExtendedTimestampLocalExtraField(int flags, long timeLastModified, long timeLastAccessed,
			long timeCreation) {
		super(EXPECTED_ID, EXTRA_SIZE);
		this.flags = flags;
		this.timeLastModified = timeLastModified;
		this.timeLastAccessed = timeLastAccessed;
		this.timeCreation = timeCreation;
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
	public static ExtendedTimestampLocalExtraField read(InputStream inputStream, int id, int size) throws IOException {
		Builder builder = new ExtendedTimestampLocalExtraField.Builder();
		builder.flags = IoUtils.readByte(inputStream, "ExtendedTimestampLocalExtraField.flags");
		builder.timeLastModified = IoUtils.readLong(inputStream, "ExtendedTimestampLocalExtraField.timeLastModified");
		builder.timeLastAccessed = IoUtils.readLong(inputStream, "ExtendedTimestampLocalExtraField.timeLastAccessed");
		builder.timeCreation = IoUtils.readLong(inputStream, "ExtendedTimestampLocalExtraField.timeCreation");
		return builder.build();
	}

	/**
	 * Write extra-field to the output-stream.
	 */
	@Override
	public void write(OutputStream outputStream) throws IOException {
		super.write(outputStream);
		IoUtils.writeByte(outputStream, flags);
		IoUtils.writeLong(outputStream, timeLastModified);
		IoUtils.writeLong(outputStream, timeLastAccessed);
		IoUtils.writeLong(outputStream, timeCreation);
	}

	public int getFlags() {
		return flags;
	}

	public long getTimeLastModified() {
		return timeLastModified;
	}

	public long getTimeLastAccessed() {
		return timeLastAccessed;
	}

	public long getTimeCreation() {
		return timeCreation;
	}

	/**
	 * Builder for {@link ExtendedTimestampLocalExtraField}..
	 */
	public static class Builder {

		private int flags;
		private long timeLastModified;;
		private long timeLastAccessed;
		private long timeCreation;

		/**
		 * Build and return the extra field. 
		 */
		public ExtendedTimestampLocalExtraField build() {
			return new ExtendedTimestampLocalExtraField(flags, timeLastModified, timeLastAccessed, timeCreation);
		}

		public int getFlags() {
			return flags;
		}

		public void setFlags(int flags) {
			this.flags = flags;
		}

		public long getTimeLastModified() {
			return timeLastModified;
		}

		public void setTimeLastModified(long timeLastModified) {
			this.timeLastModified = timeLastModified;
		}

		public long getTimeLastAccessed() {
			return timeLastAccessed;
		}

		public void setTimeLastAccessed(long timeLastAccess) {
			this.timeLastAccessed = timeLastAccess;
		}

		public long getTimeCreation() {
			return timeCreation;
		}

		public void setTimeCreation(long timeCreation) {
			this.timeCreation = timeCreation;
		}
	}
}
