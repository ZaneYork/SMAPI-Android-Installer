package com.j256.simplezip.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class that sets file permissions to be assigned to the {@link ZipCentralDirectoryFileEntry}.
 * 
 * @author graywatson
 */
public class ExternalFileAttributesUtils {

	public static int UNIX_REGULAR_FILE = (0100000 << 16);
	public static int UNIX_DIRECTORY = (040000 << 16);
	public static int UNIX_SYMLINK = (0120000 << 16);

	public static int UNIX_READ_ONLY_PERMISSIONS = (0444 << 16);
	public static int UNIX_READ_WRITE_PERMISSIONS = (0644 << 16);
	public static int UNIX_READ_ONLY_EXECUTE_PERMISSIONS = (0555 << 16);
	private static int UNIX_READ_WRITE_EXECUTE_PERMISSIONS = (0755 << 16);
	private static final int MS_DOS_EXTERNAL_ATTRIBUTES_MASK = 0xFFFF;

	public static int MS_DOS_READONLY = 0x01;
	public static int MS_DOS_DIRECTORY = 0x010;

	/**
	 * Get the permissions flags from a file.
	 */
	public static int fromFile(File file) {
		return fromFile(file, false);
	}

	/**
	 * Get the permissions flags from a file using the Java permissions calls on {@link File}. This is exposed mostly
	 * for testing purposes.
	 */
	public static int fromFile(File file, boolean useJavaAttributes) {
		if (!file.exists()) {
			return 0;
		}
		int permissions = 0;
		if (file.isDirectory()) {
			permissions |= MS_DOS_DIRECTORY;
			permissions |= UNIX_DIRECTORY;
		} else if (isSymlink(file)) {
			permissions |= UNIX_SYMLINK;
		} else {
			permissions |= UNIX_REGULAR_FILE;
		}
		if (useJavaAttributes) {
			permissions |= extractJavaFileAttributes(file);
			return permissions;
		}
		try {
			// try to read in the posix permissions
			Path path = FileSystems.getDefault().getPath(file.getPath());
			Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
			permissions |= Permission.modeFromPermSet(perms);
		} catch (Exception e) {
			permissions |= extractJavaFileAttributes(file);
		}
		return permissions;
	}

	/**
	 * Set the permissions on an output file.
	 */
	public static void assignToFile(File file, int permissions) {
		assignToFile(file, permissions, false);
	}

	/**
	 * Set the permissions on an output file. This is exposed mostly for testing purposes.
	 */
	public static void assignToFile(File file, int permissions, boolean useJavaAttributes) {
		if (!file.exists()) {
			return;
		}
		if (useJavaAttributes) {
			assignJavaFileAttributes(file, permissions);
			return;
		}
		try {
			// try to read in the posix permissions
			Path path = FileSystems.getDefault().getPath(file.getPath());
			Set<PosixFilePermission> permSet = Permission.permSetFromMode(permissions);
			Files.setPosixFilePermissions(path, permSet);
		} catch (Exception e) {
			assignJavaFileAttributes(file, permissions);
		}
	}

	/**
	 * Assign MS-DOS attributes on our attributes integer and return it.
	 */
	public static int assignMsdosAttributes(int attributes, int msDosFileAttributes) {
		attributes = (attributes & ~MS_DOS_EXTERNAL_ATTRIBUTES_MASK) | (msDosFileAttributes & 0xFF);
		return attributes;
	}

	/**
	 * Assign Unix attributes on our attributes integer and return it.
	 */
	public static int assignUnixFileAttributes(int attributes, int unixFileAttributes) {
		return assignUnixAttributes(attributes, (unixFileAttributes << 16));
	}

	/**
	 * Assign the unix portion of the external file attributes.
	 */
	public static int assignUnixAttributes(int attributes, int unixFileAttributes) {
		attributes = ((attributes & MS_DOS_EXTERNAL_ATTRIBUTES_MASK) | unixFileAttributes);
		return attributes;
	}

