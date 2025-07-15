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

import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class Ins21lh extends Size4Ins implements RegistersSet {
    private InsConstWide mReplaced;
    public Ins21lh(Opcode<?> opcode) {
        super(opcode);
    }

    @Override
    public int getRegistersCount() {
        return 1;
    }
    @Override
    public void setRegistersCount(int count) {
    }
    @Override
    public int getRegister(int index) {
        return getByteUnsigned(1);
    }
    @Override
    public void setRegister(int index, int value) {
        setByte(1, value);
    }
    @Override
    public int getRegisterLimit(int index){
        return 0xff;
    }

    @Override
    public int getData() {
        InsConstWide insConstWide = mReplaced;
        if(insConstWide != null){
            return insConstWide.getData();
        }
        return getShortUnsigned(2);
    }
    @Override
    public void setData(int data) {
        if((data & 0xffff0000) != 0){
            replaceIns(data);
        }else {
            setShort(2, data);
        }
    }
    public long getDataLong(){
        InsConstWide insConstWide = mReplaced;
        if(insConstWide != null){
            return insConstWide.getLong();
        }
        return (long) getData() << 48;
    }
    @Override
    public void setLong(long data){
        InsConstWide insConstWide = mReplaced;
        if(insConstWide != null){
            insConstWide.setData(data);
        }
        if((data & 0xffff00000000L) != 0){
            replaceIns(data);
        }else {
            setData((int) (data >>> 48));
        }
    }

    private void replaceIns(long data){
        InsConstWide insConstWide = replace(Opcode.CONST_WIDE);
        insConstWide.setRegister(this.getRegister());
        insConstWide.setData(data);
        this.mReplaced = insConstWide;
    }
    @Override
    void appendHexData(SmaliWriter writer) throws IOException {
        writer.appendHex(getDataLong());
    }
}