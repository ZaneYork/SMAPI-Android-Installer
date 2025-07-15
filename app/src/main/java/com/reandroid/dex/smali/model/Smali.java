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

import com.reandroid.common.Origin;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliParser;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public abstract class Smali implements SmaliFormat, SmaliParser {

    private Smali parent;
    private Origin origin;

    public Smali(){
    }

    public Origin getOrigin() {
        return origin;
    }
    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public Smali getParent() {
        return parent;
    }
    @SuppressWarnings("unchecked")
    public<T extends Smali> T getParent(Class<T> parentClass){
        Smali parent = getParent();
        if(parent == null){
            return null;
        }
        if(parent.getClass() == parentClass){
            return (T) parent;
        }
        return parent.getParent(parentClass);
    }
    @SuppressWarnings("unchecked")
    public<T extends Smali> T getParentInstance(Class<T> parentClass){
        Smali parent = getParent();
        if(parent == null){
            return null;
        }
        if(parentClass.isInstance(parent)){
            return (T) parent;
        }
        return parent.getParentInstance(parentClass);
    }

    void setParent(Smali parent) {
        if(parent == this){
            throw new RuntimeException("Cyclic parent set");
        }
        this.parent = parent;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {

    }

    public String toDebugString(){
        Origin origin = getOrigin();
        if(origin != null) {
            return origin.toString();
        }
        try{
            return toString();
        }catch (Throwable e){
            return e.getMessage();
        }
    }
    @Override
    public String toString() {
        return SmaliWriter.toStringSafe(this);
    }
}
