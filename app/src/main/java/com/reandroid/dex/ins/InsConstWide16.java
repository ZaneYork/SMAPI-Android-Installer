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

public class InsConstWide16 extends Ins21s implements ConstNumberLong{

    private InsConstWide mReplaced;

    public InsConstWide16(){
        super(Opcode.CONST_WIDE_16);
    }

    @Override
    public void set(long value) {
        InsConstWide insConstWide = mReplaced;
        if(insConstWide != null){
            insConstWide.set(value);
        }else if(value <= 0x7fff && value >= -0x7fff){
            setData((int) value);
        }else {
            replaceIns(value);
        }
    }
    @Override
    public long getLong() {
        InsConstWide insConstWide = mReplaced;
        if(insConstWide != null){
            return insConstWide.getLong();
        }
        return this.getData();
    }

    @Override
    public int getRegister() {
        return getRegister(0);
    }
    @Override
    public void setRegister(int register) {
        setRegister(0, register);
    }

    @Override
    public int getData() {
        InsConstWide insConstWide = mReplaced;
        if(insConstWide != null){
            return insConstWide.getData();
        }
        return super.getData();
    }

    @Override
    public void setData(int data) {
        InsConstWide insConstWide = mReplaced;
        if(insConstWide != null){
            insConstWide.set(data);
        }else if(data <= 0x7fff && data >= -0x7fff){
            super.setData(data);
        }else {
            replaceIns(data);
        }
    }

    private void replaceIns(long data){
        InsConstWide insConstWide = replace(Opcode.CONST_WIDE);
        insConstWide.setRegister(this.getRegister());
        insConstWide.setData(data);
        this.mReplaced = insConstWide;
    }

    @Override
    public boolean isWideRegisterAt(int index) {
        return true;
    }
}
