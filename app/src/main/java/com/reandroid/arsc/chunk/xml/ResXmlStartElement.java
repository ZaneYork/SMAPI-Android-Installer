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
package com.reandroid.arsc.chunk.xml;

import com.reandroid.arsc.array.ResXmlAttributeArray;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.utils.StringsUtil;

import java.util.Iterator;

public class ResXmlStartElement extends BaseXmlChunk {
    private final ShortItem mAttributeStart;
    private final ShortItem mAttributeUnitSize;
    private final ShortItem mAttributeCount;
    private final ShortItem mIdAttributePosition;
    private final ShortItem mClassAttributePosition;
    private final ShortItem mStyleAttributePosition;
    private final ResXmlAttributeArray mAttributeArray;
    private ResXmlEndElement mResXmlEndElement;
    public ResXmlStartElement() {
        super(ChunkType.XML_START_ELEMENT, 7);
        mAttributeStart = new ShortItem(ATTRIBUTES_DEFAULT_START);
        mAttributeUnitSize = new ShortItem(ATTRIBUTES_UNIT_SIZE);
        mAttributeCount = new ShortItem();
        mIdAttributePosition = new ShortItem();
        mClassAttributePosition = new ShortItem();
        mStyleAttributePosition = new ShortItem();
        mAttributeArray = new ResXmlAttributeArray(getHeaderBlock(),
                mAttributeStart,
                mAttributeCount,
                mAttributeUnitSize);
        addChild(mAttributeStart);
        addChild(mAttributeUnitSize);
        addChild(mAttributeCount);
        addChild(mIdAttributePosition);
        addChild(mClassAttributePosition);
        addChild(mStyleAttributePosition);
        addChild(mAttributeArray);
    }

    public int removeUndefinedAttributes(){
        return getResXmlAttributeArray().removeUndefinedAttributes();
    }
    public ResXmlAttribute getIdAttribute(){
        return getResXmlAttributeArray().get(mIdAttributePosition.unsignedInt()-1);
    }
    public ResXmlAttribute getClassAttribute(){
        return getResXmlAttributeArray().get(mClassAttributePosition.unsignedInt()-1);
    }
    public ResXmlAttribute getStyleAttribute(){
        return getResXmlAttributeArray().get(mStyleAttributePosition.unsignedInt()-1);
    }
    void setAttributesUnitSize(int size){
        mAttributeArray.setAttributesUnitSize(size);
    }
    public ResXmlAttribute newAttribute(){
        ResXmlAttributeArray attributeArray = getResXmlAttributeArray();
        return attributeArray.createNext();
    }
    @Override
    void linkStringReferences(){
        super.linkStringReferences();
        ResXmlEndElement end = getResXmlEndElement();
        if(end != null){
            end.linkStringReferences();
        }
        linkNamespace();
    }
    @Override
    void onRemoved(){
        super.onRemoved();
        unlinkNamespace();
        ResXmlEndElement end = getResXmlEndElement();
        if(end != null){
            end.onRemoved();
        }
        Iterator<ResXmlAttribute> iterator = iterator();
        while (iterator.hasNext()){
            iterator.next().onRemoved();
        }
    }
    @Override
    protected void onPreRefresh(){
        sortAttributes();
    }
    void unlinkNamespace(){
        ResXmlStartNamespace namespace = getResXmlStartNamespace();
        if(namespace != null){
            namespace.removeElementReference(this);
        }
    }
    void linkNamespace(){
        ResXmlStartNamespace namespace = getResXmlStartNamespace();
        if(namespace != null){
            namespace.addElementReference(this);
        }
    }
    private void sortAttributes(){
        ResXmlAttributeArray array = getResXmlAttributeArray();

        ResXmlAttribute idAttribute=array.get(mIdAttributePosition.get()-1);
        ResXmlAttribute classAttribute=array.get(mClassAttributePosition.get()-1);
        ResXmlAttribute styleAttribute=array.get(mStyleAttributePosition.get()-1);

        array.sortAttributes();
        if(idAttribute!=null){
            mIdAttributePosition.set(idAttribute.getIndex() + 1);
        }
        if(classAttribute!=null){
            mClassAttributePosition.set(classAttribute.getIndex() + 1);
            // In case obfuscation
            if(!ATTRIBUTE_NAME_CLASS.equals(classAttribute.getName())){
                classAttribute.setName(ATTRIBUTE_NAME_CLASS, 0);
            }
        }
        if(styleAttribute != null){
            mStyleAttributePosition.set(styleAttribute.getIndex() + 1);
            // In case obfuscation
            if(!ATTRIBUTE_NAME_STYLE.equals(styleAttribute.getName())){
                styleAttribute.setName(ATTRIBUTE_NAME_STYLE, 0);
            }
        }
    }
    void calculatePositions(){
        ResXmlAttribute idAttribute=getAttribute(ATTRIBUTE_RESOURCE_ID_id);
        ResXmlAttribute classAttribute=getNoIdAttribute(ATTRIBUTE_NAME_CLASS);
        ResXmlAttribute styleAttribute=getNoIdAttribute(ATTRIBUTE_NAME_STYLE);

        if(idAttribute != null){
            mIdAttributePosition.set(idAttribute.getIndex() + 1);
        }
        if(classAttribute != null){
            mClassAttributePosition.set(classAttribute.getIndex() + 1);
        }
        if(styleAttribute != null){
            mStyleAttributePosition.set(styleAttribute.getIndex() + 1);
        }
    }
    public ResXmlAttribute getAttribute(int resourceId){
        Iterator<ResXmlAttribute> iterator = iterator();
        while (iterator.hasNext()){
            ResXmlAttribute attribute = iterator.next();
            if(resourceId == attribute.getNameId()){
                return attribute;
            }
        }
        return null;
    }
    private ResXmlAttribute getNoIdAttribute(String name){
        Iterator<ResXmlAttribute> iterator = iterator();
        while (iterator.hasNext()){
            ResXmlAttribute attribute = iterator.next();
            if(attribute.getNameId()!=0){
                continue;
            }
            if(name.equals(attribute.getName())){
                return attribute;
            }
        }
        return null;
    }
    public ResXmlAttribute getAttribute(String uri, String name){
        if(name==null){
            return null;
        }
        Iterator<ResXmlAttribute> iterator = iterator();
        while (iterator.hasNext()){
            ResXmlAttribute attribute = iterator.next();
            if(attribute.equalsName(name)){
                if(uri != null){
                    if(uri.equals(attribute.getUri())){
                        return attribute;
                    }
                    continue;
                }
                return attribute;
            }
        }
        return null;
    }
    // Searches attribute with resource id = 0
    public ResXmlAttribute searchAttributeByName(String name){
        if(name == null){
            return null;
        }
        Iterator<ResXmlAttribute> iterator = getResXmlAttributeArray().iterator();
        ResXmlAttribute withIdAttribute = null;
        while (iterator.hasNext()){
            ResXmlAttribute attribute = iterator.next();
            if(attribute.equalsName(name)){
                if(attribute.getNameId() != 0){
                    withIdAttribute = attribute;
                    continue;
                }
                return attribute;
            }
        }
        return withIdAttribute;
    }
    public ResXmlAttribute searchAttributeByResourceId(int resourceId){
        if(resourceId == 0){
            return null;
        }
        Iterator<ResXmlAttribute> iterator = iterator();
        while (iterator.hasNext()){
            ResXmlAttribute attribute = iterator.next();
            if(resourceId == attribute.getNameId()){
                return attribute;
            }
        }
        return null;
    }
    public String getTagName(){
        return getTagName(true);
    }
    public String getTagName(boolean includePrefix){
        String name = getName();
        if(includePrefix){
            String prefix = getPrefix();
            if(prefix != null){
                name = prefix + ":" + name;
            }
        }
        return name;
    }
    public String getName(boolean includePrefix){
        String name = super.getName();
        if(includePrefix){
            String prefix = getPrefix();
            if(prefix != null){
                name = prefix + ":" + name;
            }
        }
        return name;
    }
    public void setName(String name){
        if(name == null){
            setStringReference(-1);
        }else {
            setString(name);
        }
        ResXmlEndElement endElement = getResXmlEndElement();
        if(endElement != null){
            endElement.setString(name);
        }
    }
    public Iterator<ResXmlAttribute> iterator(){
        return getResXmlAttributeArray().iterator();
    }
    public ResXmlAttributeArray getResXmlAttributeArray(){
        return mAttributeArray;
    }

