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

import com.reandroid.dex.common.Register;
import com.reandroid.dex.common.RegisterType;
import com.reandroid.dex.common.RegistersTable;

public class RegisterReference extends Register {
    private final RegistersSet registersSet;
    private final int index;

    public RegisterReference(RegistersTable registersTable, RegistersSet registersSet, int index){
        super(0, false, registersTable);
        this.registersSet = registersSet;
        this.index = index;
    }

    public Editor toEditor(){
        return new Editor(this);
    }

    @Override
    public int getNumber() {
        int register = getValue();
        int local = getLocalRegistersCount();
        if(register >= local){
            register = register - local;
        }
        return register;
    }
    @Override
    public boolean isParameter() {
        return getValue() >= getLocalRegistersCount();
    }
    public int getValue(){
        return getRegistersSet().getRegister(getIndex());
    }
    public void setRegisterValue(int register){
        getRegistersSet().setRegister(getIndex(), register);
    }
    public int getIndex() {
        return index;
    }

    public int getLocalRegistersCount(){
        RegistersTable table = getRegistersTable();
        return table.getRegistersCount() - table.getParameterRegistersCount();
    }
    public RegistersSet getRegistersSet() {
        return registersSet;
    }
    public RegisterType getRegisterType(){
        return getRegistersSet().getRegisterFormat().get(getIndex());
    }
    public int getLimit() {
        return getRegistersSet().getRegisterLimit(getIndex());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RegisterReference registerReference = (RegisterReference) obj;
        return isParameter() == registerReference.isParameter() &&
                getNumber() == registerReference.getNumber();
    }

    public static class Editor extends RegisterReference {

        private final RegisterReference mBaseRegisterReference;
        private final int number;
        private final boolean parameter;

        public Editor(RegisterReference registerReference) {
            super(null, null, registerReference.getIndex());
            this.mBaseRegisterReference = registerReference;
            this.number = registerReference.getNumber();
            this.parameter = registerReference.isParameter();
        }

        public void apply(){
            RegisterReference baseRegisterReference = getBaseReg();
            baseRegisterReference.setRegisterValue(getValue());
        }
        private boolean isChanged(){
            RegisterReference baseRegisterReference = getBaseReg();
            return this.isParameter() == baseRegisterReference.isParameter() &&
                    this.getNumber() == baseRegisterReference.getNumber() &&
                    this.getValue() == baseRegisterReference.getValue();
        }
        @Override
        public int getValue() {
            int register = getNumber();
            if(isParameter()){
                register += getLocalRegistersCount();
            }
            return register;
        }

        @Override
        public int getNumber() {
            return this.number;
        }
        @Override
        public boolean isParameter() {
            return this.parameter;
        }
        @Override
        public int getIndex() {
            return getBaseReg().getIndex();
        }
        @Override
        public RegistersTable getRegistersTable() {
            return getBaseReg().getRegistersTable();
        }
        @Override
        public RegistersSet getRegistersSet() {
            return getBaseReg().getRegistersSet();
        }
        public RegisterReference getBaseReg() {
            return mBaseRegisterReference;
        }
    }
}
