package com.j256.simplezip;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.j256.simplezip.codec.DeflatorFileDataEncoder;
import com.j256.simplezip.codec.FileDataEncoder;
import com.j256.simplezip.codec.SimpleZipFileDataEncoder;
import com.j256.simplezip.codec.StoredFileDataEncoder;
import com.j256.simplezip.format.CompressionMethod;
import com.j256.simplezip.format.GeneralPurposeFlag;
import com.j256.simplezip.format.Zip64CentralDirectoryEnd;
import com.j256.simplezip.format.Zip64CentralDirectoryEndLocator;
import com.j256.simplezip.format.ZipCentralDirectoryEnd;
import com.j256.simplezip.format.ZipCentralDirectoryEndInfo;
import com.j256.simplezip.format.ZipCentralDirectoryFileEntry;
import com.j256.simplezip.format.ZipCentralDirectoryFileInfo;
import com.j256.simplezip.format.ZipDataDescriptor;
import com.j256.simplezip.format.ZipFileHeader;
import com.starry.FileUtils;

/**
 * Write out a Zip-file to either a {@link File} or an {@link OutputStream}.
 * 
 * @author graywatson
 */
public class ZipFileOutput implements Closeable {

	private final BufferedOutputStream bufferedOutputStream;
	private final ZipFileDataInfo incomingFileDateInfo = new ZipFileDataInfo();
	private final byte[] tmpBuffer = new byte[IoUtils.STANDARD_BUFFER_SIZE];
	private final List<ZipCentralDirectoryFileEntry.Builder> dirFileEntryBuilders = new ArrayList<>();
	private final Map<String, ZipCentralDirectoryFileEntry.Builder> dirFileEntryBuilderMap = new HashMap<>();

	private ZipFileHeader currentFileHeader;
	private FileDataEncoder fileDataEncoder;
	private ZipDataDescriptor.Builder dataDescriptorBuilder = ZipDataDescriptor.builder();
	private ZipCentralDirectoryFileEntry.Builder dirFileBuilder;
	private ZipFileDataOutputStream fileDataOutputStream;
	private boolean fileFinished = true;
	private boolean zipFinished;
	private long fileCount;

	/**
	 * Start writing a Zip-file to a file-path. You must call {@link #close()} to close the stream when you are done.
	 */
	public ZipFileOutput(String filePath) throws IOException {
		this(new File(filePath));
	}

	/**
	 * Start writing a Zip-file to a file. You must call {@link #close()} to close the stream when you are done.
	 */
	public ZipFileOutput(File file) throws IOException {
		this(FileUtils.getOutputStream(file));
	}

	/**
	 * Start writing a Zip-file to an output-stream. You must call {@link #close()} to close the stream when you are
	 * done.
	 */
	public ZipFileOutput(OutputStream outputStream) {
		this.bufferedOutputStream = new BufferedOutputStream(outputStream);
	}

	/**
	 * Attaches a buffered output stream to record the file-data _before_ writing out the file-header. This is useful
	 * because it means that the {@link ZipFileHeader} written before the file-data will contain the encoded size and
	 * the crc32 information as opposed to the {@link ZipDataDescriptor} which is written _after_ the file-data.
	 * 
	 * @param maxSizeBuffered
	 *            Maximum number of bytes that will be stored by the buffer before it gives up and will write out the
	 *            header and the a {@link ZipDataDescriptor} after the file-data.
	 * @param maxSizeInMemory
	 *            Maximum number of bytes that will be stored by in memory. If there is any space above this number but
	 *            below the maxSizeBuffered then it will be written to a temporary file.
	 */
	public void enableFileBuffering(int maxSizeBuffered, int maxSizeInMemory) {
		if (maxSizeBuffered < maxSizeInMemory) {
			throw new IllegalArgumentException(
					"maxSizeBuffered " + maxSizeBuffered + " should be >= maxSizeInMemory " + maxSizeInMemory);
		}
		bufferedOutputStream.enableBuffer(maxSizeBuffered, maxSizeInMemory);
	}

	/**
	 * Write a file-header which starts the Zip-file. This actually may or may not actually write it to disk depending
	 * on buffering.
	 */
	public void writeFileHeader(ZipFileHeader fileHeader) {
		if (zipFinished) {
			throw new IllegalStateException("Cannot write another file-header if the zip has been finished");
		}
		if (!fileFinished) {
			throw new IllegalStateException("Need to call finishFileData() before writing the next file-header");
		}
		bufferedOutputStream.setFileHeader(fileHeader);
		currentFileHeader = fileHeader;
		incomingFileDateInfo.reset();
		dirFileBuilder = ZipCentralDirectoryFileEntry.builder();
		dirFileBuilder.setRelativeOffsetOfLocalHeader(bufferedOutputStream.getWriteCount());
		fileFinished = false;
		fileCount++;
	}

