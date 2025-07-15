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

import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlIDMap;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ResXmlID;
import com.reandroid.arsc.item.ResXmlString;
import com.reandroid.utils.ObjectsUtil;

import java.util.Comparator;

public class ResXmlStringArray extends StringArray<ResXmlString> {

    public ResXmlStringArray(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        super(offsets, itemCount, itemStart, is_utf8);
    }

    @Override
    public void add(int index, ResXmlString item) {
        ResXmlIDArray xmlIDMap = getResXmlIDMap().getResXmlIDArray();
        if(index < xmlIDMap.size() - 1) {
            xmlIDMap.add(index, new ResXmlID());
        }
        super.add(index, item);
    }
    private ResXmlIDMap getResXmlIDMap(){
        ResXmlDocument xmlDocument = getParentInstance(ResXmlDocument.class);
        if(xmlDocument != null){
            return xmlDocument.getResXmlIDMap();
        }
        return ObjectsUtil.cast(null);
    }
    @Override
    public ResXmlString newInstance() {
        return new ResXmlString(isUtf8());
    }
    @Override
    public ResXmlString[] newArrayInstance(int length) {
        return new ResXmlString[length];
    }

    @Override
    public boolean sort(Comparator<? super ResXmlString> comparator) {
        boolean sorted = super.sort(comparator);
        getResXmlIDMap().getResXmlIDArray().sort();
        return sorted;
    }
    @Override
    protected void onPreRefresh() {
        sort();
        super.onPreRefresh();
    }
}
