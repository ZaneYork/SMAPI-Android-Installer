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


public class Ins51l extends Size10Ins implements RegistersSet {

    public Ins51l(Opcode<?> opcode) {
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
        return (int) getLong();
    }
    public long getLong() {
        return super.getLong();
    }
    @Override
    public void setLong(long data) {
        super.setLong(data);
    }

    @Override
    public void setData(int data) {
        setLong((long) data);
    }
    public void setData(long data) {
        setLong(data);
    }

}