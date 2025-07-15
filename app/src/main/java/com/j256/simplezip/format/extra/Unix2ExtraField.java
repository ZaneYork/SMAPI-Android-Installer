package com.j256.simplezip.format.extra;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;

/**
 * Unit extra field #2.
 * 
 * @author graywatson
 */
public class Unix2ExtraField extends BaseExtraField {

	public static final int EXPECTED_ID = 0x756e;
	public static final int EXTRA_SIZE = 8 + 8;

	private final int userId;
	private final int groupId;

	public Unix2ExtraField(int userId, int groupId) {
		super(EXPECTED_ID, EXTRA_SIZE);
		this.userId = userId;
		this.groupId = groupId;
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Read from the input-stream.
	 */
	public static Unix2ExtraField read(InputStream inputStream, int id, int size) throws IOException {
		Builder builder = new Unix2ExtraField.Builder();
		builder.userId = IoUtils.readShort(inputStream, "Unix2ExtraField.userId");
		builder.groupId = IoUtils.readShort(inputStream, "Unix2ExtraField.groupId");
		return builder.build();
	}

	/**
	 * Write extra-field to the output-stream.
	 */
	@Override
	public void write(OutputStream outputStream) throws IOException {
		super.write(outputStream);
		IoUtils.writeShort(outputStream, userId);
		IoUtils.writeShort(outputStream, groupId);
	}

	public int getUserId() {
		return userId;
	}

	public int getGroupId() {
		return groupId;
	}

	/**
	 * Builder for {@link Unix2ExtraField}.
	 */
	public static class Builder {
		private int userId;
		private int groupId;

		/**
		 * Build and return the extra field. 
		 */
		public Unix2ExtraField build() {
			return new Unix2ExtraField(userId, groupId);
		}

		public int getUserId() {
			return userId;
		}

		public void setUserId(int userId) {
			this.userId = userId;
		}

		public int getGroupId() {
			return groupId;
		}

		public void setGroupId(int groupId) {
			this.groupId = groupId;
		}
	}
}
