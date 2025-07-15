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
import com.reandroid.dex.smali.model.SmaliInstruction;

import java.io.IOException;

public class Ins10x extends Size2Ins {
    public Ins10x(Opcode<?> opcode) {
        super(opcode);
    }
    @Override
    public int getData(){
        return 0;
    }
    @Override
    public void setData(int data){
    }
    @Override
    public void appendCode(SmaliWriter writer) throws IOException {
        writer.append(getOpcode().getName());
    }
    @Override
    public void fromSmali(SmaliInstruction smaliInstruction) throws IOException {
        validateOpcode(smaliInstruction);
    }
}