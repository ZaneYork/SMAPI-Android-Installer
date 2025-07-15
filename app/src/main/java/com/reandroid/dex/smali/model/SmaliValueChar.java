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

import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.PrimitiveKey;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class SmaliValueChar extends SmaliValue{

    private char value;

    public SmaliValueChar(){
        super();
    }

    public char getValue(){
        return value;
    }
    public void setValue(char value) {
        this.value = value;
    }

    @Override
    public Key getKey() {
        return PrimitiveKey.of(getValue());
    }

    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.CHAR;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        DexUtils.appendSingleQuotedChar(writer, getValue());
    }
    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        if(reader.read() != '\''){
            reader.skip(-1);
            throw new SmaliParseException("Missing start \"'\"", reader);
        }
        char ch = reader.readASCII();
        if(ch == '\\'){
            ch = reader.readASCII();
            if(ch == 'u'){
                try{
                    int i = HexUtil.parseHex(reader.readString(4));
                    ch = (char) i;
                }catch (NumberFormatException ex){
                    reader.skip(-4);
                    throw new SmaliParseException("Invalid four-char hex encoded char", reader);
                }
            }else {
                switch (ch) {
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                }
            }
        }
        setValue(ch);
        if(reader.read() != '\''){
            reader.skip(-2);
            throw new SmaliParseException("Missing end \"'\"", reader);
        }
    }

}
