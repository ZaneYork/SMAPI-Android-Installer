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
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.xml.XMLUtil;

public abstract class AttributeValue extends ValueItem{
    public AttributeValue(int bytesLength, int sizeOffset) {
        super(bytesLength, sizeOffset);
    }
    public abstract int getNameId();
    public abstract void setNameId(int resourceId);
    public abstract String decodePrefix();
    public abstract String decodeName(boolean includePrefix);

    public String decodeName(){
        return decodeName(true);
    }
    public ResourceEntry resolveName(){
        return resolve(getNameId());
    }
    public EncodeResult encodeStyleValue(ResourceEntry nameEntry, String value){
        return encodeStyleValue(false, nameEntry, value);
    }
    public EncodeResult encodeStyleValue(boolean validate, ResourceEntry name, String value){
        return ValueCoder.encodeAttributeValue(validate, this, name, value);
    }
    public ResourceEntry encodeAttrName(String name){
        return encodeAttrName(XMLUtil.splitPrefix(name), XMLUtil.splitName(name));
    }
    public ResourceEntry encodeAttrName(String prefix, String name){
        if(name == null){
            return null;
        }
        if(prefix == null){
            prefix = XMLUtil.splitPrefix(name);
        }
        name = XMLUtil.splitName(name);
        EncodeResult encodeResult = ValueCoder.encodeUnknownNameId(name);
        if(encodeResult != null){
            setName(name, encodeResult.value);
            return new ResourceEntry(getPackageBlock(), encodeResult.value);
        }
        if(prefix == null){
            if(!allowNullPrefixEncode()){
                return null;
            }
        }
        PackageBlock packageBlock = getPackageBlock();
        ResourceEntry resourceEntry = packageBlock.getTableBlock()
                .getAttrResource(packageBlock, prefix, name);
        if(resourceEntry != null){
            setName(name, resourceEntry.getResourceId());
        }
        return resourceEntry;
    }
    public ResourceEntry encodeIdName(String name){
        return encodeIdName(XMLUtil.splitPrefix(name), XMLUtil.splitName(name));
    }
    public ResourceEntry encodeIdName(String prefix, String name){
        if(name == null){
            return null;
        }
        if(prefix == null){
            prefix = XMLUtil.splitPrefix(name);
        }
        name = XMLUtil.splitName(name);
        EncodeResult encodeResult = ValueCoder.encodeUnknownNameId(name);
        if(encodeResult != null){
            setName(name, encodeResult.value);
            return new ResourceEntry(getPackageBlock(), encodeResult.value);
        }
        if(prefix == null){
            if(!allowNullPrefixEncode()){
                return null;
            }
        }
        PackageBlock packageBlock = getPackageBlock();
        ResourceEntry resourceEntry = packageBlock.getTableBlock()
                .getIdResource(packageBlock, prefix, name);
        if(resourceEntry != null){
            setName(name, resourceEntry.getResourceId());
        }
        return resourceEntry;
    }
    boolean allowNullPrefixEncode(){
        return false;
    }
    public void setName(String name, int nameId){
        setNameId(nameId);
    }
    @Override
    public String decodeValue(boolean validatePackage){
        if(AttributeDataFormat.INTEGER.contains(getValueType())){
            String value = decodeDataAsAttrFormats();
            if(value == null){
                value = decodeDataAsAttr();
            }
            if(value != null){
                return value;
            }
        }
        return super.decodeValue(validatePackage);
    }
    private String decodeDataAsAttr(){
        ResourceEntry attr = resolveName();
        if(attr != null){
            return attr.decodeAttributeData(getData());
        }
        return null;
    }
    String decodeDataAsAttrFormats(){
        return null;
    }
    @Override
    public void mergeWithName(ResourceMergeOption mergeOption, ValueItem valueItem){
        if(valueItem == null || valueItem == this || getClass() != valueItem.getClass()){
            return;
        }
        AttributeValue attributeValue = (AttributeValue) valueItem;
        super.mergeWithName(mergeOption, attributeValue);

        String name = attributeValue.decodeName(false);
        ResourceEntry nameId = attributeValue.resolveName();
        if(nameId == null){
            setName(name, attributeValue.getNameId());
        }else {
            int id = attributeValue.getNameId();
            if(nameId.isContext(attributeValue.getPackageBlock())){
                ResourceEntry mergedName;
                PackageBlock packageBlock = getPackageBlock();
                if(nameId.isDeclared()){
                    mergedName = packageBlock.mergeWithName(mergeOption, nameId);
                }else {
                    mergedName = mergeOption.resolveUndeclared(packageBlock, nameId);
                }
                if(mergedName != null){
                    id = mergedName.getResourceId();
                    name = mergedName.getName();
                }
            }else {
                id = nameId.getResourceId();
            }
            setName(name, id);
        }
    }
}
