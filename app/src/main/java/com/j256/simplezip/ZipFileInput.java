package com.j256.simplezip;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.j256.simplezip.codec.FileDataDecoder;
import com.j256.simplezip.codec.InflatorFileDataDecoder;
import com.j256.simplezip.codec.SimpleZipFileDataDecoder;
import com.j256.simplezip.codec.StoredFileDataDecoder;
import com.j256.simplezip.format.CompressionMethod;
import com.j256.simplezip.format.ExternalFileAttributesUtils;
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.Zip64CentralDirectoryEnd;
import com.j256.simplezip.format.Zip64CentralDirectoryEndLocator;
import com.j256.simplezip.format.ZipCentralDirectoryEnd;
import com.j256.simplezip.format.ZipCentralDirectoryFileEntry;
import com.j256.simplezip.format.ZipDataDescriptor;
import com.j256.simplezip.format.ZipFileHeader;
import com.starry.FileUtils;

/**
 * Read in a Zip-file either from a {@link File} or an {@link InputStream}.
 * 
 * @author graywatson
 */
public class ZipFileInput implements Closeable {

	private final RewindableInputStream inputStream;
	private final ZipFileDataInfo fileDataCountingInfo = new ZipFileDataInfo();
	private final byte[] tmpBuffer = new byte[IoUtils.STANDARD_BUFFER_SIZE];

	private FileDataDecoder fileDataDecoder;
	private ZipFileHeader currentFileHeader;
	private ZipDataDescriptor currentDataDescriptor;
	private boolean currentFileEofReached = true;
	private ZipFileDataInputStream fileDataInputStream;
	private boolean readTillEof;
	private Map<String, File> outputFileMap;

	/**
	 * Start reading a Zip-file from the file-path. You must call {@link #close()} to close the stream when you are
	 * done.
	 */
	public ZipFileInput(String path) throws IOException {
		this(new File(path));
	}

	/**
	 * Read a Zip-file from a file. You must call {@link #close()} to close the stream when you are done.
	 */
	public ZipFileInput(File file) throws IOException {
		this(FileUtils.getInputStream(file));
	}

	/**
	 * Read a Zip-file from an input-stream. You must call {@link #close()} to close the stream when you are done.
	 */
	public ZipFileInput(InputStream inputStream) {
		this.inputStream = new RewindableInputStream(inputStream, IoUtils.STANDARD_BUFFER_SIZE);
		readTillEof = true;
	}

	/**
	 * Read the next file header from the zip file. This is first thing that you will call after opening the Zip file.
	 */
	public ZipFileHeader readFileHeader() throws IOException {
		if (!currentFileEofReached) {
			skipFileData();
		}
		currentDataDescriptor = null;
		currentFileHeader = ZipFileHeader.read(inputStream);
		if (currentFileHeader != null) {
			currentFileEofReached = false;
			// reset the counting info now that we are ready to read the next file
			fileDataCountingInfo.reset();
		}
		return currentFileHeader;
	}

	/**
	 * Return an iterator that can be used to step across the file-headers. The iterator will return false for
	 * {@link Iterator#hasNext()} and null for {@link Iterator#next()} once the end has been reached.
	 */
	public Iterator<ZipFileHeader> fileHeaderIterator() {
		return new FileHeaderIterator();
	}

	/**
	 * Skip over the file data in the zip.
	 * 
	 * @return The number of bytes skipped.
	 */
	public long skipFileData() throws IOException {
		long byteCount = 0;
		while (true) {
			int numRead = readFileDataPart(tmpBuffer, 0, tmpBuffer.length);
			if (numRead < 0) {
				break;
			}
			byteCount += numRead;
		}
		// NOTE: close gets called in readFileDataPart()
		return byteCount;
	}

	/**
	 * Read file data from the Zip stream, decode it, and write it to the file path argument.
	 * 
	 * @param outputPath
	 *            Where to write the data read from the zip stream.
	 * @return THe number of bytes written into the output-stream.
	 */
	public long readFileDataToFile(String outputPath) throws IOException {
		return readFileDataToFile(new File(outputPath));
	}

