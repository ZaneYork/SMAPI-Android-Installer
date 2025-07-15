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

import com.reandroid.archive.InputSource;
import com.reandroid.archive.RenamedInputSource;
import com.reandroid.archive.io.ArchiveFileEntrySource;
import com.reandroid.archive.io.ZipFileOutput;
import com.reandroid.arsc.chunk.TableBlock;

import java.io.File;
import java.io.IOException;

public class ApkFileWriter extends ApkWriter<ZipFileOutput, FileOutputSource> {
    private BufferFileInput buffer;
    public ApkFileWriter(File file, InputSource[] sources) throws IOException {
        super(new ZipFileOutput(file), sources);
    }
    @Override
    void closeBuffer() throws IOException{
        buffer.close();
    }
    @Override
    void writeApk(FileOutputSource outputSource, ZipAligner zipAligner) throws IOException{
        outputSource.writeApk(getZipOutput(), zipAligner);
    }
    @Override
    void prepareOutputs(FileOutputSource[] outList) throws IOException {
        logMessage("Buffering compress changed files ...");
        BufferFileInput buffer = writeBuffer(outList);
        buffer.unlock();
        this.buffer = buffer;
    }
    @Override
    FileOutputSource[] createOutArray(int length){
        return new FileOutputSource[length];
    }
    @Override
    FileOutputSource toOutputSource(InputSource inputSource){
        if(inputSource instanceof ArchiveFileEntrySource){
            return new ArchiveOutputSource(inputSource);
        }
        if(inputSource instanceof RenamedInputSource){
            RenamedInputSource<?> renamedInputSource = ((RenamedInputSource<?>) inputSource);
            if(renamedInputSource.getParentInputSource(ArchiveFileEntrySource.class) != null){
                return new RenamedArchiveSource(renamedInputSource);
            }
        }
        return new FileOutputSource(inputSource);
    }

    private BufferFileInput writeBuffer(FileOutputSource[] outputList) throws IOException {
        File bufferFile = getBufferFile();
        BufferFileOutput output = new BufferFileOutput(bufferFile);
        BufferFileInput input = new BufferFileInput(bufferFile);
        FileOutputSource tableSource = null;
        int length = outputList.length;
        for(int i = 0; i < length; i++){
            FileOutputSource fileOutputSource = outputList[i];
            InputSource inputSource = fileOutputSource.getInputSource();
            if(tableSource == null && TableBlock.FILE_NAME.equals(inputSource.getAlias())){
                tableSource = fileOutputSource;
                continue;
            }
            onCompressFileProgress(inputSource.getAlias(),
                    inputSource.getMethod(),
                    output.position());
            fileOutputSource.makeBuffer(input, output);
        }
        if(tableSource != null){
            tableSource.makeBuffer(input, output);
        }
        output.close();
        return input;
    }
    private File getBufferFile(){
        File file = getZipOutput().getFile();
        File dir = file.getParentFile();
        String name = file.getAbsolutePath();
        name = "tmp" + name.hashCode();
        File bufFile;
        if(dir != null){
            bufFile = new File(dir, name);
        }else {
            bufFile = new File(name);
        }
        bufFile.deleteOnExit();
        return bufFile;
    }
}