	/**
	 * Add additional information (most optional) to the Zip central-directory file-header to be associated with the
	 * most recent file-header. This information is written to the end of the Zip-file and which holds information about
	 * the files that is not contained in the {@link ZipFileHeader}. This should be done after the
	 * {@link #writeFileHeader(ZipFileHeader)} method and before {@link #finishFileData()} have been called.
	 */
	public void addDirectoryFileInfo(ZipCentralDirectoryFileInfo fileInfo) {
		if (dirFileBuilder == null) {
			throw new IllegalStateException("Cannot add directory file-info a file header has not been written");
		}
		if (zipFinished) {
			throw new IllegalStateException("Cannot add directory file-info if the zip has been finished");
		}
		dirFileBuilder.addFileInfo(fileInfo);
	}

	/**
	 * Add additional information (most optional_ to the Zip central-directory file-header to be associated with the
	 * file-name parameter This information is written to the end of the Zip-file and which holds information about the
	 * files that is not contained in the {@link ZipFileHeader}.
	 * 
	 * @return True if this works otherwise false if the file-name is not found.
	 */
	public boolean addDirectoryFileInfo(String fileName, ZipCentralDirectoryFileInfo fileInfo) {
		if (zipFinished) {
			throw new IllegalStateException("Cannot add directory file-info if the zip has been finished");
		}
		ZipCentralDirectoryFileEntry.Builder builder = dirFileEntryBuilderMap.get(fileName);
		if (builder == null) {
			return false;
		} else {
			builder.addFileInfo(fileInfo);
			return true;
		}
	}

	/**
	 * Write the contents of a file to the Zip-file stream while encoding it based on the
	 * {@link ZipFileHeader#getCompressionMethod()}. Should be called after you write the file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long writeFileData(String filePath) throws IOException {
		return writeFileData(new File(filePath));
	}

	/**
	 * Write the contents of a file to the Zip-file stream without any encoding. Should be called after you write the
	 * file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long writeRawFileData(String filePath) throws IOException {
		return writeRawFileData(new File(filePath));
	}

	/**
	 * Write the contents of a file to the Zip-file stream while encoding it based on the
	 * {@link ZipFileHeader#getCompressionMethod()}. Should be called after you write the file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long writeFileData(File file) throws IOException {
		try (InputStream inputStream = new FileInputStream(file)) {
			return writeFileData(inputStream);
		}
	}

	/**
	 * Write the contents of a file to the Zip-file stream without any encoding. Should be called after you write the
	 * file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long writeRawFileData(File file) throws IOException {
		try (InputStream inputStream = new FileInputStream(file)) {
			return writeRawFileData(inputStream);
		}
	}

	/**
	 * Read from the input-stream and write its contents to the the Zip-file stream while encoding it based on the
	 * {@link ZipFileHeader#getCompressionMethod()}. Should be called after you write the file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long writeFileData(InputStream inputStream) throws IOException {
		while (true) {
			int numRead = inputStream.read(tmpBuffer);
			if (numRead < 0) {
				break;
			}
			writeFileDataPart(tmpBuffer, 0, numRead);
		}
		return finishFileData();
	}

	/**
	 * Read from the input-stream and write its contents to the the Zip-file stream without any encoding. Should be
	 * called after you write the file-header.
	 * 
	 * NOTE: this method calls {@link #finishFileData()} for you.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long writeRawFileData(InputStream inputStream) throws IOException {
		while (true) {
			int numRead = inputStream.read(tmpBuffer);
			if (numRead < 0) {
				break;
			}
			writeRawFileDataPart(tmpBuffer, 0, numRead);
		}
		return finishFileData();
	}

	/**
	 * Get an output stream suitable for writing the bytes for a single Zip file-entry. A call to the
	 * {@link OutputStream#write(byte[], int, int)} basically calls through to
	 * {@link #writeFileDataPart(byte[], int, int)}. A call to {@link OutputStream#close()} calls through to
	 * {@link #finishFileData()}.
	 * 
	 * @param raw
	 *            Set to true to have write() call thru to {@link #writeRawFileDataPart(byte[], int, int)} or false to
	 *            have it call thru tol {@link #writeFileDataPart(byte[], int, int)}.
	 */
	public OutputStream openFileDataOutputStream(boolean raw) {
		if (fileDataOutputStream == null) {
			fileDataOutputStream = new ZipFileDataOutputStream(raw);
		}
		return fileDataOutputStream;
	}

