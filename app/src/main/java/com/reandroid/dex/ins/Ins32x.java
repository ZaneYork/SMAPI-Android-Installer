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


public class Ins32x extends Size6Ins implements RegistersSet{

    public Ins32x(Opcode<?> opcode) {
        super(opcode);
    }

    @Override
    public int getRegistersCount() {
        return 2;
    }
    @Override
    public final void setRegistersCount(int count) {
    }
    @Override
    public int getRegister(int index) {
        return getShortUnsigned(2 + index * 2);
    }
    @Override
    public void setRegister(int index, int value) {
        setShort(2 + index * 2, value);
    }
    @Override
    public int getRegisterLimit(int index){
        return 0xffff;
    }

    @Override
    public int getData(){
        return 0;
    }
    public void setData(int data){
    }

    @Override
    void appendCodeData(SmaliWriter writer) {
    }
}