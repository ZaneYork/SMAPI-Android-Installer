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

import java.io.IOException;

public class InsConstWide extends Ins51l implements ConstNumberLong{

    public InsConstWide(){
        super(Opcode.CONST_WIDE);
    }

    @Override
    public void set(long value) {
        setLong(value);
    }
    @Override
    public long getLong() {
        return super.getLong();
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
    public boolean isWideRegisterAt(int index) {
        return true;
    }

    @Override
    void appendHexData(SmaliWriter writer) throws IOException {
        writer.appendHex(getLong());
    }
}
