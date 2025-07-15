package com.j256.simplezip.format;

import java.io.File;

/**
 * Additional file information that can be written into a central-directory file-entry that does not exist in the
 * {@link ZipFileHeader}.
 * 
 * @author graywatson
 */
public class ZipCentralDirectoryFileInfo {

	private int versionMade;
	private int versionNeeded;
	private int diskNumberStart;
	private int internalFileAttributes;
	private int externalFileAttributes;
	private final byte[] commentBytes;

	public ZipCentralDirectoryFileInfo(int versionMade, int versionNeeded, int diskNumberStart,
			int internalFileAttributes, int externalFileAttributes, byte[] commentBytes) {
		this.versionMade = versionMade;
		this.versionNeeded = versionNeeded;
		this.diskNumberStart = diskNumberStart;
		this.internalFileAttributes = internalFileAttributes;
		this.externalFileAttributes = externalFileAttributes;
		this.commentBytes = commentBytes;
	}

	/**
	 * Make a builder for this class.
	 */
	public static Builder builder() {
		return new Builder();
	}

	public int getVersionMade() {
		return versionMade;
	}

	/**
	 * Extract the platform from the version-made information.
	 */
	public Platform getPlatformMade() {
		return Platform.fromValue((versionMade >> 8) & 0xFF);
	}

	/**
	 * Extract the needed version from the version-made field.
	 */
	public int getVersionMadeMajorMinor() {
		return (versionMade & 0xFF);
	}

	/**
	 * Get the version made value as a #.# string.
	 */
	public String getVersionMadeMajorMinorString() {
		int high = (versionMade & 0xFF) / 10;
		int low = (versionMade & 0xFF) % 10;
		return high + "." + low;
	}

	public int getVersionNeeded() {
		return versionNeeded;
	}

	/**
	 * Extract the needed version from the version-needed field.
	 */
	public int getVersionNeededMajorMinor() {
		return (versionNeeded & 0xFF);
	}

	/**
	 * Get the version needed value as a #.# string.
	 */
	public String getVersionNeededMajorMinorString() {
		int high = (versionNeeded & 0xFF) / 10;
		int low = (versionNeeded & 0xFF) % 10;
		return high + "." + low;
	}

	public int getDiskNumberStart() {
		return diskNumberStart;
	}

	public int getInternalFileAttributes() {
		return internalFileAttributes;
	}

	/**
	 * Return whether this is a text file or not based on the internalFileAttributes.
	 */
	public boolean isTextFile() {
		return ((internalFileAttributes & ZipCentralDirectoryFileEntry.INTERNAL_ATTRIBUTES_TEXT_FILE) != 0);
	}

	public int getExternalFileAttributes() {
		return externalFileAttributes;
	}

	public byte[] getCommentBytes() {
		return commentBytes;
	}

	public String getComment() {
		if (commentBytes == null) {
			return null;
		} else {
			return new String(commentBytes);
		}
	}

	/**
	 * Builder for {@link ZipCentralDirectoryFileInfo}.
	 */
	public static class Builder {
		private int versionMade;
		private int versionNeeded;
		private int diskNumberStart = ZipCentralDirectoryFileEntry.DEFAULT_DISK_NUMBER;
		private int internalFileAttributes;
		private int externalFileAttributes;
		private byte[] commentBytes;

		public Builder() {
			// detect and set the platform and version automatically
			setPlatformMade(Platform.detectPlatform());
			setVersionMadeMajorMinor(2, 0);
			setVersionNeededMajorMinor(1, 0);
		}

		/**
		 * Create a builder from an existing central-directory file-entry.
		 */
		public static Builder fromCentralDirectoryFileEntry(ZipCentralDirectoryFileEntry entry) {
			Builder builder = new Builder();
			builder.versionMade = entry.getVersionMade();
			builder.versionNeeded = entry.getVersionNeeded();
			builder.diskNumberStart = entry.getDiskNumberStart();
			builder.internalFileAttributes = entry.getInternalFileAttributes();
			builder.externalFileAttributes = entry.getExternalFileAttributes();
			builder.commentBytes = entry.getCommentBytes();
			return builder;
		}

		/**
		 * Create a builder from a file which sets the externalFileAttributes.
		 */
		public static Builder fromFile(File file) {
			Builder builder = new Builder();
			builder.externalFileAttributes = ExternalFileAttributesUtils.fromFile(file);
			return builder;
		}

		/**
		 * Build an instance of the central-directory file info.
		 */
		public ZipCentralDirectoryFileInfo build() {
			return new ZipCentralDirectoryFileInfo(versionMade, versionNeeded, diskNumberStart, internalFileAttributes,
					externalFileAttributes, commentBytes);
		}

		public int getVersionMade() {
			return versionMade;
		}

		public void setVersionMade(int versionMade) {
			this.versionMade = versionMade;
		}

