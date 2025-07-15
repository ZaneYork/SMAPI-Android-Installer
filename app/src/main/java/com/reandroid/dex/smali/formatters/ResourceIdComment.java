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
package com.reandroid.dex.smali.formatters;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResValue;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public interface ResourceIdComment extends SmaliComment {

    void writeComment(SmaliWriter writer, int id) throws IOException;

    class ResourceTableComment implements ResourceIdComment{

        private final PackageBlock packageBlock;
        private final TableBlock tableBlock;

        public ResourceTableComment(PackageBlock packageBlock){
            this.packageBlock = packageBlock;
            this.tableBlock = packageBlock.getTableBlock();
        }

        @Override
        public void writeComment(SmaliWriter writer, int resourceId) {
            if(!PackageBlock.isResourceId(resourceId)){
                return;
            }
            String comment = buildComment(resourceId);
            if(comment != null){
                writer.appendComment(comment);
            }
        }
        private String buildComment(int resourceId){
            ResourceEntry resourceEntry = tableBlock.getResource(resourceId);
            if(resourceEntry == null || !resourceEntry.isDeclared()){
                return null;
            }

            String ref = resourceEntry
                    .buildReference(packageBlock, ValueType.REFERENCE);

            if(resourceEntry.getPackageBlock().getTableBlock() != tableBlock){
                return ref;
            }
            if("id".equals(resourceEntry.getType())){
                return ref;
            }

            Entry entry = resourceEntry.get();
            if(entry == null){
                return ref;
            }
            ResValue resValue = entry.getResValue();
            if(resValue == null){
                return ref;
            }
            String decoded = resValue.decodeValue();
            if(decoded == null){
                return ref;
            }
            if(decoded.length() > 100){
                decoded = decoded.substring(0, 100) + " ...";
            }
            return ref + " '" + replaceNewLines(decoded)+ "'";
        }
        private String replaceNewLines(String decoded){
            StringBuilder builder = new StringBuilder();
            int length = decoded.length();
            for(int i = 0; i < length; i++){
                char ch = decoded.charAt(i);
                builder.append(escapeChar(ch));
            }
            return builder.toString();
        }
        private String escapeChar(char ch){
            if(ch == '\n'){
                return "\\n";
            }
            if(ch == '\t'){
                return "\\t";
            }
            if(ch == '\r'){
                return "\\r";
            }
            return String.valueOf(ch);
        }
    }
}
