package com.j256.simplezip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Input/output utility methods.
 * 
 * @author graywatson
 */
public class IoUtils {

	public static int STANDARD_BUFFER_SIZE = 4096;
	public static int MAX_UNSIGNED_SHORT_VALUE = 65535;
	public static long MAX_UNSIGNED_INT_VALUE = 4294967295L;
	private static final byte[] NO_BYTES = new byte[0];

	/**
	 * Read a byte from the input stream
	 * 
	 * @throws IOException
	 *             If end is reached or some io problem occurs.
	 */
	public static int readByte(InputStream input, String label) throws IOException {
		int value = input.read();
		if (value < 0) {
			throw new EOFException("reached unexpected EOF while reading " + label);
		} else {
			return value;
		}
	}

	/**
	 * Read a 2-byte short in little-endian from the input stream.
	 * 
	 * @throws IOException
	 *             If end is reached or some io problem occurs.
	 */
	public static int readShort(InputStream input, String label) throws IOException {
		return ((readByte(input, label) & 0xFF) << 0) //
				| ((readByte(input, label) & 0xFF) << 8);
	}

	/**
	 * Read an 4-byte int in little-endian from the input stream.
	 * 
	 * @throws IOException
	 *             If end is reached or some io problem occurs.
	 */
	public static int readInt(InputStream input, String label) throws IOException {
		return ((readByte(input, label) & 0xFF) << 0) //
				| ((readByte(input, label) & 0xFF) << 8) //
				| ((readByte(input, label) & 0xFF) << 16) //
				| ((readByte(input, label) & 0xFF) << 24);
	}

	/**
	 * Read an 4-byte int in little-endian from the input stream as a long. This is done to handle positive integer
	 * values larger than Integer.MAX_VALUE.
	 * 
	 * @throws IOException
	 *             If end is reached or some io problem occurs.
	 */
	public static long readIntAsLong(InputStream input, String label) throws IOException {
		return (((long) readByte(input, label) & 0xFF) << 0) //
				| (((long) readByte(input, label) & 0xFF) << 8) //
				| (((long) readByte(input, label) & 0xFF) << 16) //
				| (((long) readByte(input, label) & 0xFF) << 24);
	}

	/**
	 * Read a 8-byte long in little-endian from the input stream.
	 * 
	 * @throws IOException
	 *             If end is reached or some io problem occurs.
	 */
	public static long readLong(InputStream input, String label) throws IOException {
		return (((long) readByte(input, label) & 0xFF) << 0) //
				| (((long) readByte(input, label) & 0xFF) << 8) //
				| (((long) readByte(input, label) & 0xFF) << 16) //
				| (((long) readByte(input, label) & 0xFF) << 24) //
				| (((long) readByte(input, label) & 0xFF) << 32) //
				| (((long) readByte(input, label) & 0xFF) << 40) //
				| (((long) readByte(input, label) & 0xFF) << 48) //
				| (((long) readByte(input, label) & 0xFF) << 56);
	}

	/**
	 * Read an array of bytes from the input stream.
	 * 
	 * @throws IOException
	 *             If end is reached or some io problem occurs.
	 */
	public static byte[] readBytes(InputStream input, int size, String label) throws IOException {
		if (size == 0) {
			return NO_BYTES;
		} else {
			return readFully(input, size, label);
		}
	}

	/**
	 * Write a byte to the output stream.
	 */
	public static void writeByte(OutputStream output, int value) throws IOException {
		output.write(value);
	}

	/**
	 * Write a 2-byte short in little-endian to the output stream.
	 */
	public static void writeShort(OutputStream output, int value) throws IOException {
		output.write((byte) ((value >> 0) & 0xFF));
		output.write((byte) ((value >> 8) & 0xFF));
	}

	/**
	 * Write a 4-byte int in little-endian to the output stream.
	 */
	public static void writeInt(OutputStream output, long value) throws IOException {
		output.write((byte) ((value >> 0) & 0xFF));
		output.write((byte) ((value >> 8) & 0xFF));
		output.write((byte) ((value >> 16) & 0xFF));
		output.write((byte) ((value >> 24) & 0xFF));
	}

	/**
	 * Write a 8-byte long in little-endian to the output stream.
	 */
	public static void writeLong(OutputStream output, long value) throws IOException {
		output.write((byte) ((value >> 0) & 0xFF));
		output.write((byte) ((value >> 8) & 0xFF));
		output.write((byte) ((value >> 16) & 0xFF));
		output.write((byte) ((value >> 24) & 0xFF));
		output.write((byte) ((value >> 32) & 0xFF));
		output.write((byte) ((value >> 40) & 0xFF));
		output.write((byte) ((value >> 48) & 0xFF));
		output.write((byte) ((value >> 56) & 0xFF));
	}

	/**
	 * Write the length of bytes as a little-endian short to the output stream.
	 */
	public static void writeShortBytesLength(OutputStream output, byte[] bytes) throws IOException {
		if (bytes == null) {
			writeShort(output, 0);
		} else {
			writeShort(output, bytes.length);
		}
	}

	/**
	 * Write an array of bytes to the output stream.
	 */
	public static void writeBytes(OutputStream output, byte[] bytes) throws IOException {
		if (bytes != null && bytes.length > 0) {
			output.write(bytes);
		}
	}

	/**
	 * Copy the bytes from the input stream to the output stream.
	 */
	public static void copyStream(InputStream inputStream, OutputStream output) throws IOException {
		byte[] buffer = new byte[4096];
		while (true) {
			int num = inputStream.read(buffer);
			if (num < 0) {
				break;
			}
			output.write(buffer, 0, num);
		}
	}

	/**
	 * Read a complete buffer of bytes of a certain length.
	 */
	public static void readFully(InputStream input, byte[] bytes, int length, String label) throws IOException {
		int offset = 0;
		while (length > 0) {
			int numRead = input.read(bytes, offset, length);
			if (numRead < 0) {
				throw new EOFException("reached unexpected EOF while reading " + length + " bytes for " + label);
			}
			length -= numRead;
			offset += numRead;
		}
	}

	private static byte[] readFully(InputStream input, int length, String label) throws IOException {
		byte[] bytes = new byte[length];
		readFully(input, bytes, length, label);
		return bytes;
	}
}