	/**
	 * Write the complete file data as single byte array to the the Zip-file stream while encoding it based on the
	 * {@link ZipFileHeader#getCompressionMethod()}. Should be called after you write the file-header. This method calls
	 * {@link #finishFileData()} after the write.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long writeFileDataAll(byte[] buffer) throws IOException {
		writeFileDataPart(buffer, 0, buffer.length);
		return finishFileData();
	}

	/**
	 * Write file data as single or multiple byte arrays to the the Zip-file stream while encoding it based on the
	 * {@link ZipFileHeader#getCompressionMethod()}. Should be called after you write the file-header. At the end of the
	 * writing, you must call {@link #finishFileData()}.
	 */
	public void writeFileDataPart(byte[] buffer) throws IOException {
		writeFileDataPart(buffer, 0, buffer.length);
	}

	/**
	 * Write file data as single or multiple byte arrays to the the Zip-file stream without any encoding. Should be
	 * called after you write the file-header. At the end of the writing, you must call {@link #finishFileData()}.
	 */
	public void writeRawFileDataPart(byte[] buffer) throws IOException {
		writeRawFileDataPart(buffer, 0, buffer.length);
	}

	/**
	 * Write file data as single or multiple byte arrays to the the Zip-file stream while encoding it based on the
	 * {@link ZipFileHeader#getCompressionMethod()}. Should be called after you write the file-header. At the end of the
	 * writing, you must call {@link #finishFileData()}.
	 */
	public void writeFileDataPart(byte[] buffer, int offset, int length) throws IOException {
		if (currentFileHeader == null) {
			throw new IllegalStateException("Need to call writeFileHeader() before you can write file data");
		}
		doWriteFileDataPart(buffer, offset, length, currentFileHeader.getCompressionMethod());
	}

	/**
	 * Write file data as single or multiple byte arrays to the the Zip-file stream without any encoding. Should be
	 * called after you write the file-header. At the end of the writing, you must call {@link #finishFileData()}.
	 */
	public void writeRawFileDataPart(byte[] buffer, int offset, int length) throws IOException {
		if (currentFileHeader == null) {
			throw new IllegalStateException("Need to call writeFileHeader() before you can write file data");
		}
		doWriteFileDataPart(buffer, offset, length, CompressionMethod.NONE.getValue());
	}

