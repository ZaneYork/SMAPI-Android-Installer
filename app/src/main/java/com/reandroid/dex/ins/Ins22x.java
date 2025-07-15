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

public class Ins22x extends Ins22 {

    public Ins22x(Opcode<?> opcode) {
        super(opcode);
    }

    @Override
    public int getRegister(int index) {
        if(index == 0) {
            return getByteUnsigned(1);
        }
        return getShortUnsigned(2);
    }
    @Override
    public void setRegister(int index, int value) {
        if(index == 0){
            setByte(1, value);
        }else {
            setShort(2, value);
        }
    }
    @Override
    public int getRegisterLimit(int index){
        if(index == 0) {
            return 0xff;
        }
        return 0xffff;
    }

    @Override
    void appendCodeData(SmaliWriter writer) {
    }
}