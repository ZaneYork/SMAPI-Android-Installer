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

import com.reandroid.archive.ZipEntryMap;
import com.reandroid.archive.InputSource;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.CompareUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class UncompressedFiles implements JSONConvert<JSONObject> {
    private final Set<String> mPathList;
    private final Set<String> mExtensionList;
    private String mResRawDir;
    public UncompressedFiles(){
        this.mPathList=new HashSet<>();
        this.mExtensionList=new HashSet<>();
    }
    public void setResRawDir(String resRawDir){
        this.mResRawDir=resRawDir;
    }
    public void apply(ZipEntryMap archive){
        for(InputSource inputSource:archive.toArray()){
            apply(inputSource);
        }
    }
    public void apply(InputSource inputSource){
        inputSource.setUncompressed(isUncompressed(inputSource.getAlias())
                || isUncompressed(inputSource.getName()));
    }
    public boolean isUncompressed(String path){
        if(path==null){
            return false;
        }
        if(containsPath(path)||containsExtension(path)||isResRawDir(path)){
            return true;
        }
        String extension=getExtension(path);
        return containsExtension(extension);
    }
    private boolean isResRawDir(String path){
        String dir=mResRawDir;
        if(dir==null||dir.length()==0){
            return false;
        }
        return path.startsWith(dir);
    }
    public boolean containsExtension(String extension){
        if(extension==null){
            return false;
        }
        if(mExtensionList.contains(extension)){
            return true;
        }
        if(!extension.startsWith(".")){
            return mExtensionList.contains("."+extension);
        }
        return mExtensionList.contains(extension.substring(1));
    }
    public boolean containsPath(String path){
        path=sanitizePath(path);
        if(path==null){
            return false;
        }
        return mPathList.contains(path);
    }
    public void addPath(ZipEntryMap zipArchive){
        for(InputSource inputSource: zipArchive.toArray()){
            addPath(inputSource);
        }
    }
    public void addPath(InputSource inputSource){
        if(!inputSource.isUncompressed()){
            return;
        }
        addPath(inputSource.getAlias());
    }
    public void addPath(String path){
        path=sanitizePath(path);
        if(path==null){
            return;
        }
        mPathList.add(path);
    }
    public void removePath(String path){
        path=sanitizePath(path);
        if(path==null){
            return;
        }
        mPathList.remove(path);
    }
    public void replacePath(String path, String rep){
        path=sanitizePath(path);
        rep=sanitizePath(rep);
        if(path==null||rep==null){
            return;
        }
        if(!mPathList.contains(path)){
            return;
        }
        mPathList.remove(path);
        mPathList.add(rep);
    }
    public void addCommonExtensions(){
        for(String ext:COMMON_EXTENSIONS){
            addExtension(ext);
        }
    }
    public void addExtension(String extension){
        if(extension==null || extension.length()==0){
            return;
        }
        mExtensionList.add(extension);
    }
    public void clearPaths(){
        mPathList.clear();
    }
    public void clearExtensions(){
        mExtensionList.clear();
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        JSONArray extensions = new JSONArray(mExtensionList);
        extensions.sort(CompareUtil.STRING_COMPARATOR);
        jsonObject.put(NAME_extensions, extensions);
        JSONArray paths = new JSONArray(mPathList);
        paths.sort(CompareUtil.STRING_COMPARATOR);
        jsonObject.put(NAME_paths, paths);
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        clearExtensions();
        clearPaths();
        if(json==null){
            return;
        }
        JSONArray extensions = json.optJSONArray(NAME_extensions);
        if(extensions!=null){
            int length = extensions.length();
            for(int i=0;i<length;i++){
                this.addExtension(extensions.getString(i));
            }
        }
        JSONArray paths = json.optJSONArray(NAME_paths);
        if(paths!=null){
            int length = paths.length();
            for(int i=0;i<length;i++){
                this.addPath(paths.getString(i));
            }
        }
    }
    public void merge(UncompressedFiles uf){
        if(uf==null||uf==this){
            return;
        }
        for(String path: uf.mPathList){
            addPath(path);
        }
        for(String ext:uf.mExtensionList){
            addExtension(ext);
        }
    }
    public void fromJson(File jsonFile) throws IOException {
        if(!jsonFile.isFile()){
            return;
        }
        JSONObject jsonObject=new JSONObject(new FileInputStream(jsonFile));
        fromJson(jsonObject);
    }
    private static String sanitizePath(String path){
        if(path==null || path.length()==0){
            return null;
        }
        path=path.replace(File.separatorChar, '/').trim();
        while (path.startsWith("/")){
            path=path.substring(1);
        }
        if(path.length()==0){
            return null;
        }
        return path;
    }
    private static String getExtension(String path){
        path=sanitizePath(path);
        if(path==null){
            return null;
        }
        int i = path.lastIndexOf('/');
        if(i>0){
            i++;
            path=path.substring(i);
        }
        i = path.lastIndexOf('.');
        if(i>0){
            return path.substring(i);
        }
        return null;
    }

    public static final String JSON_FILE = "uncompressed-files.json";
    public static final String NAME_paths = "paths";
    public static final String NAME_extensions = "extensions";
    public static String[] COMMON_EXTENSIONS=new String[]{
            ".png",
            ".jpg",
            ".mp3",
            ".mp4",
            ".wav",
            ".webp",
    };
}
