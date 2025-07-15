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

public class Ins21ih extends Size4Ins implements RegistersSet {
    private InsConst mReplaced;

    public Ins21ih(Opcode<?> opcode) {
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
        InsConst insConst = this.mReplaced;
        if(insConst != null){
            return insConst.getData();
        }
        return getShortUnsigned(2) << 16;
    }
    @Override
    public void setData(int data) {
        InsConst insConst = this.mReplaced;
        if(insConst != null){
            insConst.setData(data);
            return;
        }
        if((data & 0x0000ffff) != 0){
            replaceIns(data);
        }else {
            setShort(2, data >>> 16);
        }
    }
    private void replaceIns(int data){
        InsConst insConst = replace(Opcode.CONST);
        insConst.setRegister(this.getRegister());
        insConst.setData(data);
        this.mReplaced = insConst;
    }

}