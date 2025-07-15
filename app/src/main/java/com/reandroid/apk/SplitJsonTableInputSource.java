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
package com.reandroid.apk;

import com.reandroid.archive.BlockInputSource;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.utils.CRCDigest;

import java.io.*;

public class SplitJsonTableInputSource extends BlockInputSource<TableBlock> {
    private final File resourcesDirectory;
    private TableBlock mCache;

    public SplitJsonTableInputSource(File resourcesDirectory) {
        super(TableBlock.FILE_NAME, null);
        this.resourcesDirectory =resourcesDirectory;
    }

    @Override
    public TableBlock getBlock() {
        try {
            return getTableBlock();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public long write(OutputStream outputStream) throws IOException {
        return getTableBlock().writeBytes(outputStream);
    }
    @Override
    public InputStream openStream() throws IOException {
        TableBlock tableBlock = getTableBlock();
        return new ByteArrayInputStream(tableBlock.getBytes());
    }
    @Override
    public long getLength() throws IOException{
        TableBlock tableBlock = getTableBlock();
        return tableBlock.countBytes();
    }
    @Override
    public long getCrc() throws IOException {
        CRCDigest outputStream = new CRCDigest();
        this.write(outputStream);
        return outputStream.getValue();
    }
    public TableBlock getTableBlock() throws IOException {
        if(mCache!=null){
            return mCache;
        }
        TableBlockJsonBuilder builder = new TableBlockJsonBuilder();
        TableBlock tableBlock = builder.scanDirectory(resourcesDirectory);
        mCache = tableBlock;
        return tableBlock;
    }
}
