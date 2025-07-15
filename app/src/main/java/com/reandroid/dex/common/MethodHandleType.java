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
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MethodHandleType implements Comparable<MethodHandleType>, SmaliFormat {

    public static final MethodHandleType STATIC_PUT;
    public static final MethodHandleType STATIC_GET;
    public static final MethodHandleType INSTANCE_PUT;
    public static final MethodHandleType INSTANCE_GET;
    public static final MethodHandleType INVOKE_STATIC;
    public static final MethodHandleType INVOKE_INSTANCE;
    public static final MethodHandleType INVOKE_CONSTRUCTOR;
    public static final MethodHandleType INVOKE_DIRECT;
    public static final MethodHandleType INVOKE_INTERFACE;

    private static final MethodHandleType[] VALUES;
    private static final Map<String, MethodHandleType> nameMap;

    static {

        STATIC_PUT = new MethodHandleType(0, "static-put", true);
        STATIC_GET = new MethodHandleType(1, "static-get", true);
        INSTANCE_PUT = new MethodHandleType(2, "instance-put", true);
        INSTANCE_GET = new MethodHandleType(3, "instance-get", true);
        INVOKE_STATIC = new MethodHandleType(4, "invoke-static", false);
        INVOKE_INSTANCE = new MethodHandleType(5, "invoke-instance", false);
        INVOKE_CONSTRUCTOR = new MethodHandleType(6, "invoke-constructor", false);
        INVOKE_DIRECT = new MethodHandleType(7, "invoke-direct", false);
        INVOKE_INTERFACE = new MethodHandleType(8, "invoke-interface", false);

        MethodHandleType [] values = new MethodHandleType[] {
                STATIC_PUT,
                STATIC_GET,
                INSTANCE_PUT,
                INSTANCE_GET,
                INVOKE_STATIC,
                INVOKE_INSTANCE,
                INVOKE_CONSTRUCTOR,
                INVOKE_DIRECT,
                INVOKE_INTERFACE
        };
        VALUES = values;
        int length = values.length;
        Map<String, MethodHandleType> map = new HashMap<>(length);
        nameMap = map;
        for (int i = 0; i < length; i++) {
            MethodHandleType type = values[i];
            map.put(type.name, type);
        }
    }

    private final int type;
    private final String name;
    private final boolean field;

    private MethodHandleType(int type, String name, boolean field) {
        this.type = type;
        this.name = name;
        this.field = field;
    }

    public String name() {
        return name;
    }
    public int type() {
        return type;
    }

    public boolean isField() {
        return field;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(name());
    }

    @Override
    public int compareTo(MethodHandleType handleType) {
        if (handleType == this) {
            return 0;
        }
        if (handleType == null) {
            return -1;
        }
        return CompareUtil.compare(this.type(), handleType.type());
    }
    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return type;
    }

    @Override
    public String toString() {
        return name;
    }

    public static MethodHandleType valueOf(int type) {
        if (type >= 0 && type < 9) {
            return VALUES[type];
        }
        return null;
    }
    public static MethodHandleType valueOf(String name) {
        return nameMap.get(name);
    }

    public static MethodHandleType read(SmaliReader reader) {
        reader.skipWhitespaces();
        int position = reader.position();
        char c = reader.getASCII(position);
        if (c != 's' && c != 'i') {
            return null;
        }
        int i = reader.indexOfBeforeLineEnd('@');
        if (i < 0) {
            return null;
        }
        MethodHandleType type = valueOf(reader.readString(i - position).trim());
        if (type == null) {
            reader.position(position);
        }
        return type;
    }
}
