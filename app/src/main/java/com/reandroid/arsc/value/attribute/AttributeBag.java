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

import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.value.*;

public class AttributeBag {
    private final AttributeBagItem[] mBagItems;
    public AttributeBag(AttributeBagItem[] bagItems){
        this.mBagItems=bagItems;
    }

    public boolean contains(AttributeDataFormat valueType){
        return getFormat().contains(valueType);
    }
    public boolean isEqualType(AttributeDataFormat valueType){
        return getFormat().isEqualType(valueType);
    }
    public boolean isCompatible(ValueType valueType){
        return AttributeDataFormat.contains(getFormats(), valueType);
    }
    public EncodeResult encode(String valueString){
        EncodeResult encodeResult = encodeEnumOrFlagValue(valueString);
        if(encodeResult != null){
            return encodeResult;
        }
        AttributeDataFormat[] formats = getFormats();
        if(formats != null){
            encodeResult = ValueCoder.encode(valueString, formats);
            if(encodeResult != null){
                return encodeResult;
            }
        }
        if(isCompatible(ValueType.STRING)){
            return new EncodeResult(ValueType.STRING, -1);
        }
        if(isEnumOrFlag()){
            return new EncodeResult("Invalid attribute enum/flag/value");
        }
        return new EncodeResult("Incompatible attribute value, expected formats "
                + AttributeDataFormat.toString(formats));
    }
    public EncodeResult encodeEnumOrFlagValue(String valueString){
        if(valueString == null || !isEnumOrFlag()){
            return null;
        }
        EncodeResult encodeResult = ValueCoder
                .encode(valueString, AttributeDataFormat.INTEGER);
        if(encodeResult != null){
            // Could be decoded as hex or integer
            return encodeResult;
        }
        int value = 0;
        boolean foundOnce = false;
        String[] splitNames = valueString.split("[\\s|]+");
        for(String name : splitNames){
            name = name.trim();
            AttributeBagItem item = searchByName(name);
            if(item == null){
                if(name.length() != 0){
                    return null;
                }
                continue;
            }
            value |= item.getBagItem().getData();
            foundOnce = true;
        }
        if(!foundOnce){
            return null;
        }
        ValueType valueType = isFlag() ? ValueType.HEX : ValueType.DEC;
        return new EncodeResult(valueType, value);
    }
    public String decodeAttributeValue(int attrValue){
        AttributeBagItem[] bagItems=searchValue(attrValue);
        return AttributeBagItem.toString(bagItems, false);
    }
    public AttributeBagItem searchByName(String entryName){
        AttributeBagItem[] bagItems= getBagItems();
        for(AttributeBagItem item:bagItems){
            if(item.isType()){
                continue;
            }
            if(entryName.equals(item.getNameOrHex())){
                return item;
            }
        }
        return null;
    }
    public AttributeBagItem[] searchValue(int attrValue){
        if(isFlag()){
            return searchFlagValue(attrValue);
        }
        AttributeBagItem item = searchEnumValue(attrValue);
        if(item != null){
            return new AttributeBagItem[]{item};
        }
        return null;
    }
    private AttributeBagItem searchEnumValue(int attrValue){
        AttributeBagItem[] bagItems= getBagItems();
        for(AttributeBagItem item:bagItems){
            if(item.isType()){
                continue;
            }
            int data=item.getData();
            if(attrValue==data){
                return item;
            }
        }
        return null;
    }

    private AttributeBagItem[] searchFlagValue(int attrValue){
        AttributeBagItem[] bagItems= getBagItems();
        int len=bagItems.length;
        AttributeBagItem[] foundBags = new AttributeBagItem[len];
        for(int i=0;i<len;i++){
            AttributeBagItem item=bagItems[i];
            if(item.isType()){
                continue;
            }
            int data = item.getData();
            if ((attrValue & data) != data) {
                continue;
            }
            if(attrValue == data){
                return new AttributeBagItem[]{item};
            }
            int index = indexOf(foundBags, data);
            if (index >= 0) {
                foundBags[index] = item;
            }
        }
        return removeNull(foundBags);
    }

    private int indexOf(AttributeBagItem[] foundFlag, int data) {
        for (int i = 0; i < foundFlag.length; i++) {
            AttributeBagItem item=foundFlag[i];
            if(item==null){
                return i;
            }
            int flag=item.getData();
            if(flag==0){
                return i;
            }
            if ((flag & data) == data) {
                return -1;
            }
            if ((flag & data) == flag) {
                return i;
            }
        }
        return -1;
    }

    private AttributeBagItem[] removeNull(AttributeBagItem[] bagItems){
        int count=countNonNull(bagItems);
        if(count==0){
            return null;
        }
        AttributeBagItem[] results=new AttributeBagItem[count];
        int index=0;
        int len=bagItems.length;
        for(int i=0;i<len;i++){
            AttributeBagItem item=bagItems[i];
            if(item!=null){
                results[index]=item;
                index++;
            }
        }
        return results;
    }
    private int countNonNull(AttributeBagItem[] bagItems) {
        if(bagItems==null){
            return 0;
        }
        int result=0;
        int len=bagItems.length;
        for (int i = 0; i < len; i++) {
            if(bagItems[i]!=null){
                result++;
            }
        }
        return result;
    }
    public AttributeBagItem[] getBagItems(){
        return mBagItems;
    }

    public AttributeDataFormat[] getFormats(){
        return getFormat().getDataFormats();
    }
    public AttributeBagItem getFormat(){
        AttributeBagItem item = find(AttributeType.FORMATS);
        if(item == null){
            item = getBagItems()[0];
        }
        return item;
    }
    public AttributeBagItem find(AttributeType attributeType){
        for(AttributeBagItem bagItem : getBagItems()){
            if(bagItem.getType() == attributeType){
                return bagItem;
            }
        }
        return null;
    }
    public boolean isEnumOrFlag(){
        return isFlag() || isEnum();
    }
    public boolean isFlag(){
        return getFormat().isFlag();
    }
    public boolean isEnum(){
        return getFormat().isEnum();
    }

    @Override
    public String toString(){
        AttributeBagItem[] bagItems= getBagItems();
        StringBuilder builder=new StringBuilder();
        AttributeBagItem format=getFormat();
        Entry entry =format.getBagItem().getEntry();
        if(entry !=null){
            builder.append(entry.getSpecString());
        }
        int len=bagItems.length;
        builder.append(", childes=").append(len);
        for(int i=0;i<len;i++){
            AttributeBagItem item = bagItems[i];
            builder.append("\n    [").append((i+1)).append("]  ");
            builder.append(item.toString());
        }
        return builder.toString();
    }

    public static AttributeBag create(Entry entry){
        if(entry != null){
            return create(entry.getResValueMapArray());
        }
        return null;
    }
    public static AttributeBag create(ResValueMapArray resValueMapArray){
        if(resValueMapArray==null || resValueMapArray.size() == 0){
            return null;
        }
        AttributeBagItem[] bagItems = AttributeBagItem.create(resValueMapArray.getChildes());
        if(bagItems == null){
            return null;
        }
        return new AttributeBag(bagItems);
    }
    public static boolean isAttribute(ResTableMapEntry mapEntry){
        if(mapEntry==null){
            return false;
        }
        AttributeBagItem[] bagItems=AttributeBagItem.create(mapEntry.listResValueMap());
        return bagItems!=null;
    }

    public static final short TYPE_ENUM = 0x0001;
    public static final short TYPE_FLAG = 0x0002;
}