	/**
	 * Read file data from the Zip stream, decode it, and write it to the file argument. This will associate the File
	 * with the current header file-name so you can call assign the permissions for the file with a later call to
	 * {@link #assignDirectoryFileEntryPermissions(ZipCentralDirectoryFileEntry)} or
	 * {@link #readDirectoryFileEntriesAndAssignPermissions()}.
	 * 
	 * @param outputFile
	 *            Where to write the data read from the zip stream.
	 * @return THe number of bytes written into the output-stream.
	 */
	public long readFileDataToFile(File outputFile) throws IOException {
		try(OutputStream os = FileUtils.getOutputStream(outputFile)) {
			long numBytes = readFileData(os);
			if (outputFileMap == null) {
				outputFileMap = new HashMap<>();
			}
			outputFileMap.put(currentFileHeader.getFileName(), outputFile);
			return numBytes;
		}

	}

	/**
	 * Read file data from the Zip stream, decode it, and write it to the output-steam argument.
	 * 
	 * @return THe number of bytes written into the output-stream.
	 */
	public long readFileData(OutputStream outputStream) throws IOException {
		long byteCount = 0;
		while (true) {
			int numRead = readFileDataPart(tmpBuffer, 0, tmpBuffer.length);
			if (numRead < 0) {
				break;
			}
			outputStream.write(tmpBuffer, 0, numRead);
			byteCount += numRead;
		}
		return byteCount;
	}

	/**
	 * Read raw file data from the Zip stream, no decoding, and write it to the output-steam argument.
	 * 
	 * @return THe number of bytes written into the output-stream.
	 */
	public long readRawFileData(OutputStream outputStream) throws IOException {
		long byteCount = 0;
		while (true) {
			int numRead = readRawFileDataPart(tmpBuffer, 0, tmpBuffer.length);
			if (numRead < 0) {
				break;
			}
			outputStream.write(tmpBuffer, 0, numRead);
			byteCount += numRead;
		}
		return byteCount;
	}

	/**
	 * Get an input stream suitable for reading the bytes of a single Zip file-entry. A call to the
	 * {@link InputStream#read(byte[], int, int)} basically calls through to
	 * {@link #readFileDataPart(byte[], int, int)}.
	 * 
	 * NOTE: you _must_ read from the input-stream until it returns EOF (-1).
	 * 
	 * @param raw
	 *            Set to true to have read() call thru to {@link #readRawFileDataPart(byte[], int, int)} or false to
	 *            have it call thru to {@link #readFileDataPart(byte[], int, int)}.
	 * @return Stream that can be used to read the file bytes. Calling close() on this stream is a no-op.
	 */
	public InputStream openFileDataInputStream(boolean raw) {
		if (fileDataInputStream == null || fileDataInputStream.raw != raw) {
			fileDataInputStream = new ZipFileDataInputStream(raw);
		}
		return fileDataInputStream;
	}

	/**
	 * Read file data from the Zip stream and decode it into the buffer argument. See
	 * {@link #readFileDataPart(byte[], int, int)} for more details.
	 * 
	 * @return The number of bytes written into the buffer or -1 if the end of zipped bytes for this file have been
	 *         reached. This doesn't mean that the end of the file has been reached.
	 */
	public int readFileDataPart(byte[] buffer) throws IOException {
		return readFileDataPart(buffer, 0, buffer.length);
	}

	/**
	 * Read raw file data from the Zip stream, decode it, and write it to the file path argument.
	 * 
	 * @param outputPath
	 *            Where to write the data read from the zip stream.
	 * @return THe number of bytes written into the output-stream.
	 */
	public long readRawFileDataToFile(String outputPath) throws IOException {
		return readRawFileDataToFile(new File(outputPath));
	}

	/**
	 * Read raw file data from the Zip stream, decode it, and write it to the file argument. This will associate the
	 * File with the current header file-name so you can call assign the permissions for the file with a later call to
	 * {@link #assignDirectoryFileEntryPermissions(ZipCentralDirectoryFileEntry)} or
	 * {@link #readDirectoryFileEntriesAndAssignPermissions()}.
	 * 
	 * @param outputFile
	 *            Where to write the data read from the zip stream.
	 * @return THe number of bytes written into the output-stream.
	 */
	public long readRawFileDataToFile(File outputFile) throws IOException {
		try(OutputStream os = FileUtils.getOutputStream(outputFile)) {
			long numBytes = readRawFileData(os);
			if (outputFileMap == null) {
				outputFileMap = new HashMap<>();
			}
			outputFileMap.put(currentFileHeader.getFileName(), outputFile);
			return numBytes;
		}
	}

