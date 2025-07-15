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
import com.reandroid.archive.FileInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.json.JSONException;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.CRCDigest;

import java.io.*;

public class SingleJsonTableInputSource extends BlockInputSource<TableBlock> {

    private final InputSource inputSource;
    private TableBlock mCache;

    public SingleJsonTableInputSource(InputSource inputSource) {
        super(TableBlock.FILE_NAME, null);
        this.inputSource = inputSource;
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
    public TableBlock getTableBlock() throws IOException{
        if(mCache != null){
            return mCache;
        }
        TableBlock tableBlock = new TableBlock();
        InputStream inputStream = inputSource.openStream();
        try{
            JSONObject jsonObject = new JSONObject(inputStream);
            tableBlock.fromJson(jsonObject);
        }catch (JSONException ex){
            throw new JSONException(inputSource.getAlias(), ex);
        }
        mCache = tableBlock;
        return tableBlock;
    }
    public static SingleJsonTableInputSource fromFile(File rootDir, File jsonFile){
        String path = ApkUtil.jsonToArchiveResourcePath(rootDir, jsonFile);
        FileInputSource fileInputSource = new FileInputSource(jsonFile, path);
        return new SingleJsonTableInputSource(fileInputSource);
    }
}
