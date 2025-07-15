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
package com.reandroid.arsc.array;

import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.TypeString;

public class TypeStringArray extends StringArray<TypeString> {
    private int lastCreateIndex;
    public TypeStringArray(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        super(offsets, itemCount, itemStart, is_utf8);
    }
    @Override
    public TypeString newInstance() {
        TypeString typeString=new TypeString(isUtf8());
        //create default name
        this.lastCreateIndex ++;
        typeString.set("type-" + lastCreateIndex, false);
        return typeString;
    }
    @Override
    public TypeString[] newArrayInstance(int len) {
        return new TypeString[len];
    }
}