	/**
	 * Get the permissions flags from a file.
	 */
	public static String toString(int attributes) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		if ((attributes & MS_DOS_EXTERNAL_ATTRIBUTES_MASK) != 0) {
			sb.append("ms-dos:");
			if ((attributes & MS_DOS_READONLY) == MS_DOS_READONLY) {
				sb.append(" read-only");
			}
			if ((attributes & MS_DOS_DIRECTORY) == MS_DOS_DIRECTORY) {
				sb.append(" directory");
			}
			first = false;
		}
		int unixAttributes = ((attributes & ~MS_DOS_EXTERNAL_ATTRIBUTES_MASK) >>> 16);
		if (unixAttributes != 0) {
			if (!first) {
				sb.append(", ");
			}
			sb.append("unix: 0").append(Integer.toOctalString(unixAttributes));
		}
		return sb.toString();
	}

	/**
	 * Extract the meager permissions from the File attributes if not posix.
	 */
	private static int extractJavaFileAttributes(File file) {
		int permissions = 0;
		if (file.canWrite()) {
			if (file.canExecute()) {
				permissions |= UNIX_READ_WRITE_EXECUTE_PERMISSIONS;
			} else {
				permissions |= UNIX_READ_WRITE_PERMISSIONS;
			}
		} else {
			permissions |= MS_DOS_READONLY;
			if (file.canExecute()) {
				permissions |= UNIX_READ_ONLY_EXECUTE_PERMISSIONS;
			} else {
				permissions |= UNIX_READ_ONLY_PERMISSIONS;
			}
		}
		return permissions;
	}

	/**
	 * Set the meager permissions on File if not posix.
	 */
	private static void assignJavaFileAttributes(File file, int permissions) {
		if ((permissions & UNIX_READ_WRITE_EXECUTE_PERMISSIONS) == UNIX_READ_WRITE_EXECUTE_PERMISSIONS) {
			file.setReadable(true);
			file.setWritable(true);
			file.setExecutable(true);
		} else if ((permissions & UNIX_READ_WRITE_PERMISSIONS) == UNIX_READ_WRITE_PERMISSIONS) {
			file.setReadable(true);
			file.setWritable(true);
			file.setExecutable(false);
		} else if ((permissions & UNIX_READ_ONLY_EXECUTE_PERMISSIONS) == UNIX_READ_ONLY_EXECUTE_PERMISSIONS) {
			file.setReadable(true);
			file.setWritable(false);
			file.setExecutable(true);
		} else if ((permissions & UNIX_READ_ONLY_PERMISSIONS) == UNIX_READ_ONLY_PERMISSIONS
				|| (permissions & MS_DOS_READONLY) == MS_DOS_READONLY) {
			file.setReadable(true);
			file.setWritable(false);
			file.setExecutable(false);
		}
	}

	/**
	 * Returns true if the file is a symlink otherwise false.
	 */
	private static boolean isSymlink(File file) {
		try {
			File canonFile;
			File canonDir = file.getParentFile();
			if (canonDir == null) {
				canonFile = file;
			} else {
				canonFile = new File(canonDir.getCanonicalFile(), file.getName());
			}
			return !canonFile.getCanonicalFile().equals(canonFile.getAbsoluteFile());
		} catch (IOException ioe) {
			// ignored
			return false;
		}
	}

	/**
	 * Mapping from the posix permissions to unix file modes.
	 */
	public static enum Permission {

		OWNER_READ(PosixFilePermission.OWNER_READ, (0400 << 16)),
		OWNER_WRITE(PosixFilePermission.OWNER_WRITE, (0200 << 16)),
		OWNER_EXECUTE(PosixFilePermission.OWNER_EXECUTE, (0100 << 16)),
		GROUP_READ(PosixFilePermission.GROUP_READ, (0040 << 16)),
		GROUP_WRITE(PosixFilePermission.GROUP_WRITE, (0020 << 16)),
		GROUP_EXECUTE(PosixFilePermission.GROUP_EXECUTE, (0010 << 16)),
		OTHERS_READ(PosixFilePermission.OTHERS_READ, (0004 << 16)),
		OTHERS_WRITE(PosixFilePermission.OTHERS_WRITE, (0002 << 16)),
		OTHERS_EXECUTE(PosixFilePermission.OTHERS_EXECUTE, (0001 << 16)),
		// end
		;

		private final PosixFilePermission permission;
		private final int value;
		private static final Map<PosixFilePermission, Integer> posixPermValueMap = new HashMap<>();

		static {
			for (Permission permValue : values()) {
				posixPermValueMap.put(permValue.permission, permValue.value);
			}
		}

		private Permission(PosixFilePermission permission, int value) {
			this.permission = permission;
			this.value = value;
		}

		/**
		 * Get a mode int from a collection of permissions.
		 */
		public static int modeFromPermSet(Collection<PosixFilePermission> permSet) {
			int mode = 0;
			for (PosixFilePermission perm : permSet) {
				Integer permValue = posixPermValueMap.get(perm);
				if (permValue != null) {
					mode |= permValue;
				}
			}
			return mode;
		}

		/**
		 * Get a collection of permissions based from the mode.
		 */
		public static Set<PosixFilePermission> permSetFromMode(int mode) {
			Set<PosixFilePermission> permSet = new HashSet<>(Permission.values().length);
			for (Permission permValue : Permission.values()) {
				if ((mode & permValue.value) > 0) {
					permSet.add(permValue.permission);
				}
			}
			return permSet;
		}
	}
}
