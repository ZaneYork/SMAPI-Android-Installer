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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliValidateException;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CollectionUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class SmaliLabel extends SmaliCode {

    private String labelName;

    public SmaliLabel(){
        super();
    }

    public String getLabelName() {
        return labelName;
    }
    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public int getIntegerData() throws IOException{
        int address = getAddress();
        if(address == -1){
            throw new SmaliValidateException("Missing target label '" + getLabelName() + "'", this);
        }
        return address;
    }
    public int getAddress() {
        return searchAddress();
    }
    private int searchAddress() {
        SmaliCodeSet codeSet = getCodeSet();
        if(codeSet == null){
            return -1;
        }
        if(codeSet != getParent()){
            int i = codeSet.indexOf(this);
            if(i < 0){
                return -1;
            }
            SmaliLabel label = (SmaliLabel) codeSet.get(i);
            return label.getAddress();
        }
        Iterator<SmaliInstruction> iterator = codeSet.iterator(codeSet.indexOf(this) + 1,
                SmaliInstruction.class);
        if(iterator.hasNext()) {
            return iterator.next().getAddress();
        }
        SmaliInstruction nullInstruction = codeSet.getNullInstruction();
        if(nullInstruction != null) {
            return nullInstruction.getAddress();
        }
        return -1;
    }
    public SmaliInstruction getTargetInstruction() {
        SmaliCodeSet codeSet = getCodeSet();
        if(codeSet == null) {
            return null;
        }
        int i = codeSet.indexOf(this);
        if(i < 0) {
            return null;
        }
        SmaliInstruction instruction = CollectionUtil.getFirst(codeSet.iterator(i + 1,
                SmaliInstruction.class));
        if(instruction == null) {
            instruction = codeSet.getNullInstruction();
        }
        return instruction;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(':');
        writer.append(getLabelName());
    }

    @Override
    public void parse(SmaliReader reader) throws IOException{
        reader.skipWhitespaces();
        setOrigin(reader.getCurrentOrigin());
        SmaliParseException.expect(reader, ':');
        int i1 = reader.indexOfWhiteSpaceOrComment();
        int i2 = reader.indexOfBeforeLineEnd('}');
        int i;
        if(i2 >= 0 && i2 < i1){
            i = i2;
        }else {
            i = i1;
        }
        int length = i - reader.position();
        setLabelName(reader.readString(length));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SmaliLabel)) {
            return false;
        }
        SmaliLabel other = (SmaliLabel) obj;
        return ObjectsUtil.equals(getLabelName(), other.getLabelName());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getLabelName());
    }
}