	/**
	 * Read raw file data from the Zip stream, without decoding, into the buffer argument. See
	 * {@link #readRawFileDataPart(byte[], int, int)} for more details.
	 * 
	 * @return The number of bytes written into the buffer or -1 if the end of zipped bytes for this file have been
	 *         reached. This doesn't mean that the end of the file has been reached.
	 */
	public int readRawFileDataPart(byte[] buffer) throws IOException {
		return readRawFileDataPart(buffer, 0, buffer.length);
	}

	/**
	 * Read file data from the Zip stream and decode it into the buffer argument.
	 * 
	 * NOTE: This _must_ be called until it returns -1 which indicates that EOF has been reached. Until the underlying
	 * decoders return EOF we don't know that we are done and we can't rewind over any pre-fetched bytes to continue to
	 * process the next file or the directory at the end of the Zip file.
	 * 
	 * @return The number of bytes written into the buffer or -1 if the end of zipped bytes for this file have been
	 *         reached. This doesn't mean that the end of the file has been reached.
	 */
	public int readFileDataPart(byte[] buffer, int offset, int length) throws IOException {
		if (currentFileHeader == null) {
			throw new IllegalStateException("Need to call readFileHeader() before you can read file data");
		}
		return doReadFileDataPart(buffer, offset, length, currentFileHeader.getCompressionMethod());
	}

	/**
	 * Read raw file data from the Zip stream, without decoding, into the buffer argument. This will only work if the
	 * compressed-size value is set in the file-header.
	 * 
	 * NOTE: This _must_ be called until it returns -1 which indicates that EOF has been reached. Until the underlying
	 * decoders return EOF we don't know that we are done and we can't rewind over any pre-fetched bytes to continue to
	 * process the next file or the directory at the end of the Zip file.
	 * 
	 * @return The number of bytes written into the buffer or -1 if the end of zipped bytes for this file have been
	 *         reached. This doesn't mean that the end of the file has been reached.
	 */
	public int readRawFileDataPart(byte[] buffer, int offset, int length) throws IOException {
		if (currentFileHeader == null) {
			throw new IllegalStateException("Need to call readNextHeader() before you can read file data");
		}
		return doReadFileDataPart(buffer, offset, length, CompressionMethod.NONE.getValue());
	}

	/**
	 * After all of the files have been read, you can read and examine the central-directory entries.
	 * 
	 * @return The next central-directory file-header or null if all entries have been read.
	 */
	public ZipCentralDirectoryFileEntry readDirectoryFileEntry() throws IOException {
		ZipCentralDirectoryFileEntry entry = ZipCentralDirectoryFileEntry.read(inputStream);
		return entry;
	}

	/**
	 * Return an iterator that can be used to step across the central-directory file entries. The iterator will return
	 * false for {@link Iterator#hasNext()} and null for {@link Iterator#next()} once the end has been reached.
	 */
	public Iterator<ZipCentralDirectoryFileEntry> directoryFileEntryIterator() {
		return new DirectoryFileEntryIterator();
	}

	/**
	 * Assigns the file permissions from the current dir-entry to the File that was previously read by
	 * {@link #readFileDataToFile(File)}. A previous call to {@link #readDirectoryFileEntry()} must have been made with
	 * a file-name that matches the file-header written with the File previously. This assigns the permissions based on
	 * a call to {@link ExternalFileAttributesUtils#assignToFile(File, int)}.
	 * 
	 * @return True if successful otherwise false if the file was not found.
	 */
	public boolean assignDirectoryFileEntryPermissions(ZipCentralDirectoryFileEntry entry) {
		if (entry.getFileName() == null) {
			return false;
		}
		File file = outputFileMap.get(entry.getFileName());
		if (file == null) {
			return false;
		} else {
			ExternalFileAttributesUtils.assignToFile(file, entry.getExternalFileAttributes());
			return true;
		}
	}

