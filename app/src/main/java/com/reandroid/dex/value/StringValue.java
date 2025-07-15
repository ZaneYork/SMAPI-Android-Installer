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
package com.reandroid.dex.value;

import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.data.AnnotationItem;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.SectionType;

public class StringValue extends SectionIdValue<StringId> {

    public StringValue() {
        super(SectionType.STRING_ID, DexValueType.STRING);
    }

    @Override
    public StringKey getKey() {
        return (StringKey) super.getKey();
    }
    public String getString() {
        StringKey stringKey = getKey();
        if(stringKey != null) {
            return stringKey.getString();
        }
        return null;
    }
    public void setString(String value){
        setKey(StringKey.create(value));
    }

    @Override
    void updateUsageType(StringId stringId) {
        super.updateUsageType(stringId);
        if(stringId != null &&
                stringId.containsUsage(UsageMarker.USAGE_ANNOTATION) &&
                !stringId.containsUsage(UsageMarker.USAGE_SIGNATURE_TYPE)) {

            AnnotationItem annotationItem = getParentInstance(AnnotationItem.class);
            if(annotationItem != null &&
                    TypeKey.DALVIK_Signature.equals(annotationItem.getTypeKey())){
                stringId.addUsageType(UsageMarker.USAGE_SIGNATURE_TYPE);
            }
        }
    }

    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.STRING;
    }
    @Override
    public TypeKey getDataTypeKey() {
        return TypeKey.STRING;
    }
    @Override
    public String getData() {
        return getString();
    }
    @Override
    public void setData(Object data) {
        setString((String) data);
    }
}
