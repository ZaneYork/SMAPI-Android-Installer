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
package com.reandroid.dex.ins;

import com.reandroid.dex.id.ProtoId;
import com.reandroid.dex.key.ProtoKey;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class Ins45cc extends Size8Ins implements RegistersSet {

    private ProtoId mProtoId;

    public Ins45cc(Opcode<?> opcode) {
        super(opcode);
    }

    public ProtoId getProtoId(){
        return mProtoId;
    }
    public void setProtoId(ProtoKey protoKey){
        Section<ProtoId> section = getSection(SectionType.PROTO_ID);
        setProtoId(section.getOrCreate(protoKey));
    }
    public void setProtoId(ProtoId protoId){
        setShort(6, protoId.getIndex());
        this.mProtoId = protoId;
    }

    @Override
    public int getRegistersCount() {
        return getNibble(3);
    }
    @Override
    public void setRegistersCount(int count) {
        setNibble(3, count);
    }

    @Override
    public int getRegister(int index) {
        if(index < 4){
            return getNibble(8 + index);
        }
        return getNibble(2);
    }
    @Override
    public void setRegister(int index, int value) {
        if(index < 4){
            setNibble(8 + index, value);
        }else {
            setNibble(2, value);
        }
    }
    @Override
    public int getRegisterLimit(int index){
        return 0x0f;
    }

    @Override
    public int getData(){
        return getShortUnsigned(2);
    }
    @Override
    public void setData(int data){
        setShort(2, data);
    }

    public int getProtoIndex(){
        return getShortUnsigned(6);
    }
    public void setProtoIndex(int data){
        setShort(6, data);
        cacheProto();
    }

    @Override
    void cacheSectionItem() {
        super.cacheSectionItem();
        cacheProto();
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        refreshProtoId();
    }
    private void refreshProtoId() {
        ProtoId protoId = this.mProtoId;
        protoId = protoId.getReplace();
        setProtoIndex(protoId.getIndex());
    }
    private void cacheProto() {
        mProtoId = getSectionItem(SectionType.PROTO_ID, getProtoIndex());
    }
    @Override
    public void appendCode(SmaliWriter writer) throws IOException {
        super.appendCode(writer);
        writer.append(", ");
        getProtoId().append(writer);
    }
}