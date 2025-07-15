package com.j256.simplezip.format.extra;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.j256.simplezip.IoUtils;
import com.j256.simplezip.format.ZipCentralDirectoryFileEntry;
import com.j256.simplezip.format.ZipFileHeader;

/**
 * Utility for reading in the extra fields.
 * 
 * @author graywatson
 */
public class ExtraFieldUtil {

	/**
	 * Read in an extra field returning either for a local file or the central directory.
	 * 
	 * @param fileHeader
	 *            Set to true if we are processing extra-bytes from the {@link ZipFileHeader} or false if from
	 *            {@link ZipCentralDirectoryFileEntry}.
	 * 
	 * @return Extra field or null if none.
	 */
	public static <T extends BaseExtraField> T readExtraField(InputStream input, boolean fileHeader)
			throws IOException {
		try {
			return doReadExtraField(input, fileHeader);
		} catch (EOFException ee) {
			return null;
		}
	}

	private static <T extends BaseExtraField> T doReadExtraField(InputStream input, boolean fileHeader)
			throws IOException {
		/*
		 * When reading a file-header we aren't sure if this is a file-header or the start of the central directory.
		 */
		int id = IoUtils.readShort(input, "BaseExtraField.id");
		int size = IoUtils.readShort(input, "BaseExtraField.size");

		switch (id) {
			case ExtendedTimestampCentralExtraField.EXPECTED_ID: {
				if (fileHeader) {
					if (size == ExtendedTimestampLocalExtraField.EXTRA_SIZE) {
						@SuppressWarnings("unchecked")
						T extra = (T) ExtendedTimestampLocalExtraField.read(input, id, size);
						return extra;
					}
				} else {
					if (size >= ExtendedTimestampCentralExtraField.EXTRA_MINIMUM_SIZE) {
						@SuppressWarnings("unchecked")
						T extra = (T) ExtendedTimestampCentralExtraField.read(input, id, size);
						return extra;
					}
				}
				break;
			}
			case Unix1ExtraField.EXPECTED_ID: {
				if (size >= Unix1ExtraField.EXTRA_MINIMUM_SIZE) {
					@SuppressWarnings("unchecked")
					T extra = (T) Unix1ExtraField.read(input, id, size);
					return extra;
				}
				break;
			}
			case Unix2ExtraField.EXPECTED_ID: {
				if (size >= Unix2ExtraField.EXTRA_SIZE) {
					@SuppressWarnings("unchecked")
					T extra = (T) Unix2ExtraField.read(input, id, size);
					return extra;
				}
				break;
			}
			case Zip64ExtraField.EXPECTED_ID: {
				if (size == Zip64ExtraField.EXTRA_SIZE) {
					@SuppressWarnings("unchecked")
					T extra = (T) Zip64ExtraField.read(input, id, size);
					return extra;
				}
				break;
			}
			default: {
				// created below
				break;
			}
		}

		@SuppressWarnings("unchecked")
		T extra = (T) UnknownExtraField.read(input, id, size);
		return extra;
	}
}
