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

import com.reandroid.archive.FileInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.json.JSONException;
import com.reandroid.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


 public class JsonXmlInputSource extends InputSource {
    private final InputSource inputSource;
    private APKLogger apkLogger;
    public JsonXmlInputSource(InputSource inputSource) {
        super(inputSource.getAlias());
        this.inputSource=inputSource;
    }
    @Override
    public long write(OutputStream outputStream) throws IOException {
        return getResXmlBlock().writeBytes(outputStream);
    }
    @Override
    public InputStream openStream() throws IOException {
        ResXmlDocument resXmlDocument = getResXmlBlock();
        return new ByteArrayInputStream(resXmlDocument.getBytes());
    }
    @Override
    public long getLength() throws IOException{
        ResXmlDocument resXmlDocument = getResXmlBlock();
        return resXmlDocument.countBytes();
    }
    private ResXmlDocument getResXmlBlock() throws IOException{
        logVerbose("From json: "+getAlias());
        ResXmlDocument resXmlDocument =newInstance();
        InputStream inputStream=inputSource.openStream();
        try{
            JSONObject jsonObject=new JSONObject(inputStream);
            resXmlDocument.fromJson(jsonObject);
        }catch (JSONException ex){
            throw new JSONException(inputSource.getAlias()+": "+ex.getMessage(), ex);
        }
        return resXmlDocument;
    }
    ResXmlDocument newInstance(){
        return new ResXmlDocument();
    }
    void setAPKLogger(APKLogger logger) {
        this.apkLogger = logger;
    }
    void logMessage(String msg) {
        if(apkLogger!=null){
            apkLogger.logMessage(msg);
        }
    }
    private void logError(String msg, Throwable tr) {
        if(apkLogger!=null){
            apkLogger.logError(msg, tr);
        }
    }
    private void logVerbose(String msg) {
        if(apkLogger!=null){
            apkLogger.logVerbose(msg);
        }
    }

    public static JsonXmlInputSource fromFile(File rootDir, File jsonFile){
        String path=ApkUtil.jsonToArchiveResourcePath(rootDir, jsonFile);
        FileInputSource fileInputSource=new FileInputSource(jsonFile, path);
        return new JsonXmlInputSource(fileInputSource);
    }
}
