package com.j256.simplezip.format.extra;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;

/**
 * Central directory extended timestamp information.
 * 
 * @author graywatson
 */
public class ExtendedTimestampCentralExtraField extends BaseExtraField {

	public static final int EXPECTED_ID = 0x5455;
	public static final int EXTRA_MINIMUM_SIZE = 1;

	public static final int TIME_MODIFIED_FLAG = (1 << 0);
	public static final int TIME_ACCESSED_FLAG = (1 << 1);
	public static final int TIME_CREATED_FLAG = (1 << 2);

	private final int flags;
	private final Long time;

	public ExtendedTimestampCentralExtraField(int size, int flags, Long time) {
		super(EXPECTED_ID, size);
		this.flags = flags;
		this.time = time;
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
	public static ExtendedTimestampCentralExtraField read(InputStream inputStream, int id, int size)
			throws IOException {
		Builder builder = new ExtendedTimestampCentralExtraField.Builder();
		builder.flags = IoUtils.readByte(inputStream, "ExtendedTimestampCentralExtraField.flags");
		if (size >= EXTRA_MINIMUM_SIZE + 8) {
			builder.time = IoUtils.readLong(inputStream, "ExtendedTimestampCentralExtraField.time");
		}
		return builder.build();
	}

	/**
	 * Write extra-field to the output-stream.
	 */
	@Override
	public void write(OutputStream outputStream) throws IOException {
		super.write(outputStream);
		IoUtils.writeByte(outputStream, flags);
		if (time != null) {
			IoUtils.writeLong(outputStream, time);
		}
	}

	public int getFlags() {
		return flags;
	}

	/**
	 * Optional time value or null if none.
	 */
	public Long getTime() {
		return time;
	}

	public boolean isTimeModified() {
		return ((flags & TIME_MODIFIED_FLAG) != 0);
	}

	public boolean isTimeAccessed() {
		return ((flags & TIME_ACCESSED_FLAG) != 0);
	}

	public boolean isTimeCreated() {
		return ((flags & TIME_CREATED_FLAG) != 0);
	}

	/**
	 * Builder for @{link ExtendedTimestampCentralExtraField}.
	 */
	public static class Builder {

		private int flags;
		private Long time;;

		/**
		 * Build and return the extra field. 
		 */
		public ExtendedTimestampCentralExtraField build() {
			int size = EXTRA_MINIMUM_SIZE;
			if (time != null) {
				size += 8;
			}
			return new ExtendedTimestampCentralExtraField(size, flags, time);
		}

		public int getFlags() {
			return flags;
		}

		public void setFlags(int flags) {
			this.flags = flags;
		}

		public Long getTime() {
			return time;
		}

		public void setTime(Long time) {
			this.time = time;
		}

		public boolean isTimeModified() {
			return ((flags & TIME_MODIFIED_FLAG) != 0);
		}

		public void setTimeModified(boolean timeModified) {
			if (timeModified) {
				this.flags |= TIME_MODIFIED_FLAG;
			} else {
				this.flags &= ~TIME_MODIFIED_FLAG;
			}
		}

		public boolean isTimeAccessed() {
			return ((flags & TIME_ACCESSED_FLAG) != 0);
		}

		public void setTimeAccessed(boolean timeAccessed) {
			if (timeAccessed) {
				this.flags |= TIME_ACCESSED_FLAG;
			} else {
				this.flags &= ~TIME_ACCESSED_FLAG;
			}
		}

		public boolean isTimeCreated() {
			return ((flags & TIME_CREATED_FLAG) != 0);
		}

		public void setTimeCreated(boolean timeCreated) {
			if (timeCreated) {
				this.flags |= TIME_CREATED_FLAG;
			} else {
				this.flags &= ~TIME_CREATED_FLAG;
			}
		}
	}
}
