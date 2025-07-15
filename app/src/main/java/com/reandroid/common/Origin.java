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
package com.reandroid.common;

import com.reandroid.utils.StringsUtil;

public class Origin {

    private Origin parent;
    private final Object name;
    private Object separator = ":";

    public Origin(Origin parent, Object name) {
        this.parent = parent;
        this.name = name;
    }

    public Object getName() {
        return name;
    }
    public Origin getParent() {
        return parent;
    }
    public void setParent(Origin parent) {
        this.parent = parent;
    }

    public void setSeparator(Object separator) {
        this.separator = separator;
    }
    public Object getSeparator() {
        Object separator = this.separator;
        if(separator == null) {
            separator = StringsUtil.EMPTY;
        }
        return separator;
    }

    public boolean isRoot() {
        return getParent() == null;
    }
    public Origin[] toArray() {
        int count = 0;
        Origin source = this;
        while (!source.isRoot()) {
            count ++;
            source = source.getParent();
        }
        Origin[] result = new Origin[count];
        count = count - 1;
        source = this;
        while (count >= 0) {
            result[count] = source;
            source = source.getParent();
            count --;
        }
        return result;
    }

    public Origin createChild(Object name) {
        return new Origin(this, name);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Origin[] sources = toArray();
        int length = sources.length;
        for(int i = 0; i < length; i++) {
            Origin source = sources[i];
            if(i != 0) {
                builder.append(source.getSeparator());
            }
            builder.append(source.getName());
        }
        return builder.toString();
    }

    public static Origin newRoot() {
        return new Origin(null, StringsUtil.EMPTY);
    }
    public static Origin createNew(Object name) {
        return newRoot().createChild(name);
    }
}