		public Builder withVersionMade(int versionMade) {
			this.versionMade = versionMade;
			return this;
		}

		public Platform getPlatformMade() {
			return Platform.fromValue((versionMade >> 8) & 0xFF);
		}

		public void setPlatformMade(Platform platform) {
			this.versionMade = ((this.versionMade & 0xFF) | (platform.getValue() << 8));
		}

		public Builder withPlatformMade(Platform platform) {
			setPlatformMade(platform);
			return this;
		}

		public int getVersionMadeMajorMinor() {
			return (versionMade & 0xFF);
		}

		/**
		 * Set the made zip version as a major and minor value. So if version 4.5 made this zip then you should pass in
		 * major 4 and minor 5.
		 */
		public void setVersionMadeMajorMinor(int major, int minor) {
			this.versionMade = ((this.versionMade & 0xFF00) | (major * 10 + minor));
		}

		/**
		 * Set the made zip version as a major and minor value. So if version 4.5 made this zip then you should pass in
		 * major 4 and minor 5.
		 */
		public Builder withVersionMadeMajorMinor(int major, int minor) {
			setVersionMadeMajorMinor(major, minor);
			return this;
		}

		public int getVersionNeeded() {
			return versionNeeded;
		}

		public void setVersionNeeded(int versionNeeded) {
			this.versionNeeded = versionNeeded;
		}

		public Builder withVersionNeeded(int versionNeeded) {
			this.versionNeeded = versionNeeded;
			return this;
		}

		/**
		 * Set the needed zip version as a major and minor value. So if version 2.0 made this zip then you should pass
		 * in major 2 and minor 0.
		 */
		public void setVersionNeededMajorMinor(int major, int minor) {
			this.versionNeeded = (major * 10 + minor);
		}

		/**
		 * Set the needed zip version as a major and minor value. So if version 2.0 made this zip then you should pass
		 * in major 2 and minor 0.
		 */
		public Builder withVersionNeededMajorMinor(int major, int minor) {
			setVersionNeededMajorMinor(major, minor);
			return this;
		}

		public int getDiskNumberStart() {
			return diskNumberStart;
		}

		public void setDiskNumberStart(int diskNumberStart) {
			this.diskNumberStart = diskNumberStart;
		}

		public Builder withDiskNumberStart(int diskNumberStart) {
			this.diskNumberStart = diskNumberStart;
			return this;
		}

		public int getInternalFileAttributes() {
			return internalFileAttributes;
		}

		public void setInternalFileAttributes(int internalFileAttributes) {
			this.internalFileAttributes = internalFileAttributes;
		}

		public Builder withInternalFileAttributes(int internalFileAttributes) {
			this.internalFileAttributes = internalFileAttributes;
			return this;
		}

		/**
		 * Gets from the internalFileAttributes.
		 */
		public boolean isTextFile() {
			return ((internalFileAttributes & ZipCentralDirectoryFileEntry.INTERNAL_ATTRIBUTES_TEXT_FILE) != 0);
		}

		/**
		 * Set in the internalFileAttributes.
		 */
		public void setTextFile(boolean textFile) {
			if (textFile) {
				internalFileAttributes |= ZipCentralDirectoryFileEntry.INTERNAL_ATTRIBUTES_TEXT_FILE;
			} else {
				internalFileAttributes &= ~ZipCentralDirectoryFileEntry.INTERNAL_ATTRIBUTES_TEXT_FILE;
			}
		}

		/**
		 * Set in the internalFileAttributes.
		 */
		public Builder withTextFile(boolean textFile) {
			setTextFile(textFile);
			return this;
		}

		public int getExternalFileAttributes() {
			return externalFileAttributes;
		}

		public void setExternalFileAttributes(int externalFileAttributes) {
			this.externalFileAttributes = externalFileAttributes;
		}

		public Builder withExternalFileAttributes(int externalFileAttributes) {
			this.externalFileAttributes = externalFileAttributes;
			return this;
		}

		/**
		 * Add the attributes parameter to the existing externalFileAttributes.
		 */
		public Builder addExternalFileAttributes(int attributes) {
			this.externalFileAttributes |= attributes;
			return this;
		}

		/**
		 * Set the externalFileAttributes from the attributes associated with the file argument.
		 */
		public void setExternalAttributesFromFile(File file) {
			this.externalFileAttributes = ExternalFileAttributesUtils.fromFile(file);
		}

		/**
		 * Set the externalFileAttributes from the attributes associated with the file argument.
		 */
		public Builder withExternalAttributesFromFile(File file) {
			setExternalAttributesFromFile(file);
			return this;
		}

		/**
		 * Set the MS-DOS file mode into the bottom of the externalFileAttributes.
		 */
		public void setMsDosExternalFileAttributes(int msDosFileAttributes) {
			externalFileAttributes =
					ExternalFileAttributesUtils.assignMsdosAttributes(externalFileAttributes, msDosFileAttributes);
		}

