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

import com.reandroid.archive.io.ZipFileInput;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class BufferFileInput extends ZipFileInput {
    private boolean unlocked;
    public BufferFileInput(File file){
        super(file);
    }

    public void unlock(){
        this.unlocked = true;
    }

    @Override
    public FileChannel getFileChannel() throws IOException {
        if(unlocked){
            return super.getFileChannel();
        }
        throw new IOException("File locked!");
    }
    @Override
    public void close() throws IOException {
        super.close();
        if(unlocked){
            File file = super.getFile();
            if(file.isFile()){
                file.delete();
            }
            unlocked = false;
        }
    }
}
