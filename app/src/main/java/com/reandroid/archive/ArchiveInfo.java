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
package com.reandroid.archive;

import com.reandroid.archive.block.CentralEntryHeader;
import com.reandroid.archive.block.DataDescriptor;
import com.reandroid.archive.block.LocalFileHeader;
import com.reandroid.archive.io.ArchiveFileEntrySource;
import com.reandroid.archive.writer.HeaderInterceptor;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.ObjectsUtil;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class ArchiveInfo implements HeaderInterceptor, JSONConvert<JSONObject> {

    private long dosTime;
    private int cehVersionMadeBy;
    private int lfhVersionMadeBy;
    private int versionExtract;

    public ArchiveInfo(){
    }

    public long getDosTime() {
        return dosTime;
    }
    public void setDosTime(long dosTime) {
        this.dosTime = dosTime;
    }
    public int getCehVersionMadeBy() {
        return cehVersionMadeBy;
    }
    public void setCehVersionMadeBy(int cehVersionMadeBy) {
        this.cehVersionMadeBy = cehVersionMadeBy;
    }
    public int getLfhVersionMadeBy() {
        return lfhVersionMadeBy;
    }
    public void setLfhVersionMadeBy(int lfhVersionMadeBy) {
        this.lfhVersionMadeBy = lfhVersionMadeBy;
    }
    public void setVersionExtract(int versionExtract) {
        this.versionExtract = versionExtract;
    }
    public int getVersionExtract() {
        return versionExtract;
    }
    @Override
    public void onWriteLfh(LocalFileHeader header) {
        long dosTime = getDosTime();
        if(dosTime != -1){
            header.setDosTime(dosTime);
        }
        int i = getLfhVersionMadeBy();
        if(i != -1){
            header.setVersionMadeBy(i);
        }
    }

    @Override
    public void onWriteDD(DataDescriptor dataDescriptor) {

    }
    @Override
    public void onWriteCeh(CentralEntryHeader header) {
        long dosTime = getDosTime();
        if(dosTime != -1){
            header.setDosTime(dosTime);
        }
        int i = getCehVersionMadeBy();
        if(i != -1){
            header.setVersionMadeBy(i);
        }
        i = getVersionExtract();
        if(i != -1){
            header.setVersionExtract(i);
        }
    }
    public void writeToDirectory(File dir) throws IOException {
        File file = new File(dir, JSON_FILE);
        write(file);
    }
    public void write(File file) throws IOException {
        toJson().write(file);
    }
    @Override
    public JSONObject toJson(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NAME_dosTime, getDosTime());
        jsonObject.put(NAME_cehVersionMadeBy, getCehVersionMadeBy());
        jsonObject.put(NAME_lfhVersionMadeBy, getLfhVersionMadeBy());
        jsonObject.put(NAME_versionExtract, getVersionExtract());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject jsonObject){
        setDosTime(jsonObject.optLong(NAME_dosTime, getDosTime()));
        setCehVersionMadeBy(jsonObject.optInt(NAME_cehVersionMadeBy, getCehVersionMadeBy()));
        setLfhVersionMadeBy(jsonObject.optInt(NAME_lfhVersionMadeBy, getLfhVersionMadeBy()));
        setVersionExtract(jsonObject.optInt(NAME_versionExtract, getVersionExtract()));
    }
    @Override
    public String toString() {
        return toJson().toString();
    }

    public static ArchiveInfo readJson(File fileOrDir) throws IOException{
        File file;
        if(fileOrDir.isFile()){
            file = fileOrDir;
        }else {
            file = new File(fileOrDir, JSON_FILE);
        }
        if(!file.isFile()){
            return null;
        }
        ArchiveInfo info = new ArchiveInfo();
        info.fromJson(new JSONObject(file));
        return info;
    }

    public static ArchiveInfo apk(){

        ArchiveInfo info = new ArchiveInfo();

        info.setDosTime(0x2210821);
        info.setCehVersionMadeBy(0x0300);
        info.setLfhVersionMadeBy(0);
        info.setVersionExtract(0x0000);

        return info;
    }
    public static ArchiveInfo zip(){

        ArchiveInfo info = new ArchiveInfo();

        info.setDosTime(0x210000);
        info.setCehVersionMadeBy(0x0014);
        info.setLfhVersionMadeBy(0x0014);
        info.setVersionExtract(0x0014);

        return info;
    }
    public static ArchiveInfo build(InputSource[] sources){
        return build(pick(sources));
    }
    public static ArchiveInfo build(Iterator<InputSource> iterator){
        return build(pick(iterator));
    }
    public static ArchiveInfo build(ArchiveFileEntrySource entrySource){
        if(entrySource == null){
            return null;
        }
        ArchiveEntry entry = entrySource.getArchiveEntry();
        ArchiveInfo info = new ArchiveInfo();

        info.setDosTime(entry.getDosTime());
        info.setCehVersionMadeBy(entry.getCentralEntryHeader().getVersionMadeBy());
        info.setLfhVersionMadeBy(entry.getLocalFileHeader().getVersionMadeBy());
        info.setVersionExtract(entry.getCentralEntryHeader().getVersionExtract());
        return info;
    }
    private static ArchiveFileEntrySource pick(InputSource[] sources){
        ArchiveFileEntrySource result = null;
        for(InputSource inputSource : sources){
            if(inputSource instanceof ArchiveFileEntrySource){
                result = (ArchiveFileEntrySource) inputSource;
            }else {
                continue;
            }
            String name = inputSource.getName();
            if(!name.startsWith("META-INF/")){
                break;
            }
        }
        return result;
    }
    private static ArchiveFileEntrySource pick(Iterator<InputSource> iterator){
        ArchiveFileEntrySource result = null;
        while (iterator.hasNext()){
            InputSource inputSource = iterator.next();
            if(inputSource instanceof ArchiveFileEntrySource){
                result = (ArchiveFileEntrySource) inputSource;
            }else {
                continue;
            }
            String name = inputSource.getName();
            if(!name.startsWith("META-INF/")){
                break;
            }
        }
        return result;
    }

    public static final String JSON_FILE = ObjectsUtil.of("archive-info.json");

    public static final String NAME_dosTime = ObjectsUtil.of("dos_time");
    public static final String NAME_cehVersionMadeBy = ObjectsUtil.of("ceh_version_made_by");
    public static final String NAME_lfhVersionMadeBy = ObjectsUtil.of("lfh_version_made_by");
    public static final String NAME_versionExtract = ObjectsUtil.of("version_extract");
    public static final String NAME_platform = ObjectsUtil.of("platform");

}