    public String getUri(){
        ResXmlStartNamespace startNamespace = getResXmlStartNamespace();
        if(startNamespace != null){
            return startNamespace.getUri();
        }
        return null;
    }
    public String getPrefix(){
        ResXmlStartNamespace startNamespace = getResXmlStartNamespace();
        if(startNamespace != null){
            return startNamespace.getPrefix();
        }
        return null;
    }
    public void setTagNamespace(String uri, String prefix){
        unlinkNamespace();
        if(uri == null || prefix == null){
            setNamespaceReference(-1);
            return;
        }
        ResXmlElement parentElement = getParentResXmlElement();
        if(parentElement == null){
            return;
        }
        ResXmlNamespace ns = parentElement.getOrCreateNamespace(uri, prefix);
        setNamespaceReference(ns.getUriReference());
        linkNamespace();
    }
    ResXmlStartNamespace getResXmlStartNamespace(){
        int uriRef = getNamespaceReference();
        if(uriRef < 0){
            return null;
        }
        ResXmlElement parentElement = getParentResXmlElement();
        if(parentElement != null){
            return parentElement.getStartNamespaceByUriRef(uriRef);
        }
        return null;
    }
    public void setResXmlEndElement(ResXmlEndElement element){
        mResXmlEndElement=element;
    }
    public ResXmlEndElement getResXmlEndElement(){
        return mResXmlEndElement;
    }

    @Override
    protected void onChunkRefreshed() {
        refreshAttributeStart();
        refreshAttributeCount();
    }
    private void refreshAttributeStart(){
        int start = countUpTo(mAttributeArray);
        start = start - getHeaderBlock().getHeaderSize();
        mAttributeStart.set(start);
    }
    private void refreshAttributeCount(){
        int count = mAttributeArray.size();
        mAttributeCount.set(count);
    }
    @Override
    public void setLineNumber(int lineNumber){
        super.setLineNumber(lineNumber);
        ResXmlEndElement endElement = getResXmlEndElement();
        if(endElement != null){
            endElement.setLineNumber(lineNumber);
        }
    }

    @Override
    public String toString(){
        String tag = getTagName();
        if(tag == null){
            return super.toString();
        }
        return tag + " " + StringsUtil.join(iterator(), ' ');
    }

    private static final short ATTRIBUTES_UNIT_SIZE=20;
    private static final short ATTRIBUTES_DEFAULT_START=20;
    /*
     * Find another way to mark an attribute is class, device actually relies on
     * value of mClassAttributePosition */
    private static final String ATTRIBUTE_NAME_CLASS="class";
    /*
     * Find another way to mark an attribute is style, device actually relies on
     * value of mStyleAttributePosition */
    private static final String ATTRIBUTE_NAME_STYLE="style";
    /*
     * Resource id value of attribute 'android:id'
     * instead of relying on hardcoded value, we should find another way to
     * mark an attribute is 'id' */
    private static final int ATTRIBUTE_RESOURCE_ID_id =0x010100d0;
}
