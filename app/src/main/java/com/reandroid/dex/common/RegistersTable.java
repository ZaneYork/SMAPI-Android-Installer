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
package com.reandroid.dex.common;

import com.reandroid.dex.ins.RegistersSet;

import java.util.Iterator;

public interface RegistersTable {

    int getRegistersCount();
    int getParameterRegistersCount();
    void setRegistersCount(int count);
    void setParameterRegistersCount(int count);
    boolean ensureLocalRegistersCount(int count);

    default int getLocalRegistersCount(){
        return getRegistersCount() - getParameterRegistersCount();
    }
    default Register getRegisterFor(int registerValue) {
        boolean parameter = false;
        int local = getLocalRegistersCount();
        if(registerValue >= local){
            registerValue = registerValue - local;
            parameter = true;
        }
        return new Register(registerValue, parameter);
    }
    default int getRegisterValue(Register register) {
        int result = register.getNumber();
        if(register.isParameter()){
            result += getLocalRegistersCount();
        }
        return result;
    }
    default Iterator<Register> getRegisters(RegistersSet registersSet) {
        return new Iterator<Register>() {
            private int mIndex;
            @Override
            public boolean hasNext() {
                return mIndex < registersSet.getRegistersCount();
            }
            @Override
            public Register next() {
                return RegistersTable.this.getRegisterFor(registersSet.getRegister(mIndex++));
            }
        };
    }

}
