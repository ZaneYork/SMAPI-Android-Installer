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
public class UnknownExtraField extends BaseExtraField {

	private final byte[] bytes;

	public UnknownExtraField(int id, byte[] bytes) {
		super(id, (bytes == null ? 0 : bytes.length));
		this.bytes = bytes;
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Read in from the input-stream.
	 */
	public static UnknownExtraField read(InputStream inputStream, int id, int extraSize) throws IOException {
		Builder builder = new UnknownExtraField.Builder();
		builder.id = id;
		builder.bytes = IoUtils.readBytes(inputStream, extraSize, "UnknownExtraField.bytes");
		return builder.build();
	}

	/**
	 * Write extra-field to the output-stream.
	 */
	@Override
	public void write(OutputStream outputStream) throws IOException {
		super.write(outputStream);
		IoUtils.writeBytes(outputStream, bytes);
	}

	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * Builder for {@link UnknownExtraField}.
	 */
	public static class Builder {
		private int id;
		private byte[] bytes;

		/**
		 * Build and return the extra field. 
		 */
		public UnknownExtraField build() {
			return new UnknownExtraField(id, bytes);
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public Builder withId(int id) {
			this.id = id;
			return this;
		}

		public byte[] getBytes() {
			return bytes;
		}

		public void setBytes(byte[] bytes) {
			this.bytes = bytes;
		}

		public Builder withBytes(byte[] bytes) {
			this.bytes = bytes;
			return this;
		}
	}
}
