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
package com.reandroid.dex.smali;

import com.reandroid.utils.StringsUtil;

import java.io.IOException;

public class SmaliDirective implements SmaliFormat {

    private static final byte[] END_BYTES;
    public static final SmaliDirective CLASS;
    public static final SmaliDirective SUPER;
    public static final SmaliDirective SOURCE;
    public static final SmaliDirective IMPLEMENTS;
    public static final SmaliDirective ANNOTATION;
    public static final SmaliDirective SUB_ANNOTATION;
    public static final SmaliDirective FIELD;
    public static final SmaliDirective METHOD;
    public static final SmaliDirective ENUM;

    public static final SmaliDirective CATCH_ALL;
    public static final SmaliDirective CATCH;
    public static final SmaliDirective LOCALS;

    public static final SmaliDirective ARRAY_DATA;
    public static final SmaliDirective PACKED_SWITCH;
    public static final SmaliDirective SPARSE_SWITCH;
    public static final SmaliDirective PROLOGUE;
    public static final SmaliDirective PARAM;
    public static final SmaliDirective END_LOCAL;
    public static final SmaliDirective LOCAL;

    public static final SmaliDirective LINE;
    public static final SmaliDirective RESTART_LOCAL;
    public static final SmaliDirective EPILOGUE;
    public static final SmaliDirective SET_SOURCE_FILE;


    private static final SmaliDirective[] VALUES;

    static {

        END_BYTES = new byte[]{'.', 'e', 'n', 'd'};

        CLASS = new SmaliDirective("class");
        SUPER = new SmaliDirective("super");
        SOURCE = new SmaliDirective("source");
        IMPLEMENTS = new SmaliDirective("implements");
        ANNOTATION = new SmaliDirective("annotation");
        SUB_ANNOTATION = new SmaliDirective("subannotation");
        FIELD = new SmaliDirective("field");
        METHOD = new SmaliDirective("method");
        ENUM = new SmaliDirective("enum");

        LOCALS = new SmaliDirective("locals");
        CATCH = new SmaliDirective("catch", true);
        CATCH_ALL = new SmaliDirective("catchall", true);
        ARRAY_DATA = new SmaliDirective("array-data", true);
        PACKED_SWITCH = new SmaliDirective("packed-switch", true);
        SPARSE_SWITCH = new SmaliDirective("sparse-switch", true);

        LINE = new SmaliDirective("line", true);
        RESTART_LOCAL = new SmaliDirective("restart local", true);

        PROLOGUE = new SmaliDirective("prologue", true);
        PARAM = new SmaliDirective("param", true);
        END_LOCAL = new SmaliDirective("end local", true);
        LOCAL = new SmaliDirective("local", true);
        EPILOGUE = new SmaliDirective("epilogue", true);
        SET_SOURCE_FILE = new SmaliDirective("set source file", true);


        VALUES = new SmaliDirective[]{
                CLASS,
                SUPER,
                SOURCE,
                IMPLEMENTS,
                ANNOTATION,
                SUB_ANNOTATION,
                FIELD,
                METHOD,
                ENUM,
                LOCALS,
                CATCH_ALL,
                CATCH,
                ARRAY_DATA,
                PACKED_SWITCH,
                SPARSE_SWITCH,
                LINE,
                RESTART_LOCAL,
                EPILOGUE,
                SET_SOURCE_FILE,
                PROLOGUE,
                PARAM,
                END_LOCAL,
                LOCAL
        };
    }

    private final String name;
    private final byte[] nameBytes;
    private final boolean methodCode;

    SmaliDirective(String name, byte[] nameBytes, boolean methodCode){
        this.name = name;
        this.nameBytes = nameBytes;
        this.methodCode = methodCode;
    }
    SmaliDirective(String name, boolean methodCode){
        this(name, StringsUtil.getASCII(name), methodCode);
    }
    SmaliDirective(String name){
        this(name, StringsUtil.getASCII(name), false);
    }

    public String getName() {
        return name;
    }
    public boolean isMethodCode() {
        return methodCode;
    }

    public boolean is(SmaliDirective smaliDirective) {
        return smaliDirective == this;
    }
    boolean readMatches(SmaliReader reader){
        int length = reader.startsWithSqueezeSpaces(nameBytes);
        if(length > 0){
            reader.skip(length);
            return true;
        }
        return false;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append('.');
        writer.append(getName());
        writer.append(' ');
    }
    public void appendEnd(SmaliWriter writer) throws IOException {
        writer.newLine();
        writer.append(".end ");
        writer.append(getName());
    }

    public boolean isEnd(SmaliReader reader){
        if(!reader.startsWith(END_BYTES)){
            return false;
        }
        return parse(reader, false) == this;
    }
    public boolean skipEnd(SmaliReader reader){
        reader.skipWhitespaces();
        if(!reader.startsWith(END_BYTES)){
            return false;
        }
        int position = reader.position();
        if(parse(reader, true) == this){
            return true;
        }
        reader.position(position);
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "." + getName();
    }
    public String toString(boolean end) {
        if(end){
            return ".end " + getName();
        }
        return "." + getName();
    }

    public static SmaliDirective parse(SmaliReader reader){
        return parse(reader, true);
    }
    public static SmaliDirective parse(SmaliReader reader, boolean skip){
        if(reader == null){
            return null;
        }
        int position = reader.position();
        reader.skipWhitespaces();
        if(reader.finished()){
            return null;
        }
        if(reader.get() != '.'){
            reader.position(position);
            return null;
        }
        if(reader.startsWith(END_BYTES)){
            reader.skip(1);
            if(END_LOCAL.readMatches(reader)){
                if(!skip){
                    reader.position(position);
                }
                return END_LOCAL;
            }
            reader.skip(END_BYTES.length - 1);
            reader.skipWhitespaces();
        }else {
            reader.skip(1);
        }
        SmaliDirective directive = directiveOf(reader);
        if(!skip){
            reader.position(position);
        }
        return directive;
    }
    private static SmaliDirective directiveOf(SmaliReader reader){
        if(!reader.finished()) {
            for(SmaliDirective smaliDirective : VALUES){
                if(smaliDirective.readMatches(reader)){
                    return smaliDirective;
                }
            }
        }
        return null;
    }

}
