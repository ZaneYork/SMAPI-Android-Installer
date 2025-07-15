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
package com.reandroid.arsc.header;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.common.FileChannelInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class InfoHeader extends HeaderBlock{
    public InfoHeader(short type) {
        super(type);
    }
    public InfoHeader() {
        this((short) 0);
    }

    @Override
    public int getMinimumSize(){
        return INFO_MIN_SIZE;
    }

    @Override
    void initExtraBytes(ByteArray extraBytes, int difference){
    }
    @Override
    public int countBytes() {
        return 8;
    }


    public static InfoHeader readHeaderBlock(File file) throws IOException {
        return readHeaderBlock(FileChannelInputStream.read(file, INFO_MIN_SIZE));
    }
    public static InfoHeader readHeaderBlock(InputStream inputStream) throws IOException {
        InfoHeader infoHeader=new InfoHeader();
        infoHeader.readBytes(inputStream);
        return infoHeader;
    }
    public static InfoHeader readHeaderBlock(BlockReader blockReader) throws IOException {
        InfoHeader infoHeader=new InfoHeader();
        infoHeader.readBytes(blockReader);
        return infoHeader;
    }
    public static InfoHeader readHeaderBlock(byte[] bytes) throws IOException {
        BlockReader reader = new BlockReader(bytes);
        InfoHeader infoHeader = new InfoHeader();
        infoHeader.readBytes(reader);
        return infoHeader;
    }

    public static InfoHeader read(BlockReader reader) throws IOException {
        InfoHeader infoHeader = new InfoHeader();
        if(reader.available() < infoHeader.getMinimumSize()){
            return null;
        }
        int pos = reader.getPosition();
        infoHeader.readBytes(reader);
        reader.seek(pos);
        return infoHeader;
    }

    public static final int INFO_MIN_SIZE = 8;
}
