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

public class Ins3rms extends Size6Ins implements RegistersSet {

    public Ins3rms(Opcode<?> opcode) {
        super(opcode);
    }

    @Override
    public int getRegistersCount() {
        return getByteUnsigned(1);
    }
    @Override
    public void setRegistersCount(int count) {
    }
    @Override
    public int getRegister(int index) {
        return getShortUnsigned(4) + index;
    }
    @Override
    public void setRegister(int index, int value) {
        if(index != 0) {
            setShort(2, value + 1 - getRegister());
        }else {
            setShort(4, value);
        }
    }
    @Override
    public int getRegisterLimit(int index){
        return 0xffff;
    }

    @Override
    public String toString() {
        return getOpcode() + " {" + getRegistersIterator() + "}, " + getSectionId();
    }

}