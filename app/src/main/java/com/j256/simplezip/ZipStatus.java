package com.j256.simplezip;

/**
 * The status of the Zip-file processing. Used to report on validation and other problems.
 * 
 * @author graywatson
 */
public class ZipStatus {

	/** standard all is ok status response */
	public static final ZipStatus OK = new ZipStatus(ZipStatusId.OK, "ok");

	private final String message;
	private final ZipStatusId id;

	public ZipStatus(ZipStatusId id, String message) {
		this.message = message;
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public ZipStatusId getId() {
		return id;
	}

	/**
	 * Enumerated status id for easy determining the source of the validation problem.
	 */
	public static enum ZipStatusId {
		HEADER_BAD_SIGNATURE,
		HEADER_CRC_NO_MATCH,
		HEADER_LENGTH_SHORT,
		HEADER_LENGTH_LONG,
		DESCRIPTOR_CRC_NO_MATCH,
		DESCRIPTOR_LENGTH_SHORT,
		DESCRIPTOR_LENGTH_LONG,
		/** everything checked out fine */
		OK,
		// end
		;
	}
}
