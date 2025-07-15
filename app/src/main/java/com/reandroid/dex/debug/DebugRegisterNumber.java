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
package com.reandroid.dex.debug;

import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliDebug;
import com.reandroid.dex.smali.model.SmaliDebugElement;
import com.reandroid.dex.smali.model.SmaliDebugRegister;

import java.io.IOException;

abstract class DebugRegisterNumber extends DebugElement {

    private final Ule128Item registerNumber;

    DebugRegisterNumber(int childesCount, int flag) {
        super(childesCount + 1, flag);
        this.registerNumber = new Ule128Item();
        addChild(1, registerNumber);
    }
    DebugRegisterNumber(int childesCount, DebugElementType<?> elementType) {
        this(childesCount, elementType.getFlag());
    }

    public int getRegisterNumber() {
        return registerNumber.get();
    }
    public void setRegister(int register){
        this.registerNumber.set(register);
    }

    @Override
    public void appendExtra(SmaliWriter writer) throws IOException {
        if(isValid()) {
            getSmaliDirective().append(writer);
            writer.appendRegister(getRegisterNumber());
        }
    }

    @Override
    public void merge(DebugElement element){
        super.merge(element);
        DebugRegisterNumber coming = (DebugRegisterNumber) element;
        this.registerNumber.set(coming.registerNumber.get());
    }

    @Override
    public void fromSmali(SmaliDebugElement smaliDebugElement) throws IOException {
        super.fromSmali(smaliDebugElement);
        SmaliDebugRegister smaliDebugRegister = (SmaliDebugRegister) smaliDebugElement;
        setRegister(smaliDebugRegister.getRegister().getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DebugRegisterNumber debug = (DebugRegisterNumber) obj;
        return getFlag() == debug.getFlag() &&
                registerNumber.get() == debug.registerNumber.get();
    }
    @Override
    public int hashCode() {
        int hash = getFlag();
        hash = hash * 31 + registerNumber.get();
        return hash;
    }

    @Override
    public String toString() {
        return getElementType() + " v" + getRegisterNumber();
    }
}
