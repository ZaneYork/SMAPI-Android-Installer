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
package com.reandroid.arsc.value;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.utils.HexUtil;
import com.reandroid.json.JSONObject;
import com.reandroid.xml.XMLAttribute;
import com.reandroid.xml.XMLElement;
import com.reandroid.xml.XMLUtil;

public class ResValueMap extends AttributeValue implements Comparable<ResValueMap>{

    public ResValueMap() {
        super(12, OFFSET_SIZE);
    }

    public void setArrayIndex(){
        setArrayIndex(getIndex() + 1);
    }
    public void setArrayIndex(int index){
        setNameId(0x01000000 | index);
    }
    public int getArrayIndex(){
        int name = getNameId();
        int high = name & 0xffff0000;
        if(high != 0x01000000 && high != 0x02000000){
            return -1;
        }
        return name & 0xffff;
    }
    public EncodeResult encodeStyle(XMLElement xmlElement){
        return encodeStyle(false, xmlElement);
    }
    public EncodeResult encodeStyle(boolean validate, XMLElement xmlElement){
        XMLAttribute xmlAttribute = xmlElement.getAttribute("name");
        if(xmlAttribute == null){
            return new EncodeResult("Missing attribute name");
        }
        return encodeStyle(validate, xmlAttribute.getPrefix(),
                xmlAttribute.getValueAsString(),
                xmlElement.getTextContent());
    }
    public EncodeResult encodeStyle(boolean validate, String name, String value){
        return encodeStyle(validate, XMLUtil.splitPrefix(name), XMLUtil.splitName(name), value);
    }
    public EncodeResult encodeStyle(String name, String value){
        return encodeStyle(false, XMLUtil.splitPrefix(name), XMLUtil.splitName(name), value);
    }
    public EncodeResult encodeStyle(String prefix, String name, String value){
        return encodeStyle(false, prefix, name, value);
    }
    public EncodeResult encodeStyle(boolean validate, String prefix, String name, String value){
        ResourceEntry nameEntry = super.encodeAttrName(prefix, name);
        if(nameEntry == null){
            return new EncodeResult("Unknown attribute name");
        }
        return super.encodeStyleValue(validate, nameEntry, value);
    }
    @Override
    boolean allowNullPrefixEncode(){
        return true;
    }
    @Override
    public String decodeName(boolean includePrefix){
        int resourceId = getNameId();
        if(!PackageBlock.isResourceId(resourceId)){
            if(resourceId != 0 && getAttributeType() == null){
                return ValueCoder.decodeUnknownNameId(resourceId);
            }
            return null;
        }
        ResourceEntry resourceEntry = resolve(resourceId);
        if(resourceEntry == null || !resourceEntry.isDeclared()){
            return ValueCoder.decodeUnknownNameId(resourceId);
        }
        String name = resourceEntry.getName();
        if(includePrefix && resourceEntry.getPackageBlock() != getPackageBlock()){
            String prefix = resourceEntry.getPackageName();
            if(prefix != null){
                name = prefix + ":" + name;
            }
        }
        return name;
    }
    @Override
    String decodeDataAsAttrFormats(){
        AttributeType attributeType = getAttributeType();
        if(attributeType != AttributeType.FORMATS){
            return null;
        }
        int data = getData() & 0x00ff;
        if(data == 0){
            return "";
        }
        return AttributeDataFormat.toString(
                AttributeDataFormat.decodeValueTypes(data));
    }
    @Override
    public String decodePrefix(){
        ResourceEntry resourceEntry = resolveName();
        if(resourceEntry == null || getPackageBlock() == resourceEntry.getPackageBlock()){
            return null;
        }
        return resourceEntry.getPackageName();
    }
    public AttributeType getAttributeType(){
        return AttributeType.valueOf(getNameId());
    }
    public void setAttributeType(AttributeType attributeType){
        setNameId(attributeType.getId());
        if(attributeType == AttributeType.FORMATS && getValueType() == ValueType.NULL){
            setValueType(ValueType.DEC);
        }
    }
    public AttributeDataFormat[] getAttributeTypeFormats(){
        AttributeType attributeType = getAttributeType();
        if(attributeType != AttributeType.FORMATS){
            return null;
        }
        return AttributeDataFormat.decodeValueTypes(getData());
    }
    public void addAttributeTypeFormats(AttributeDataFormat... formats){
        if(formats == null){
            return;
        }
        int data = getData() | AttributeDataFormat.sum(formats);
        setData(data);
        if(getValueType() == ValueType.NULL){
            setValueType(ValueType.DEC);
        }
    }
    public void addAttributeTypeFormat(AttributeDataFormat format){
        if(format == null){
            return;
        }
        int data = getData() | format.getMask();
        setData(data);
    }
    public Entry getEntry(){
        return getParent(Entry.class);
    }
    @Override
    public PackageBlock getParentChunk(){
        Entry entry = getEntry();
        if(entry!=null){
            return entry.getPackageBlock();
        }
        return null;
    }

    public ResTableMapEntry getParentMapEntry(){
        return getParentInstance(ResTableMapEntry.class);
    }
    public Entry getParentEntry(){
        return getParentInstance(Entry.class);
    }

    @Override
    public int getNameId() {
        return getInteger(getBytesInternal(), OFFSET_NAME);
    }
    @Override
    public void setNameId(int id){
        putInteger(getBytesInternal(), OFFSET_NAME, id);
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = super.toJson();
        if(jsonObject==null){
            return null;
        }
        jsonObject.put(NAME_name, getNameId());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        setNameId(json.getInt(NAME_name));
    }

    public void setNameHigh(short val){
        int name = getNameId() & 0xffff;
        name = ((val & 0xffff) <<16 ) | name;
        setNameId(name);
    }
    public void setNameLow(short val){
        int name = getNameId() & 0xffff0000;
        name = (val & 0xffff) | name;
        setNameId(name);
    }
    public void setDataHigh(short val){
        int data = getData() & 0xffff;
        data = ((val & 0xffff) <<16 ) | data;
        setData(data);
    }
    public void setDataLow(short val){
        int data = getData() & 0xffff0000;
        data = (val & 0xffff) | data;
        setData(data);
    }
    @Override
    public void merge(ValueItem valueItem){
        if(valueItem==this || !(valueItem instanceof ResValueMap)){
            return;
        }
        ResValueMap resValueMap = (ResValueMap) valueItem;
        super.merge(resValueMap);
        setNameId(resValueMap.getNameId());
    }
    @Override
    public int compareTo(ResValueMap valueMap) {
        if(valueMap == null){
            return -1;
        }
        if(valueMap == this){
            return 0;
        }
        int id1 = getNameId();
        int id2 = valueMap.getNameId();
        if(id1 == id2){
            return 0;
        }
        if(id1 == 0){
            return 1;
        }
        if(id2 == 0){
            return -1;
        }
        return Integer.compare(id1, id2);
    }
    @Override
    public String toString(){
        String name = decodeName();
        String data = decodeValue();
        if(name != null && data != null){
            return name + "=\"" + data + "\"";
        }
        return "name=" + HexUtil.toHex8(getNameId())
                +", " + super.toString();
    }

    private static final int OFFSET_NAME = 0;
    private static final int OFFSET_SIZE = 4;

    public static final String NAME_name = "name";

}
