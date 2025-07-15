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
import com.reandroid.utils.StringsUtil;

public class AnnotationVisibility extends Modifier{

    public static final AnnotationVisibility BUILD;
    public static final AnnotationVisibility RUNTIME;
    public static final AnnotationVisibility SYSTEM;

    private static final AnnotationVisibility[] VALUES;

    static {

        BUILD = new AnnotationVisibility(0, "build");
        RUNTIME = new AnnotationVisibility(1, "runtime");
        SYSTEM = new AnnotationVisibility(2, "system");
        VALUES = new AnnotationVisibility[]{
                BUILD,
                RUNTIME,
                SYSTEM
        };

    }

    private AnnotationVisibility(int value, String name){
        super(value, name);
    }

    @Override
    public boolean isSet(int value) {
        return value == this.getValue();
    }

    public static AnnotationVisibility valueOf(int visibility) {
        if (visibility < 0 || visibility >= VALUES.length) {
            return null;
        }
        return VALUES[visibility];
    }
    public static AnnotationVisibility valueOf(String visibility) {
        visibility = StringsUtil.toLowercase(visibility);
        if (visibility.equals("build")) {
            return BUILD;
        }
        if (visibility.equals("runtime")) {
            return RUNTIME;
        }
        if (visibility.equals("system")) {
            return SYSTEM;
        }
        return null;
    }
    public static AnnotationVisibility parse(SmaliReader reader){
        reader.skipSpaces();
        int position = reader.position();
        int length = reader.indexOfWhiteSpace() - position;
        AnnotationVisibility visibility = valueOf(reader.readString(length));
        if(visibility == null){
            reader.position(position);
        }
        return visibility;
    }
}