		/**
		 * Set the MS-DOS file mode into the bottom of the externalFileAttributes.
		 */
		public Builder withMsDosExternalFileAttributes(int msDosFileAttributes) {
			setMsDosExternalFileAttributes(msDosFileAttributes);
			return this;
		}

		/**
		 * Set the Unix file mode into the top of the externalFileAttributes. For example you can set this with 0644 or
		 * 0777.
		 */
		public void setUnixExternalFileAttributes(int unixFileAttributes) {
			externalFileAttributes =
					ExternalFileAttributesUtils.assignUnixFileAttributes(externalFileAttributes, unixFileAttributes);
		}

		/**
		 * Set the Unix file mode into the top of the externalFileAttributes. For example you can set this with 0644 or
		 * 0777.
		 */
		public Builder withUnixExternalFileAttributes(int unixFileAttributes) {
			setUnixExternalFileAttributes(unixFileAttributes);
			return this;
		}

		/**
		 * Set in the externalFileAttributes whether or not the file is a directory.
		 */
		public void setFileIsDirectory(boolean isDirectory) {
			if (isDirectory) {
				externalFileAttributes |= ExternalFileAttributesUtils.UNIX_DIRECTORY;
				externalFileAttributes |= ExternalFileAttributesUtils.MS_DOS_DIRECTORY;
			} else {
				externalFileAttributes &= ~ExternalFileAttributesUtils.UNIX_DIRECTORY;
				externalFileAttributes &= ~ExternalFileAttributesUtils.MS_DOS_DIRECTORY;
			}
		}

		/**
		 * Set in the externalFileAttributes whether or not the file is a directory.
		 */
		public Builder withFileIsDirectory(boolean isDirectory) {
			setFileIsDirectory(isDirectory);
			return this;
		}

		/**
		 * Set in the externalFileAttributes whether or not the file is a symbolic-link.
		 */
		public void setFileIsSymlink(boolean isSymlink) {
			if (isSymlink) {
				externalFileAttributes |= ExternalFileAttributesUtils.UNIX_SYMLINK;
			} else {
				externalFileAttributes &= ~ExternalFileAttributesUtils.UNIX_SYMLINK;
			}
		}

		/**
		 * Set in the externalFileAttributes whether or not the file is a symbolic-link.
		 */
		public Builder withFileIsSymlink(boolean isSymlink) {
			setFileIsSymlink(isSymlink);
			return this;
		}

		/**
		 * Set in the externalFileAttributes whether or not the file is a regular-file.
		 */
		public void setFileIsRegular(boolean isRegular) {
			if (isRegular) {
				externalFileAttributes |= ExternalFileAttributesUtils.UNIX_REGULAR_FILE;
			} else {
				externalFileAttributes &= ~ExternalFileAttributesUtils.UNIX_REGULAR_FILE;
			}
		}

		/**
		 * Set in the externalFileAttributes whether or not the file is a regular-file.
		 */
		public Builder withFileIsRegular(boolean isRegular) {
			setFileIsRegular(isRegular);
			return this;
		}

		/**
		 * Set in the externalFileAttributes whether or not the file is read-only.
		 */
		public void setFileIsReadOnly(boolean readOnly) {
			if (readOnly) {
				// add in read-only permissions
				externalFileAttributes = ExternalFileAttributesUtils.assignUnixAttributes(externalFileAttributes,
						ExternalFileAttributesUtils.UNIX_READ_ONLY_PERMISSIONS);
				externalFileAttributes |= ExternalFileAttributesUtils.MS_DOS_READONLY;
			} else {
				// not 100% sure this is correct but maybe the best we can do
				externalFileAttributes = ExternalFileAttributesUtils.assignUnixAttributes(externalFileAttributes,
						ExternalFileAttributesUtils.UNIX_READ_WRITE_PERMISSIONS);
				externalFileAttributes &= ~ExternalFileAttributesUtils.MS_DOS_READONLY;
			}
		}

		/**
		 * Set in the externalFileAttributes whether or not the file is read-only.
		 */
		public Builder withFileIsReadOnly(boolean readOnly) {
			setFileIsReadOnly(readOnly);
			return this;
		}

		public byte[] getCommentBytes() {
			return commentBytes;
		}

		public void setCommentBytes(byte[] commentBytes) {
			this.commentBytes = commentBytes;
		}

		public Builder withCommentBytes(byte[] commentBytes) {
			this.commentBytes = commentBytes;
			return this;
		}

		public String getComment() {
			if (commentBytes == null) {
				return null;
			} else {
				return new String(commentBytes);
			}
		}

		public void setComment(String comment) {
			if (comment == null) {
				commentBytes = null;
			} else {
				commentBytes = comment.getBytes();
			}
		}

		public Builder withComment(String comment) {
			setComment(comment);
			return this;
		}
	}
}
