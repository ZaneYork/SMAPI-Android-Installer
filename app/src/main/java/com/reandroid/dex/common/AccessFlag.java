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

import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.ArrayIterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class AccessFlag extends Modifier{

    public static final AccessFlag PUBLIC;
    public static final AccessFlag PRIVATE;
    public static final AccessFlag PROTECTED;
    public static final AccessFlag STATIC;
    public static final AccessFlag FINAL;
    public static final AccessFlag SYNCHRONIZED;
    public static final AccessFlag VOLATILE;
    public static final AccessFlag BRIDGE;
    public static final AccessFlag TRANSIENT;
    public static final AccessFlag VARARGS;
    public static final AccessFlag NATIVE;
    public static final AccessFlag INTERFACE;
    public static final AccessFlag ABSTRACT;
    public static final AccessFlag STRICTFP;
    public static final AccessFlag SYNTHETIC;
    public static final AccessFlag ANNOTATION;
    public static final AccessFlag ENUM;
    public static final AccessFlag CONSTRUCTOR;
    public static final AccessFlag DECLARED_SYNCHRONIZED;

    private static final AccessFlag[] VALUES;

    private static final HashMap<String, AccessFlag> accessFlagsByName;

    static {

        PUBLIC = new AccessFlag(0x1, "public", true, true, true);
        PRIVATE = new AccessFlag(0x2, "private", true, true, true);
        PROTECTED = new AccessFlag(0x4, "protected", true, true, true);
        STATIC = new AccessFlag(0x8, "static", true, true, true);
        FINAL = new AccessFlag(0x10, "final", true, true, true);
        SYNCHRONIZED = new AccessFlag(0x20, "synchronized", false, true, false);
        VOLATILE = new AccessFlag(0x40, "volatile", false, false, true);
        BRIDGE = new AccessFlag(0x40, "bridge", false, true, false);
        TRANSIENT = new AccessFlag(0x80, "transient", false, false, true);
        VARARGS = new AccessFlag(0x80, "varargs", false, true, false);
        NATIVE = new AccessFlag(0x100, "native", false, true, false);
        INTERFACE = new AccessFlag(0x200, "interface", true, false, false);
        ABSTRACT = new AccessFlag(0x400, "abstract", true, true, false);
        STRICTFP = new AccessFlag(0x800, "strictfp", false, true, false);
        SYNTHETIC = new AccessFlag(0x1000, "synthetic", true, true, true);
        ANNOTATION = new AccessFlag(0x2000, "annotation", true, false, false);
        ENUM = new AccessFlag(0x4000, "enum", true, false, true);
        CONSTRUCTOR = new AccessFlag(0x10000, "constructor", false, true, false);
        DECLARED_SYNCHRONIZED = new AccessFlag(0x20000, "declared-synchronized", false, true, false);

        VALUES = new AccessFlag[]{
                PUBLIC,
                PRIVATE,
                PROTECTED,
                STATIC,
                FINAL,
                SYNCHRONIZED,
                VOLATILE,
                BRIDGE,
                TRANSIENT,
                VARARGS,
                NATIVE,
                INTERFACE,
                ABSTRACT,
                STRICTFP,
                SYNTHETIC,
                ANNOTATION,
                ENUM,
                CONSTRUCTOR,
                DECLARED_SYNCHRONIZED
        };

        accessFlagsByName = new HashMap<>();
        for (AccessFlag accessFlag : VALUES) {
            accessFlagsByName.put(accessFlag.getName(), accessFlag);
        }

    }

    private final boolean validForClass;
    private final boolean validForMethod;
    private final boolean validForField;

    private AccessFlag(int value, String name, boolean validForClass, boolean validForMethod,
                       boolean validForField) {
        super(value, name);
        this.validForClass = validForClass;
        this.validForMethod = validForMethod;
        this.validForField = validForField;
    }

    @Override
    public boolean isSet(int accessFlags) {
        return (getValue() & accessFlags) != 0;
    }
    private boolean isSetForField(int value) {
        return validForField && (getValue() & value) != 0;
    }
    private boolean isSetForMethod(int value) {
        return validForMethod && (getValue() & value) != 0;
    }
    private boolean isSetForClass(int value) {
        return validForClass && (getValue() & value) != 0;
    }

    public static Iterator<AccessFlag> valuesOfClass(int value) {
        return getValues(accessFlag -> accessFlag.isSetForClass(value));
    }
    public static Iterator<AccessFlag> valuesOfMethod(int value) {
        return getValues(accessFlag -> accessFlag.isSetForMethod(value));
    }
    public static Iterator<AccessFlag> valuesOfField(int value) {
        return getValues(accessFlag -> accessFlag.isSetForField(value));
    }
    public static AccessFlag valueOf(String name) {
        return accessFlagsByName.get(name);
    }
    public static Iterator<AccessFlag> getValues(){
        return getValues(null);
    }
    public static Iterator<AccessFlag> getValues(Predicate<AccessFlag> filter){
        return new ArrayIterator<>(VALUES, filter);
    }
    public static AccessFlag[] parse(SmaliReader reader){
        List<AccessFlag> accessFlags = new ArrayCollection<>();
        AccessFlag flag;
        while ((flag = parseNext(reader)) != null){
            accessFlags.add(flag);
        }
        int size = accessFlags.size();
        if(size == 0){
            return null;
        }
        reader.skipWhitespaces();
        return accessFlags.toArray(new AccessFlag[size]);
    }
    private static AccessFlag parseNext(SmaliReader reader){
        reader.skipWhitespaces();
        int i = reader.indexOf(' ');
        if(i < 0){
            return null;
        }
        int position = reader.position();
        AccessFlag accessFlag = valueOf(reader.readString(i - reader.position()));
        if(accessFlag == null){
            reader.position(position);
        }
        return accessFlag;
    }
}
