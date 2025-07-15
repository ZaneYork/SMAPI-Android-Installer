/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.common;

import com.abdurazaaqmohammed.AntiSplit.main.LegacyUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;


public class FileChannelInputStream extends InputStream {
    private final FileChannel fileChannel;
    private final long totalLength;
    private long startOffset;
    private long position;
    private final byte[] buffer;
    private int bufferPosition;
    private int bufferLength;
    private boolean mAutoClosable;
    private boolean mIsClosed;

    public FileChannelInputStream(FileChannel fileChannel, byte[] buffer, long length) throws IOException {
        this.fileChannel = fileChannel;
        this.totalLength = length;
        int bufferSize = buffer.length;
        this.buffer = buffer;
        this.bufferLength = bufferSize;
        this.bufferPosition = bufferSize;
        this.startOffset = fileChannel.position();
    }
    public FileChannelInputStream(FileChannel fileChannel, long length, int bufferSize) throws IOException {
        this.fileChannel = fileChannel;
        this.totalLength = length;
        if(bufferSize <= 0){
            bufferSize = 8;
        }
        if(length < bufferSize){
            bufferSize = (int) length;
        }
        this.buffer = new byte[bufferSize];
        this.bufferLength = bufferSize;
        this.bufferPosition = bufferSize;
        this.startOffset = fileChannel.position();
    }
    public FileChannelInputStream(FileChannel fileChannel, long length) throws IOException {
        this(fileChannel, length, DEFAULT_BUFFER_SIZE);
    }
    public FileChannelInputStream(File file, long length, int bufferSize) throws IOException {
        this(LegacyUtils.supportsFileChannel ?
                FileChannel.open(file.toPath(), StandardOpenOption.READ) : new RandomAccessFile(file, "r").getChannel(), length, bufferSize);
        this.mAutoClosable = true;
    }
    public FileChannelInputStream(File file, byte[] buffer, int bufferSize) throws IOException {
        this(LegacyUtils.supportsFileChannel ?
                FileChannel.open(file.toPath(), StandardOpenOption.READ) : new RandomAccessFile(file, "r").getChannel(), buffer, bufferSize);
        this.mAutoClosable = true;
    }
    public FileChannelInputStream(File file) throws IOException {
        this(LegacyUtils.supportsFileChannel ?
                FileChannel.open(file.toPath(), StandardOpenOption.READ) : new RandomAccessFile(file, "r").getChannel(), file.length());
        this.mAutoClosable = true;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }
    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        if(isFinished()){
            return -1;
        }
        if(length==0){
            return 0;
        }
        loadBuffer();
        int result = 0;
        int read = readBuffer(bytes, offset, length);
        result += read;
        length = length - read;
        offset = offset + read;
        while (length>0 && !isFinished()){
            loadBuffer();
            read = readBuffer(bytes, offset, length);
            result += read;
            length = length - read;
            offset = offset + read;
        }
        return result;
    }
    private int readBuffer(byte[] bytes, int offset, int length){
        int avail = bufferLength - bufferPosition;
        if(avail == 0){
            return 0;
        }
        int read = length;
        if(read > avail){
            read = avail;
        }
        System.arraycopy(buffer, bufferPosition, bytes, offset, read);
        bufferPosition += read;
        position += read;
        return read;
    }
    private void loadBuffer() throws IOException {
        byte[] buffer = this.buffer;
        if(this.bufferPosition < bufferLength){
            return;
        }
        int length = buffer.length;
        long available = totalLength - position;
        boolean is_last = false;
        if(length > available){
            length = (int) available;
            is_last = true;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, length);
        bufferLength = fileChannel.read(byteBuffer);
        bufferPosition = 0;
        if(is_last){
            closeAuto();
        }
    }
    private boolean isFinished() throws IOException {
        boolean finished = position >= totalLength;
        if(finished){
            closeAuto();
        }
        return finished;
    }
    @Override
    public int read() throws IOException {
        byte[] bytes = new byte[1];
        int read = read(bytes);
        if(read < 0){
            return read;
        }
        return bytes[0] & 0xff;
    }
    public long transferTo(OutputStream out) throws IOException{
        long transferred = 0;
        if(isFinished()){
            return transferred;
        }
        while (!isFinished()){
            loadBuffer();
            int offset = bufferPosition;
            int length = bufferLength - bufferPosition;
            if(length <= 0){
                break;
            }
            out.write(buffer, offset, length);
            bufferPosition += length;
            position += length;
            transferred += length;
        }
        return transferred;
    }
    @Override
    public long skip(long amount) throws IOException {
        if(amount <= 0){
            return amount;
        }
        long remaining = amount;
        remaining = remaining - skipBuffer(remaining);
        if(remaining == 0){
            return amount;
        }
        long availableChannel = totalLength - position;
        if(availableChannel > remaining){
            availableChannel = remaining;
        }
        position += availableChannel;
        remaining = remaining - availableChannel;
        amount = amount - remaining;
        fileChannel.position(fileChannel.position() + availableChannel);
        return amount;
    }
    private int skipBuffer(long amount){
        int availableBuffer = bufferLength - bufferPosition;
        if(availableBuffer > amount){
            availableBuffer = (int) amount;
        }
        bufferPosition += availableBuffer;
        position += availableBuffer;
        return availableBuffer;
    }
    public FileChannel getFileChannel() {
        return fileChannel;
    }

    public void setAutoClosable(boolean autoClosable) {
        this.mAutoClosable = autoClosable;
    }
    private void closeAuto() throws IOException {
        if(mAutoClosable && !mIsClosed){
            mIsClosed = true;
            fileChannel.close();
        }
    }

    @Override
    public void close() throws IOException {
        closeAuto();
    }
    @Override
    public void reset() throws IOException {
        position = 0;
        bufferPosition = bufferLength;
        fileChannel.position(startOffset);
    }
    @Override
    public int available(){
        return (int) (totalLength - position);
    }
    @Override
    public boolean markSupported() {
        return true;
    }
    @Override
    public synchronized void mark(int readLimit){
        if(readLimit < 0){
            readLimit = 0;
        }
        startOffset = readLimit;
    }
    @Override
    public String toString(){
        return position + " / " + totalLength;
    }

    public static byte[] read(File file, int length) throws IOException{
        FileChannelInputStream inputStream = new FileChannelInputStream(file,length, length);
        inputStream.loadBuffer();
        inputStream.closeAuto();
        return inputStream.buffer;
    }
    public static void read(File file, byte[] buffer, int length) throws IOException{
        FileChannelInputStream inputStream = new FileChannelInputStream(file, buffer, length);
        inputStream.loadBuffer();
        inputStream.closeAuto();
    }

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 100;
}
