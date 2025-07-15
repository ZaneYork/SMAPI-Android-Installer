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
package com.reandroid.dex.ins;

import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public interface Label extends ExtraLine{
    int getAddress();
    String getLabelName();
    @Override
    default void appendExtra(SmaliWriter writer) throws IOException {
        writer.appendLabelName(getLabelName());
    }
    @Override
    default boolean isEqualExtraLine(Object obj) {
        if(obj == this){
            return true;
        }
        if(!(obj instanceof Label)){
            return false;
        }
        Label label = (Label) obj;
        return getLabelName().equals(label.getLabelName());
    }
    @Override
    default int compareExtraLine(ExtraLine other) {
        int i = ExtraLine.super.compareExtraLine(other);
        if(i != 0){
            return i;
        }
        if(!(other instanceof Label)){
            return 0;
        }
        Label label = (Label) other;
        return getLabelName().compareTo(label.getLabelName());
    }
}
