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

import com.reandroid.arsc.ARSCLib;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.StagedAlias;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class SplitJsonResourceDecoder {
    private final TableBlock tableBlock;
    public SplitJsonResourceDecoder(TableBlock tableBlock){
        this.tableBlock = tableBlock;
    }
    public void decodeSplitJsonFiles(File resourcesDir) throws IOException {
        for(PackageBlock packageBlock: tableBlock.listPackages()){
            decodeSplitPackageJson(resourcesDir, packageBlock);
        }
    }
    private void decodeSplitPackageJson(File resourcesDir, PackageBlock packageBlock) throws IOException {
        File packageDir = new File(resourcesDir, packageBlock.buildDecodeDirectoryName());

        writeSplitPackageInfoJson(packageDir, packageBlock);

        for(SpecTypePair specTypePair: packageBlock.listSpecTypePairs()){
            for(TypeBlock typeBlock:specTypePair.getTypeBlockArray().listItems()){
                writeSplitTypeJsonFiles(packageDir, typeBlock);
            }
        }
    }
    private void writeSplitPackageInfoJson(File packageDirectory, PackageBlock packageBlock) throws IOException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(ARSCLib.NAME_arsc_lib_version, ARSCLib.getVersion());

        jsonObject.put(PackageBlock.NAME_package_id, packageBlock.getId());
        jsonObject.put(PackageBlock.NAME_package_name, packageBlock.getName());
        StagedAlias stagedAlias=StagedAlias
                .mergeAll(packageBlock.getStagedAliasList().getChildes());
        if(stagedAlias!=null){
            jsonObject.put(PackageBlock.NAME_staged_aliases,
                    stagedAlias.getStagedAliasEntryArray().toJson());
        }

        File packageFile = new File(packageDirectory, PackageBlock.JSON_FILE_NAME);
        jsonObject.write(packageFile);
    }
    private void writeSplitTypeJsonFiles(File packageDirectory, TypeBlock typeBlock) throws IOException {
        File file = new File(packageDirectory,
                typeBlock.buildUniqueDirectoryName() + ApkUtil.JSON_FILE_EXTENSION);
        JSONObject jsonObject = typeBlock.toJson();
        jsonObject.write(file);
    }
}
