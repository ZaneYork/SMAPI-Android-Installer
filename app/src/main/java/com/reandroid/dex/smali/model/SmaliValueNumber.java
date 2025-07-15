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

import com.reandroid.dex.key.PrimitiveKey;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;

import java.io.IOException;

public abstract class SmaliValueNumber<T extends Number> extends SmaliValue{

    public SmaliValueNumber(){
        super();
    }
    public abstract T getNumber();
    public abstract void setNumber(T number);
    public abstract int getWidth();
    public abstract int unsignedInt();
    public abstract long unsignedLong();
    public abstract PrimitiveKey getKey();

    @SuppressWarnings("unchecked")
    public static<T1 extends Number> SmaliValueNumber<T1> createFor(T1 number){
        return (SmaliValueNumber<T1>) createUnchecked(number);
    }
    private static SmaliValueNumber<?> createUnchecked(Number number){
        if(number == null){
            throw new NullPointerException();
        }
        if(number instanceof Byte){
            return new SmaliValueByte((Byte) number);
        }
        if(number instanceof Short){
            return new SmaliValueShort((Short) number);
        }
        if(number instanceof Integer){
            return new SmaliValueInteger((Integer) number);
        }
        if(number instanceof Long){
            return new SmaliValueLong((Long) number);
        }
        if(number instanceof Float){
            return new SmaliValueFloat((Float) number);
        }
        if(number instanceof Double){
            return new SmaliValueDouble((Double) number);
        }
        throw new RuntimeException("Unrecognized number class: " + number.getClass());
    }

    public static SmaliValueNumber<?> createNumber(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        int position = reader.position();
        byte b = reader.get(position);
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
        throw new SmaliParseException("Unrecognized number format", reader);
    }
}
