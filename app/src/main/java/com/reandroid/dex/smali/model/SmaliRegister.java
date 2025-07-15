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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.common.Register;
import com.reandroid.dex.common.RegistersTable;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class SmaliRegister extends Smali{

    private boolean parameter;
    private int number;

    public SmaliRegister(){
        super();
    }

    public Register toRegister(){
        return toRegister(null);
    }
    public Register toRegister(RegistersTable registersTable){
        return new Register(getNumber(), isParameter(), registersTable);
    }
    public boolean isParameter() {
        return parameter;
    }
    public void setParameter(boolean parameter) {
        this.parameter = parameter;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        if(isParameter()){
            writer.append('p');
        }else {
            writer.append('v');
        }
        writer.appendInteger(getNumber());
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        char ch = SmaliParseException.expect(reader, 'v', 'p');
        setParameter(ch == 'p');
        setNumber(reader.readInteger());
    }
}
