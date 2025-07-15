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

public class Ins22b extends Ins22 implements RegistersSet {

    public Ins22b(Opcode<?> opcode) {
        super(opcode);
    }

    @Override
    public int getRegister(int index) {
        return getByteUnsigned(1 + index);
    }
    @Override
    public void setRegister(int index, int value) {
        setByte(1 + index, value);
    }
    @Override
    public int getRegisterLimit(int index){
        return 0xff;
    }

    @Override
    public int getData(){
        return getByteUnsigned(3);
    }

    @Override
    public int getSignedData() {
        return toSigned(getData(), 0xff);
    }

    @Override
    public void setData(int data){
        setByte(3, data);
    }
}