	/**
	 * Called at the end of the file data. Must be called after the {@link #writeFileDataPart(byte[])} or
	 * {@link #writeFileDataPart(byte[], int, int)} methods have been called.
	 * 
	 * NOTE: This will write a {@link ZipDataDescriptor} at the end of the file unless a CRC32, compressed, or
	 * uncompressed size was specified in the header.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long finishFileData() throws IOException {
		if (zipFinished) {
			throw new IllegalStateException("Cannot finish file-data if the zip has been finished");
		}
		if (fileDataEncoder == null) {
			// we need to open the encoder if no data has been sent
			assignFileDataEncoder(currentFileHeader.getCompressionMethod());
		}
		fileDataEncoder.close();
		ZipFileHeader writtenFileHeader;
		if (currentFileHeader.getCrc32() != 0 && currentFileHeader.getUncompressedSize() != 0) {
			writtenFileHeader = bufferedOutputStream.finishFileData(currentFileHeader.getCrc32(),
					currentFileHeader.getUncompressedSize());
		} else {
			// calculate the crc and size from the incoming file data
			writtenFileHeader = bufferedOutputStream.finishFileData(incomingFileDateInfo.getCrc32(),
					incomingFileDateInfo.getByteCount());
		}
		if (writtenFileHeader == null) {
			writtenFileHeader = currentFileHeader;
		}
		// set our directory file-header info
		dirFileBuilder.setFileHeader(writtenFileHeader);
		if (writtenFileHeader != currentFileHeader) {
			dirFileBuilder.assignGeneralPurposeFlag(GeneralPurposeFlag.DATA_DESCRIPTOR, false);
			dirFileBuilder.setCompressedSize(bufferedOutputStream.getEncodedSize());
		}
		if (currentFileHeader.getCrc32() == 0 || currentFileHeader.getUncompressedSize() == 0) {
			// calculate the crc and size from the incoming file data
			dirFileBuilder.setUncompressedSize(incomingFileDateInfo.getByteCount());
			dirFileBuilder.setCrc32(incomingFileDateInfo.getCrc32());
		}
		// set our optional data-descriptor info
		if (writtenFileHeader.needsDataDescriptor()) {
			dataDescriptorBuilder.reset();
			dataDescriptorBuilder.setCompressedSize(bufferedOutputStream.getEncodedSize());
			dataDescriptorBuilder.setUncompressedSize(incomingFileDateInfo.getByteCount());
			dataDescriptorBuilder.setCrc32(incomingFileDateInfo.getCrc32());
			ZipDataDescriptor dataDescriptor = dataDescriptorBuilder.build();
			dataDescriptor.write(bufferedOutputStream);
		}
		dirFileEntryBuilders.add(dirFileBuilder);
		String fileName = dirFileBuilder.getFileName();
		if (fileName != null) {
			dirFileEntryBuilderMap.put(fileName, dirFileBuilder);
		}
		fileDataEncoder = null;
		fileFinished = true;
		return bufferedOutputStream.getWriteCount();
	}

	/**
	 * Finish writing the zip-file. See the {@link #finishZip(ZipCentralDirectoryEndInfo)} for more information.
	 * 
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long finishZip() throws IOException {
		return finishZip(null);
	}

	/**
	 * Finish writing the Zip-file with a comment. This will write out the central-directory file-headers at the end of
	 * the Zip-file that have been accumulated from the {@link #writeFileHeader(ZipFileHeader)} and
	 * {@link #addDirectoryFileInfo(ZipCentralDirectoryFileInfo)} methods. After the file-headers the directory-end
	 * information will be written either in Zip32 or Zip64 mode and lastly, if the end-info is in Zip64 mode, the Zip64
	 * locator.
	 *
	 * @return Returns the number of bytes written to the stream so far.
	 */
	public long finishZip(ZipCentralDirectoryEndInfo endInfo) throws IOException {

		if (zipFinished) {
			return bufferedOutputStream.getWriteCount();
		}
		if (!fileFinished) {
			finishFileData();
		}

		// start our directory end
		ZipCentralDirectoryEnd.Builder dirEndBuilder;
		if (endInfo == null) {
			dirEndBuilder = ZipCentralDirectoryEnd.builder();
		} else {
			dirEndBuilder = ZipCentralDirectoryEnd.Builder.fromEndInfo(endInfo);
		}
		long dirOffset = bufferedOutputStream.getWriteCount();
		dirEndBuilder.setDirectoryOffset(dirOffset);

		// write out our recorded central-directory file-headers
		for (ZipCentralDirectoryFileEntry.Builder dirEntryBuilder : dirFileEntryBuilders) {
			ZipCentralDirectoryFileEntry dirEntry = dirEntryBuilder.build();
			dirEntry.write(bufferedOutputStream);
		}

		// build our directory end but don't write it yet
		if (fileCount >= IoUtils.MAX_UNSIGNED_SHORT_VALUE) {
			dirEndBuilder.setNumRecordsOnDisk(IoUtils.MAX_UNSIGNED_SHORT_VALUE);
			dirEndBuilder.setNumRecordsTotal(IoUtils.MAX_UNSIGNED_SHORT_VALUE);
		} else {
			dirEndBuilder.setNumRecordsOnDisk((int) fileCount);
			dirEndBuilder.setNumRecordsTotal((int) fileCount);
		}
		long dirSize = (bufferedOutputStream.getWriteCount() - dirOffset);
		dirEndBuilder.setDirectorySize(dirSize);
		dirEndBuilder.setDirectoryOffset(dirOffset);

		// if the end block has zip64 values (0xFFFF or 0xFFFFFFFF) or the end-info was set to zip64
		if (dirEndBuilder.hasZip64Values() || (endInfo != null && endInfo.isNeedsZip64())) {
			finishZip64(endInfo, dirOffset, dirSize);
			// we need to recalculate the directory size now that the zip64 stuff has been written
			dirSize = (bufferedOutputStream.getWriteCount() - dirOffset);
			dirEndBuilder.setDirectorySize(dirSize);
		}

		// finally we write the standard end marker
		ZipCentralDirectoryEnd end = dirEndBuilder.build();
		end.write(bufferedOutputStream);

		zipFinished = true;
		return bufferedOutputStream.getWriteCount();
	}

