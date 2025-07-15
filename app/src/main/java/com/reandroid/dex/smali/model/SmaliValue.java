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

import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.PrimitiveKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.value.*;

import java.io.IOException;

public class SmaliValue extends Smali{

    public SmaliValue(){
        super();
    }

    public DexValueType<?> getValueType(){
        return null;
    }
    public Key getKey() {
        throw new RuntimeException("Method not implemented");
    }
    @Override
    public void parse(SmaliReader reader) throws IOException {
    }

    public static SmaliValue create(SmaliReader reader) throws IOException{
        reader.skipSpaces();
        int position = reader.position();
        byte b = reader.get(position);
        if(b == '.'){
            SmaliDirective directive = SmaliDirective.parse(reader, false);
            if(directive == SmaliDirective.ENUM){
                return new SmaliValueEnum();
            }
            if(directive == SmaliDirective.SUB_ANNOTATION){
                return new SmaliValueAnnotation();
            }
            throw new SmaliParseException("Unrecognized value", reader);
        }
        if(b == '{'){
            return new SmaliValueArray();
        }
        if(b == '\''){
            return new SmaliValueChar();
        }
        if(b == 'n'){
            return new SmaliValueNull();
        }
        if(b == 't' || b == 'f'){
            return new SmaliValueBoolean();
        }
        if(b == '-' || b == '+'){
            position ++;
            b = reader.get(position);
        }
        if(b == '0' && reader.get(position + 1) == 'x'){
            b = reader.get(reader.indexOfWhiteSpaceOrComment() - 1);
            if(b == 't'){
                return new SmaliValueByte();
            }
            if(b == 'S' || b == 's'){
                return new SmaliValueShort();
            }
            if(b == 'L'){
                return new SmaliValueLong();
            }
            return new SmaliValueInteger();
        }
        byte[] infinity = new byte[]{'I', 'n', 'f', 'i', 'n', 'i', 't', 'y'};
        if(reader.startsWith(infinity, position)){
            b = reader.get(infinity.length + position);
            if(b == 'f'){
                return new SmaliValueFloat();
            }
            return new SmaliValueDouble();
        }
        byte[] nan = new byte[]{'N', 'a', 'N'};
        if(reader.startsWith(nan, position)){
            b = reader.get(nan.length + position);
            if(b == 'f'){
                return new SmaliValueFloat();
            }
            return new SmaliValueDouble();
        }
        if(b <= '9' && b >= '0'){
            b = reader.get(reader.indexOfWhiteSpaceOrComment() - 1);
            if(b == 'f'){
                return new SmaliValueFloat();
            }
            return new SmaliValueDouble();
        }
        return new SmaliValueKey();
    }

    public static SmaliValue createDefaultFor(TypeKey typeKey){
        SmaliValue smaliValue;
        if(typeKey.isTypeArray() || !typeKey.isPrimitive()) {
            smaliValue = new SmaliValueNull();
        }else if(TypeKey.TYPE_I.equals(typeKey)) {
            smaliValue = new SmaliValueInteger();
        } else if(TypeKey.TYPE_J.equals(typeKey)) {
            smaliValue = new SmaliValueLong();
        } else if(TypeKey.TYPE_D.equals(typeKey)) {
            smaliValue = new SmaliValueDouble();
        } else if(TypeKey.TYPE_F.equals(typeKey)) {
            smaliValue = new SmaliValueFloat();
        } else if(TypeKey.TYPE_S.equals(typeKey)) {
            smaliValue = new SmaliValueShort();
        } else if(TypeKey.TYPE_B.equals(typeKey)) {
            smaliValue = new SmaliValueByte();
        } else if(TypeKey.TYPE_C.equals(typeKey)) {
            smaliValue = new SmaliValueChar();
        } else if(TypeKey.TYPE_Z.equals(typeKey)) {
            smaliValue = new SmaliValueBoolean();
        }else {
            throw new IllegalArgumentException("Undefined: " + typeKey);
        }
        return smaliValue;
    }
}
