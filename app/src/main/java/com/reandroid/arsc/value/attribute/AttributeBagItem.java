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
package com.reandroid.arsc.value.attribute;

import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.value.AttributeDataFormat;
import com.reandroid.arsc.value.AttributeType;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.utils.HexUtil;

import java.util.List;


public class AttributeBagItem {
    private final ResValueMap mBagItem;
    public AttributeBagItem(ResValueMap bagItem){
        this.mBagItem=bagItem;
    }
    public int getData(){
        return getBagItem().getData();
    }
    public String getNameOrHex(){
        String name = getName();
        if(name == null){
            name = ValueCoder.decodeUnknownNameId(getBagItem().getNameId());
        }
        return name;
    }
    public String getName(){
        if(isType()){
            return null;
        }
        ResourceEntry resourceEntry = getBagItem().resolveName();
        if(resourceEntry != null){
            return resourceEntry.getName();
        }
        return null;
    }
    public ResValueMap getBagItem() {
        return mBagItem;
    }
    public AttributeType getType(){
        return getBagItem().getAttributeType();
    }
    public boolean isFormats(){
        return getType() == AttributeType.FORMATS;
    }
    public boolean isType(){
        return getType() != null;
    }
    public boolean contains(AttributeDataFormat dataFormat){
        if(dataFormat == null || !isFormats()){
            return false;
        }
        return dataFormat.matches(getBagItem().getData());
    }
    public boolean isEqualType(AttributeDataFormat typeFormat){
        if(typeFormat == null || !isFormats()){
            return false;
        }
        return typeFormat.getMask() == getBagItem().getData();
    }
    public AttributeDataFormat[] getDataFormats(){
        if(!isFormats()){
            return null;
        }
        return AttributeDataFormat.decodeValueTypes(getBagItem().getData());
    }
    public boolean isEnum(){
        if(!isFormats()){
            return false;
        }
        return AttributeDataFormat.ENUM.matches(getBagItem().getData());
    }
    public boolean isFlag(){
        if(!isFormats()){
            return false;
        }
        return AttributeDataFormat.FLAG.matches(getBagItem().getData());
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        ResValueMap item=getBagItem();
        builder.append(getNameOrHex());
        builder.append("=").append(HexUtil.toHex8(item.getData()));
        return builder.toString();
    }

    public static String toString(AttributeBagItem[] bagItems)  {
        return toString(bagItems, false);
    }
    public static String toString(AttributeBagItem[] bagItems, boolean use_hex)  {
        if(bagItems==null){
            return null;
        }
        int len=bagItems.length;
        if(len==0){
            return null;
        }
        StringBuilder builder = new StringBuilder();
        boolean appendOnce=false;
        for (int i = 0; i < len; i++) {
            AttributeBagItem item = bagItems[i];
            if(item==null){
                continue;
            }
            if(appendOnce){
                builder.append("|");
            }
            String name;
            if(use_hex){
                name = item.getNameOrHex();
            }else {
                name = item.getName();
            }
            if(name == null){
                return null;
            }
            builder.append(name);
            appendOnce=true;
        }
        if(appendOnce){
            return builder.toString();
        }
        return null;
    }
    public static AttributeBagItem[] create(List<ResValueMap> resValueMaps){
        if(resValueMaps ==null){
            return null;
        }
        AttributeBagItem format=null;
        int size = resValueMaps.size();
        AttributeBagItem[] bagItems = new AttributeBagItem[size];
        for(int i = 0; i < size; i++){
            AttributeBagItem item = new AttributeBagItem(resValueMaps.get(i));
            bagItems[i] = item;
            if(format == null){
                if(AttributeType.FORMATS == item.getType()){
                    format = item;
                }
            }
        }
        if(format != null){
            return bagItems;
        }
        return null;
    }
}
