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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.StagedAlias;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TableBlockJsonBuilder {
    public TableBlockJsonBuilder(){
    }
    public TableBlock scanDirectory(File resourcesDir) throws IOException {
        if(!resourcesDir.isDirectory()){
            throw new IOException("No such directory: "+resourcesDir);
        }
        List<File> packageDirectories = ApkUtil.listPackageDirectories(resourcesDir);
        if(packageDirectories.size() == 0){
            throw new IOException("No package sub directory found in : "+resourcesDir);
        }
        TableBlock tableBlock = new TableBlock();
        for(File packageDir : packageDirectories){
            scanPackageDirectory(tableBlock, packageDir);
        }
        tableBlock.sortPackages();
        tableBlock.refresh();
        return tableBlock;
    }
    private void scanPackageDirectory(TableBlock tableBlock, File packageDir) throws IOException{
        File packageJsonFile = new File(packageDir, PackageBlock.JSON_FILE_NAME);
        if(!packageJsonFile.isFile()){
            throw new IOException("Invalid package directory! Package file missing: "
                    + packageJsonFile);
        }
        JSONObject jsonObject = new JSONObject(packageJsonFile);
        PackageBlock packageBlock = tableBlock.getPackageArray()
                .getOrCreate(jsonObject.getInt(PackageBlock.NAME_package_id));
        packageBlock.setName(jsonObject.optString(PackageBlock.NAME_package_name));
        if(jsonObject.has(PackageBlock.NAME_staged_aliases)){
            JSONArray stagedJson = jsonObject.getJSONArray(PackageBlock.NAME_staged_aliases);
            StagedAlias stagedAlias = new StagedAlias();
            stagedAlias.getStagedAliasEntryArray().fromJson(stagedJson);
            packageBlock.getStagedAliasList().add(stagedAlias);
        }
        List<File> typeFileList = ApkUtil.listFiles(packageDir, ApkUtil.JSON_FILE_EXTENSION);
        typeFileList.remove(packageJsonFile);
        for(File typeFile:typeFileList){
            loadType(packageBlock, typeFile);
        }
        packageBlock.sortTypes();
    }
    private void loadType(PackageBlock packageBlock, File typeJsonFile) throws IOException{
        JSONObject jsonObject = new JSONObject(typeJsonFile);
        ResConfig resConfig = new ResConfig();
        resConfig.fromJson(jsonObject.getJSONObject(TypeBlock.NAME_config));
        TypeBlock typeBlock=packageBlock.getSpecTypePairArray()
                .getOrCreate(
                        ((byte)(0xff & jsonObject.getInt(TypeBlock.NAME_id)))
                        , resConfig);
        typeBlock.fromJson(jsonObject);
    }
}
