package com.j256.simplezip.format;

/**
 * Encoded as "version made by" in the {@link ZipCentralDirectoryFileEntry#getVersionMade()}.
 * 
 * @author graywatson
 */
public enum Platform {

	MSDOS_AND_OS2(0, "ms-dos"),
	AMIGA(1, "amiga"),
	OPENVMS(2, "openvms"),
	UNIX(3, "unix"),
	VM_CMS(4, "vm-cms"),
	ATARI_ST(5, "atari"),
	OS2_HPFS(6, "os2"),
	MACINTOSH(7, "macintosh"),
	Z_SYSTEM(8, "z-system"),
	CPM(9, "cpm"),
	WINDOWS(10, "windows"),
	MVS(11, "mvs"),
	VSE(12, "vse"),
	ACORD(13, "acord"),
	VFAT(14, "vfat"),
	ALT_MVS(15, "mvs alt"),
	BEOS(16, "beqs"),
	TANDEM(17, "tandem"),
	OS400(18, "os400"),
	OSX(19, "osx"),
	OTHER(-1, "other"),
	// end
	;

	private final int value;
	private final String label;

	private Platform(int value, String label) {
		this.value = value;
		this.label = label;
	}

	public int getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}

	/**
	 * Detect our platform by looking at various JDK attributes.
	 */
	public static Platform detectPlatform() {
		return detectPlatform(System.getProperty("os.name"));
	}

	/**
	 * Detect our platform by string. Exposed mainly for testing purposes.
	 */
	public static Platform detectPlatform(String os) {
		if (os == null) {
			return OTHER;
		}
		os = os.toLowerCase();
		if (os.contains("os x")) {
			return UNIX;
		} else if (os.contains("windows")) {
			return WINDOWS;
		}
		return OTHER;
	}

	/**
	 * Given an integer, return the associated platform..
	 */
	public static Platform fromValue(int value) {
		for (Platform platform : values()) {
			if (platform.value == value) {
				return platform;
			}
		}
		return OTHER;
	}
}
