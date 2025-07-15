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

public class InsConst4 extends Size2Ins implements RegistersSet, ConstNumber {

    public InsConst4() {
        super(Opcode.CONST_4);
    }

    @Override
    public int getData(){
        return getNibble(3);
    }

    @Override
    public int getSignedData() {
        return toSigned(getData(), 0xf);
    }

    @Override
    public void setData(int data) {
        setNibble(3, data & 0xf);
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
        return getNibble(2);
    }
    @Override
    public void setRegister(int index, int value) {
        setNibble(2, value);
    }
    @Override
    public int getRegisterLimit(int index){
        return 0x0f;
    }

    @Override
    public int get() {
        return getData();
    }
    @Override
    public void set(int value) {
        setData(value);
    }
    @Override
    public int getRegister() {
        return getRegister(0);
    }
    @Override
    public void setRegister(int register) {
        setRegister(0, register);
    }
}