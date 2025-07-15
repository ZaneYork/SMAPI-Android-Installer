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
package com.reandroid.dex.data;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.smali.model.SmaliField;

import java.util.Iterator;

public class FieldDefArray extends DefArray<FieldDef> {

    public FieldDefArray(IntegerReference itemCount){
        super(itemCount, CREATOR);
    }

    @Override
    void sortAnnotations(){
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory != null){
            directory.sortFields();
        }
    }
    public void fromSmali(Iterator<SmaliField> iterator){
        while (iterator.hasNext()) {
            SmaliField smaliField = iterator.next();
            FieldDef fieldDef = createNext();
            fieldDef.fromSmali(smaliField);
        }
    }
    public void fromSmali(SmaliField smaliField){
        FieldDef fieldDef = createNext();
        fieldDef.fromSmali(smaliField);
    }

    private static final Creator<FieldDef> CREATOR = new Creator<FieldDef>() {
        @Override
        public FieldDef[] newArrayInstance(int length) {
            return new FieldDef[length];
        }
        @Override
        public FieldDef newInstance() {
            return new FieldDef();
        }
    };
}
