package com.j256.simplezip.format.extra;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.ZipStatus;

/**
 * Abstract class for all extra fields.
 * 
 * @author graywatson
 */
public abstract class BaseExtraField {

	private final int id;
	private final int extraSize;

	protected BaseExtraField(int id, int extraSize) {
		this.id = id;
		this.extraSize = extraSize;
	}

	/**
	 * Write the base portions of the extra field.
	 */
	public void write(OutputStream outputStream) throws IOException {
		IoUtils.writeShort(outputStream, id);
		IoUtils.writeShort(outputStream, extraSize);
	}

	public int getId() {
		return id;
	}

	public int getExtraSize() {
		return extraSize;
	}

	/**
	 * Get the bytes for this field including the id and size.
	 */
	public byte[] getExtraFieldBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(2 + 2 + extraSize);
		try {
			write(baos);
		} catch (IOException e) {
			// not going to happen with ByteArrayOutputStream
		}
		return baos.toByteArray();
	}

	/**
	 * Validate the field.
	 */
	public ZipStatus validate() {
		return ZipStatus.OK;
	}
}
