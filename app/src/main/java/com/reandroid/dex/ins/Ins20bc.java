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

import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.sections.SectionType;

public class Ins20bc extends Size4Ins {

    public Ins20bc(Opcode<?> opcode) {
        super(opcode);
    }

    public int getVerificationError() {
        return getByteUnsigned(1) & 0x3f;
    }
    public void setVerificationError(int error) {
        int value = (getByteUnsigned(1) & 0xc0) | (error & 0x3f);
        setByte (1, value);
    }
    public int getReferenceTypeValue() {
        return (getByteUnsigned(1) >> 6) & 0x3;
    }
    public void setReferenceTypeValue(int type) {
        int value = (getByteUnsigned(1) & 0x3f) | ((type & 0x3) << 6);
        setByte (1, value);
    }
    @Override
    public SectionType<? extends IdItem> getSectionType(){
        return SectionType.getReferenceType(getReferenceTypeValue());
    }
    public void setSectionType(SectionType<?> sectionType){
        setReferenceTypeValue(sectionType.getReferenceType());
    }

    @Override
    public int getData() {
        return getShortUnsigned(2);
    }
    @Override
    public void setData(int data) {
        setShort(2, data);
    }
}