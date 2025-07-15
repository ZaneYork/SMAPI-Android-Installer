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
package com.reandroid.dex.common;

import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.StringsUtil;

import java.io.IOException;
import java.util.Iterator;

public abstract class Modifier implements SmaliFormat {

    private final int value;
    private final String name;
    private final int hash;

    public Modifier(int value, String name){
        this.value = value;
        this.name = name;
        this.hash = name.hashCode() + value * 31;
    }

    public int getValue() {
        return value;
    }
    public String getName() {
        return name;
    }
    public abstract boolean isSet(int value);

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getName());
        writer.append(' ');
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return hash;
    }
    @Override
    public String toString() {
        return getName();
    }

    public static void append(SmaliWriter writer, Modifier[] modifiers) throws IOException {
        if(modifiers == null){
            return;
        }
        for (Modifier modifier : modifiers) {
            if (modifier != null) {
                writer.append(modifier.getName());
                writer.append(' ');
            }
        }
    }
    public static boolean contains(Modifier[] modifiers, Modifier modifier) {
        if(modifiers == null || modifier == null){
            return false;
        }
        for (Modifier m : modifiers) {
            if (modifier == m) {
                return true;
            }
        }
        return false;
    }

    public static String toString(Iterator<? extends Modifier> iterator) {
        StringBuilder builder = new StringBuilder();
        while (iterator.hasNext()){
            Modifier modifier = iterator.next();
            builder.append(modifier.getName());
            builder.append(' ');
        }
        return builder.toString();
    }
    public static String toString(Modifier[] modifiers) {
        if(modifiers == null){
            return StringsUtil.EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        for(Modifier modifier : modifiers){
            if(modifier != null){
                builder.append(modifier.getName());
                builder.append(' ');
            }
        }
        return builder.toString();
    }
    public static int combineValues(Modifier[] modifiers){
        if(modifiers == null){
            return 0;
        }
        int result = 0;
        for(Modifier modifier : modifiers){
            if(modifier != null){
                result |= modifier.getValue();
            }
        }
        return result;
    }
}
