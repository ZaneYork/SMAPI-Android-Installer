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

import com.reandroid.dex.debug.DebugElementType;
import com.reandroid.dex.debug.DebugStartLocal;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.*;

import java.io.IOException;

public class SmaliDebugLocal extends SmaliDebugRegister{

    private StringKey name;
    private TypeKey type;
    private StringKey signature;

    public SmaliDebugLocal(){
        super();
    }

    public StringKey getName() {
        return name;
    }
    public void setName(StringKey name) {
        this.name = name;
    }
    public TypeKey getType() {
        return type;
    }
    public void setType(TypeKey type) {
        this.type = type;
    }

    public StringKey getSignature() {
        return signature;
    }
    public void setSignature(StringKey signature) {
        this.signature = signature;
    }
    public boolean isExtended(){
        return signature != null;
    }

    @Override
    public DebugElementType<? extends DebugStartLocal> getDebugElementType() {
        if(isExtended()){
            return DebugElementType.START_LOCAL_EXTENDED;
        }else {
            return DebugElementType.START_LOCAL;
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        super.append(writer);
        writer.append(", ");
        writer.appendOptional(getName());
        writer.append(':');
        writer.appendOptional(getType());
        StringKey signature = getSignature();
        if(signature != null){
            writer.append(", ");
            signature.append(writer);
        }
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        super.parse(reader);
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, ',');
        reader.skipWhitespacesOrComment();
        setName(StringKey.read(reader));
        reader.skipSpaces();
        SmaliParseException.expect(reader, ':');
        reader.skipSpaces();
        setType(TypeKey.read(reader));
        reader.skipSpaces();
        if(reader.get() == ','){
            reader.skip(1);
            reader.skipWhitespacesOrComment();
            setSignature(StringKey.read(reader));
        }
    }
}
