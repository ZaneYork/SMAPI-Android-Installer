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
package com.reandroid.archive.writer;

import com.reandroid.apk.APKLogger;
import com.reandroid.archive.Archive;
import com.reandroid.archive.InputSource;
import com.reandroid.archive.ZipSignature;
import com.reandroid.archive.block.CentralEntryHeader;
import com.reandroid.archive.block.DataDescriptor;
import com.reandroid.archive.block.LocalFileHeader;
import com.reandroid.archive.io.CountingOutputStream;
import com.reandroid.archive.io.ZipOutput;
import com.reandroid.utils.io.FileUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

class OutputSource {
    private final InputSource inputSource;
    private LocalFileHeader lfh;
    private APKLogger apkLogger;
    private HeaderInterceptor headerInterceptor;

    OutputSource(InputSource inputSource){
        this.inputSource = inputSource;
    }

    void writeBuffer(ZipOutput zipOutput) throws IOException {
        LocalFileHeader lfh = getLocalFileHeader();
        InputSource inputSource = getInputSource();
        OutputStream rawStream = zipOutput.getOutputStream();
        CountingOutputStream<OutputStream> rawCounter = new CountingOutputStream<>(rawStream);
        CountingOutputStream<DeflaterOutputStream> deflateCounter = null;

        if(inputSource.getMethod() != Archive.STORED){
            DeflaterOutputStream deflaterInputStream = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) ?
                    new DeflaterOutputStream(rawCounter, new Deflater(Deflater.BEST_SPEED, true), true)
                    :  new DeflaterOutputStream(rawCounter, new Deflater(Deflater.BEST_SPEED, true));
            deflateCounter = new CountingOutputStream<>(deflaterInputStream, false);
        }
        if(deflateCounter != null){
            rawCounter.disableCrc(true);
            inputSource.write(deflateCounter);
            deflateCounter.close();
            rawCounter.close();
        }else {
            inputSource.write(rawCounter);
        }

        lfh.setCompressedSize(rawCounter.getSize());

        if(deflateCounter != null){
            lfh.setMethod(Archive.DEFLATED);
            lfh.setCrc(deflateCounter.getCrc());
            lfh.setSize(deflateCounter.getSize());
        }else {
            lfh.setSize(rawCounter.getSize());
            lfh.setMethod(Archive.STORED);
            lfh.setCrc(rawCounter.getCrc());
        }
        inputSource.disposeInputSource();
    }
    void writeCEH(ZipOutput zipOutput) throws IOException{
        LocalFileHeader lfh = getLocalFileHeader();
        CentralEntryHeader ceh = CentralEntryHeader.fromLocalFileHeader(lfh);
        notifyCEHWrite(ceh);
        ceh.writeBytes(zipOutput.getOutputStream());
    }
    void writeDD(ZipOutput apkFileWriter) throws IOException{
        DataDescriptor dataDescriptor = getLocalFileHeader().getDataDescriptor();
        if(dataDescriptor == null){
            return;
        }
        notifyDDWrite(dataDescriptor);
        dataDescriptor.writeBytes(apkFileWriter.getOutputStream());
    }
    void writeLFH(ZipOutput zipOutput, ZipAligner zipAligner) throws IOException {
        LocalFileHeader lfh = getLocalFileHeader();
        if(zipAligner != null){
            zipAligner.align(zipOutput.position(), lfh);
        }
        notifyLFHWrite(lfh);
        lfh.writeBytes(zipOutput.getOutputStream());
    }

    public void setHeaderInterceptor(HeaderInterceptor interceptor) {
        this.headerInterceptor = interceptor;
    }
    private void notifyLFHWrite(LocalFileHeader lfh){
        HeaderInterceptor interceptor = this.headerInterceptor;
        if(interceptor != null){
            interceptor.onWriteLfh(lfh);
        }
    }
    private void notifyCEHWrite(CentralEntryHeader ceh){
        HeaderInterceptor interceptor = this.headerInterceptor;
        if(interceptor != null){
            interceptor.onWriteCeh(ceh);
        }
    }
    private void notifyDDWrite(DataDescriptor dataDescriptor){
        HeaderInterceptor interceptor = this.headerInterceptor;
        if(interceptor != null){
            interceptor.onWriteDD(dataDescriptor);
        }
    }
    InputSource getInputSource() {
        return inputSource;
    }
    LocalFileHeader getLocalFileHeader(){
        if(lfh == null){
            LocalFileHeader lfh = createLocalFileHeader();
            lfh.setFileName(getInputSource().getAlias());
            lfh.setZipAlign(0);
            lfh.updateDataDescriptor();
            this.lfh = lfh;
        }
        return lfh;
    }
    LocalFileHeader createLocalFileHeader(){
        InputSource inputSource = getInputSource();
        LocalFileHeader lfh = new LocalFileHeader();
        lfh.setSignature(ZipSignature.LOCAL_FILE);
        lfh.getGeneralPurposeFlag().initDefault();
        lfh.setFileName(inputSource.getAlias());
        lfh.setMethod(inputSource.getMethod());
        return lfh;
    }
    void logLargeFileWrite(){
        APKLogger logger =  this.apkLogger;
        if(logger == null){
            return;
        }
        long size = getLocalFileHeader().getDataSize();
        if(size < LOG_LARGE_FILE_SIZE){
            return;
        }
        logFileWrite();
    }
    void logFileWrite(){
        APKLogger logger =  this.apkLogger;
        if(logger == null){
            return;
        }
        long size = getLocalFileHeader().getDataSize();
        logVerbose("Write [" + FileUtil.toReadableFileSize(size) + "] " +
                getInputSource().getAlias());
    }

    void setAPKLogger(APKLogger logger) {
        this.apkLogger = logger;
    }
    void logVerbose(String msg) {
        if(apkLogger!=null){
            apkLogger.logVerbose(msg);
        }
    }
    private static final long LOG_LARGE_FILE_SIZE = 2L * 1000 * 1024;
}
