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
package com.reandroid.arsc.pool;

import com.reandroid.arsc.array.OffsetArray;
import com.reandroid.arsc.array.ResXmlStringArray;
import com.reandroid.arsc.array.StringArray;
import com.reandroid.arsc.array.StyleArray;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlIDMap;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ResXmlID;
import com.reandroid.arsc.item.ResXmlString;
import com.reandroid.utils.NumbersUtil;
import com.reandroid.xml.StyleDocument;


public class ResXmlStringPool extends StringPool<ResXmlString> {

    public ResXmlStringPool(boolean is_utf8) {
        super(is_utf8, false);
    }

    @Override
    StringArray<ResXmlString> newInstance(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        return new ResXmlStringArray(offsets, itemCount, itemStart, is_utf8);
    }

    @Override
    public ResXmlString getOrCreate(String str){
        return getOrCreate(0, str);
    }
    @Override
    public ResXmlString getOrCreate(StyleDocument styleDocument) {
        String xml = styleDocument.getXml();
        if(!styleDocument.hasElements()) {
            return getOrCreate(0, xml);
        }
        ResXmlString xmlString = get(xml, resXmlString -> {
            if(!xml.equals(resXmlString.getXml()) ||
                    resXmlString.hasResourceId()) {
                return false;
            }
            return resXmlString.hasStyle();
        });
        if(xmlString == null) {
            xmlString = createNewString();
            xmlString.set(styleDocument);
        }
        return xmlString;
    }

    public ResXmlString getOrCreate(int resourceId, String str){
        ResXmlString xmlString = get(str, resXmlString -> {
            if(!str.equals(resXmlString.getXml()) || resXmlString.hasStyle()) {
                return false;
            }
            return (resourceId == 0) == (resXmlString.getResourceId() == 0);
        });
        if(xmlString == null) {
            xmlString = createNewString(str);
            xmlString.setResourceId(resourceId);
        }
        return xmlString;
    }
    private ResXmlIDMap getResXmlIDMap(){
        ResXmlDocument resXmlDocument = getParentInstance(ResXmlDocument.class);
        if(resXmlDocument != null){
            return resXmlDocument.getResXmlIDMap();
        }
        return null;
    }

    public void linkResXmlIDMapInternal() {
        ResXmlIDMap resXmlIDMap = getResXmlIDMap();
        if(resXmlIDMap == null) {
            return;
        }
        StringArray<ResXmlString> stringsArray = getStringsArray();
        int size = NumbersUtil.min(resXmlIDMap.size(), stringsArray.size());
        for(int i = 0; i < size; i++) {
            ResXmlString resXmlString = stringsArray.get(i);
            ResXmlID xmlID = resXmlIDMap.get(i);
            resXmlString.linkResourceIdInternal(xmlID);
        }
    }

    @Override
    public void onChunkLoaded() {
        super.onChunkLoaded();
        linkResXmlIDMapInternal();
        StyleArray styleArray = getStyleArray();
        if(styleArray.size()>0){
            notifyResXmlStringPoolHasStyles(styleArray.size());
        }
    }
    private static void notifyResXmlStringPoolHasStyles(int styleArrayCount){
        if(HAS_STYLE_NOTIFIED){
            return;
        }
        String msg="Not expecting ResXmlStringPool to have styles count="
                +styleArrayCount+",\n please create issue along with this apk/file on https://github.com/REAndroid/ARSCLib";
        System.err.println(msg);
        HAS_STYLE_NOTIFIED=true;
    }
    private static boolean HAS_STYLE_NOTIFIED;
}