	/**
	 * Reads in all of the file-entries from the Zip central-directory and assigns the permissions on the files that
	 * were previously read by {@link #readFileDataToFile(File)} and that matches the file-header written with the File.
	 * 
	 * @return True if successful or false if any of the files were not found.
	 */
	public boolean readDirectoryFileEntriesAndAssignPermissions() throws IOException {
		boolean result = true;
		while (true) {
			ZipCentralDirectoryFileEntry entry = ZipCentralDirectoryFileEntry.read(inputStream);
			if (entry == null) {
				break;
			}
			if (!assignDirectoryFileEntryPermissions(entry)) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * Read the central-directory end which is after all of the central-directory file-headers at the very end of the
	 * Zip file.
	 * 
	 * @return The zip64 end or null if there is none.
	 */
	public Zip64CentralDirectoryEnd readZip64DirectoryEnd() throws IOException {
		return Zip64CentralDirectoryEnd.read(inputStream);
	}

	/**
	 * Read the central-directory end locator which is after the zip64 end structure.
	 * 
	 * @return The zip64 locator or null if there is none.
	 */
	public Zip64CentralDirectoryEndLocator readZip64DirectoryEndLocator() throws IOException {
		return Zip64CentralDirectoryEndLocator.read(inputStream);
	}

	/**
	 * Read the central-directory end which is after all of the central-directory file-headers at the very end of the
	 * Zip file.
	 */
	public ZipCentralDirectoryEnd readDirectoryEnd() throws IOException {
		return ZipCentralDirectoryEnd.read(inputStream);
	}

	/**
	 * In some circumstances we need to read to the EOF marker in case we are in an inner Zip file. The outer decoder
	 * might need to hit the EOF so it can appropriately rewind in case it was reading ahead which happens when we are
	 * decoding file data or looking for data block magic numbers. This method will be called by {@link #close()} if
	 * {@link #setReadTillEof(boolean)} is set to true which is on by default if the {@link #ZipFileInput(InputStream)}
	 * constructor is used.
	 */
	public void readToEndOfZip() throws IOException {
		while (true) {
			int num = inputStream.read(tmpBuffer);
			if (num < 0) {
				break;
			}
			// we do nothing with the read information
		}
	}

	/**
	 * Close the underlying input-stream.
	 * 
	 * NOTE: this will read to the end of the Zip-file if the read-till-eof flag is set to true. See
	 * {@link #setReadTillEof(boolean)}.
	 */
	@Override
	public void close() throws IOException {
		if (readTillEof) {
			readToEndOfZip();
		}
		inputStream.close();
	}

	/**
	 * Return the file-name from the most recent header read or null if none.
	 */
	public String getCurrentFileName() {
		if (currentFileHeader == null) {
			return null;
		} else {
			return currentFileHeader.getFileName();
		}
	}

	/**
	 * Return some counting and CRC information from the current file that was read.
	 */
	public ZipFileDataInfo getCurrentFileCountingInfo() {
		return fileDataCountingInfo;
	}

	/**
	 * Return the number of bytes that have been read so far in the stream.
	 */
	public long getNumBytesRead() {
		return inputStream.getByteCount();
	}

	/**
	 * Returns true if the current file's data EOF has been reached. read() should have returned -1.
	 */
	public boolean isFileDataEofReached() {
		return currentFileEofReached;
	}

	/**
	 * After all of the Zip data has been read from the stream there may be an optional data-descriptor depending on
	 * whether or not the file-header has the {@link GeneralPurposeFlag#DATA_DESCRIPTOR} flag set. If there is no
	 * descriptor then null is returned here. Once the next header is read this will return null until the end of the
	 * Zip data again has been reached by the next file entry.
	 */
	public ZipDataDescriptor getCurrentDataDescriptor() {
		return currentDataDescriptor;
	}

	/**
	 * By default the reader will read to the end of the zip-file when {@link #close()} is called if we are reading from
	 * an input-stream to handle the possibility of being a Zip within a Zip. If we don't do this the encoding of the
	 * outer Zip file may not be fully read to the end which would cause processing issues.
	 */
	public void setReadTillEof(boolean readTillEof) {
		this.readTillEof = readTillEof;
	}

	private int doReadFileDataPart(byte[] buffer, int offset, int length, int compressionMethod) throws IOException {
		if (currentFileEofReached) {
			return -1;
		}
		if (length == 0) {
			return 0;
		}

		if (fileDataDecoder == null) {
			assignFileDataDecoder(compressionMethod);
		}

		// read in bytes from our decoder
		int result = fileDataDecoder.decode(buffer, offset, length);
		if (result >= 0) {
			// update our counts for the length and crc information
			fileDataCountingInfo.update(buffer, offset, result);
		} else {
			closeFileData();
		}
		return result;
	}

	/**
	 * Close a specific file-data portion.
	 */
	private void closeFileData() throws IOException {
		fileDataDecoder.close();
		long compressedSize = fileDataDecoder.getBytesRead();
		long uncompressedSize = fileDataDecoder.getBytesWritten();
		if (currentFileHeader.hasFlag(GeneralPurposeFlag.DATA_DESCRIPTOR)) {
			currentDataDescriptor = ZipDataDescriptor.read(inputStream, compressedSize, uncompressedSize);
		}
		currentFileEofReached = true;
		fileDataDecoder = null;
	}

	private void assignFileDataDecoder(int compressionMethod) throws IOException {
		if (compressionMethod == CompressionMethod.NONE.getValue()) {
			this.fileDataDecoder = new StoredFileDataDecoder(inputStream, currentFileHeader.getCompressedSize());
		} else if (compressionMethod == CompressionMethod.DEFLATED.getValue()) {
			this.fileDataDecoder = new InflatorFileDataDecoder(inputStream);
		} else if (compressionMethod == CompressionMethod.SIMPLEZIP.getValue()) {
			this.fileDataDecoder = new SimpleZipFileDataDecoder(inputStream);
		} else {
			throw new IllegalStateException("Unknown compression method: "
					+ CompressionMethod.fromValue(compressionMethod) + " (" + compressionMethod + ")");
		}
	}

	/**
	 * Input stream that can be used to read data for a single Zip file-entry.
	 */
	public class ZipFileDataInputStream extends InputStream {

		private final byte[] singleByteBuffer = new byte[1];
		private final boolean raw;

		public ZipFileDataInputStream(boolean raw) {
			this.raw = raw;
		}

		@Override
		public int read() throws IOException {
			int val = read(singleByteBuffer, 0, 1);
			if (val < 0) {
				return -1;
			} else {
				return singleByteBuffer[0];
			}
		}

		@Override
		public int read(byte[] buffer, int offset, int lengeth) throws IOException {
			if (raw) {
				return readRawFileDataPart(buffer, offset, lengeth);
			} else {
				return readFileDataPart(buffer, offset, lengeth);
			}
		}

		@Override
		public void close() {
			// no-op, nothing to close
		}
	}

	/**
	 * Iterator for the file headers in the Zip file.
	 */
	private class FileHeaderIterator implements Iterator<ZipFileHeader> {

		private ZipFileHeader previousFileHeader;

		@Override
		public boolean hasNext() {
			try {
				previousFileHeader = readFileHeader();
			} catch (IOException ioe) {
				previousFileHeader = null;
				throw new RuntimeException("problems reading next file-header", ioe);
			}
			return (previousFileHeader != null);
		}

		@Override
		public ZipFileHeader next() {
			if (previousFileHeader == null) {
				if (!hasNext()) {
					return null;
				}
			}
			ZipFileHeader result = previousFileHeader;
			previousFileHeader = null;
			return result;
		}
	}

	/**
	 * Iterator for the central directory file entries in the Zip file.
	 */
	private class DirectoryFileEntryIterator implements Iterator<ZipCentralDirectoryFileEntry> {

		private ZipCentralDirectoryFileEntry previousFileEntry;

		@Override
		public boolean hasNext() {
			try {
				previousFileEntry = readDirectoryFileEntry();
			} catch (IOException ioe) {
				previousFileEntry = null;
				throw new RuntimeException("problems reading next file-header", ioe);
			}
			return (previousFileEntry != null);
		}

		@Override
		public ZipCentralDirectoryFileEntry next() {
			if (previousFileEntry == null) {
				if (!hasNext()) {
					return null;
				}
			}
			ZipCentralDirectoryFileEntry result = previousFileEntry;
			previousFileEntry = null;
			return result;
		}
	}
}