	/**
	 * Return the current number of bytes written to the output zip-file stream.
	 */
	public long getNumBytesWritten() {
		return bufferedOutputStream.getWriteCount();
	}

	/**
	 * Flush the associated output stream.
	 */
	public void flush() throws IOException {
		bufferedOutputStream.flush();
	}

	/**
	 * Close the associated output stream. If {@link #finishZip()} has not been called then it will be called at this
	 * point.
	 */
	@Override
	public void close() throws IOException {
		if (!zipFinished) {
			finishZip(null);
		}
		bufferedOutputStream.close();
	}

	/**
	 * Write the Zip64 end structures which are right before the end block.
	 */
	private void finishZip64(ZipCentralDirectoryEndInfo endInfo, long dirOffset, long dirSize) throws IOException {
		Zip64CentralDirectoryEnd.Builder zip64EndBuilder;
		if (endInfo == null) {
			zip64EndBuilder = Zip64CentralDirectoryEnd.builder();
		} else {
			zip64EndBuilder = Zip64CentralDirectoryEnd.Builder.fromEndInfo(endInfo);
		}
		zip64EndBuilder.setNumRecordsOnDisk(fileCount);
		zip64EndBuilder.setNumRecordsTotal(fileCount);
		zip64EndBuilder.setDirectorySize(dirSize);
		zip64EndBuilder.setDirectoryOffset(dirOffset);
		long zip64EndOffset = bufferedOutputStream.getWriteCount();
		zip64EndBuilder.build().write(bufferedOutputStream);

		Zip64CentralDirectoryEndLocator.Builder dirEndLocatorBuilder;
		if (endInfo == null) {
			dirEndLocatorBuilder = Zip64CentralDirectoryEndLocator.builder();
		} else {
			dirEndLocatorBuilder = Zip64CentralDirectoryEndLocator.Builder.fromEndInfo(endInfo);
		}
		dirEndLocatorBuilder.setEndOffset(zip64EndOffset);
		dirEndLocatorBuilder.build().write(bufferedOutputStream);
	}

	private void doWriteFileDataPart(byte[] buffer, int offset, int length, int compressionMethod) throws IOException {
		if (zipFinished) {
			// might not be able to get here but let's be careful out there
			throw new IllegalStateException("Cannot write file-data if the zip has been finished");
		}
		if (fileDataEncoder == null) {
			assignFileDataEncoder(compressionMethod);
		}

		incomingFileDateInfo.update(buffer, offset, length);
		fileDataEncoder.encode(buffer, offset, length);
	}

	private void assignFileDataEncoder(int compressionMethod) {
		if (compressionMethod == CompressionMethod.NONE.getValue()) {
			this.fileDataEncoder = new StoredFileDataEncoder(bufferedOutputStream);
		} else if (compressionMethod == CompressionMethod.DEFLATED.getValue()) {
			this.fileDataEncoder =
					new DeflatorFileDataEncoder(bufferedOutputStream, currentFileHeader.getCompressionLevel());
		} else if (compressionMethod == CompressionMethod.SIMPLEZIP.getValue()) {
			this.fileDataEncoder = new SimpleZipFileDataEncoder(bufferedOutputStream);
		} else {
			throw new IllegalStateException("Unknown compression method: "
					+ CompressionMethod.fromValue(compressionMethod) + " (" + compressionMethod + ")");
		}
	}

	/**
	 * Output stream that can be used to write data for a single zip file. Once you close this output-stream then
	 * {@link ZipFileOutput#finishFileData()} will be automatically called.
	 */
	public class ZipFileDataOutputStream extends OutputStream {

		private final byte[] singleByteBuffer = new byte[1];
		private final boolean raw;

		public ZipFileDataOutputStream(boolean raw) {
			this.raw = raw;
		}

		@Override
		public void write(int b) throws IOException {
			singleByteBuffer[0] = (byte) b;
			write(singleByteBuffer, 0, 1);
		}

		@Override
		public void write(byte[] buffer, int offset, int length) throws IOException {
			if (raw) {
				writeRawFileDataPart(buffer, offset, length);
			} else {
				writeFileDataPart(buffer, offset, length);
			}
		}

		@Override
		public void flush() throws IOException {
			ZipFileOutput.this.flush();
		}

		@Override
		public void close() throws IOException {
			finishFileData();
			// NOTE: should not call ZipFileWriter.this.close() because we will continue to write to the zip stream
		}
	}
}
