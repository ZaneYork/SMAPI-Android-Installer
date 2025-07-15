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

import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.model.ResourceLibrary;
import com.reandroid.common.Namespace;
import com.reandroid.xml.XMLNamespace;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ResXmlStartNamespace extends ResXmlNamespaceChunk {
    private final Set<ResXmlAttribute> mReferencedAttributes;
    private final Set<ResXmlStartElement> mReferencedElements;

    public ResXmlStartNamespace() {
        super(ChunkType.XML_START_NAMESPACE);
        this.mReferencedAttributes = new HashSet<>();
        this.mReferencedElements = new HashSet<>();
    }
    @Override
    void onUriReferenceChanged(int old, int uriReference){
        for(ResXmlAttribute attribute : mReferencedAttributes){
            attribute.setUriReference(uriReference);
        }
        for(ResXmlStartElement element : mReferencedElements){
            element.setNamespaceReference(uriReference);
        }
    }
    ResXmlEndNamespace getEnd(){
        return (ResXmlEndNamespace) getPair();
    }
    void setEnd(ResXmlEndNamespace namespace){
        setPair(namespace);
    }
    @Override
    void linkStringReferences(){
        super.linkStringReferences();
        ResXmlEndNamespace end = getEnd();
        if(end!=null){
            end.linkStringReferences();
        }
    }
    @Override
    void onRemoved(){
        ResXmlEndNamespace end = getEnd();
        if(end!=null){
            end.onRemoved();
        }
        mReferencedAttributes.clear();
        mReferencedElements.clear();
    }
    public boolean hasReferences(){
        return mReferencedAttributes.size() > 0
                || mReferencedElements.size() > 0;
    }
    public Iterator<ResXmlAttribute> getReferencedAttributes(){
        return mReferencedAttributes.iterator();
    }
    void addAttributeReference(ResXmlAttribute attribute){
        if(attribute != null){
            mReferencedAttributes.add(attribute);
        }
    }
    void removeAttributeReference(ResXmlAttribute attribute){
        if(attribute != null){
            mReferencedAttributes.remove(attribute);
        }
    }
    void addElementReference(ResXmlStartElement element){
        if(element != null){
            mReferencedElements.add(element);
        }
    }
    void removeElementReference(ResXmlStartElement element){
        if(element != null){
            mReferencedElements.remove(element);
        }
    }
    boolean removeIfNoReference(){
        if(hasReferences()){
            return false;
        }
        ResXmlElement parent = getParentResXmlElement();
        if(parent != null){
            parent.removeNamespace(this);
            return true;
        }
        return false;
    }
    public XMLNamespace decodeToXml(){
        String uri = getUri();
        String prefix = getPrefix();
        if(isEmpty(uri) || isEmpty(prefix)){
            return null;
        }
        return new XMLNamespace(uri, prefix);
    }
    boolean fixEmpty(){
        boolean changed = fixEmptyPrefix();
        if(fixEmptyUri()){
            changed = true;
        }
        return changed;
    }
    private boolean fixEmptyPrefix(){
        if(!isEmpty(getPrefix())){
            return false;
        }
        setPrefix("ns" + getIndex());
        return true;
    }
    private boolean fixEmptyUri(){
        if(!isEmpty(getUri())){
            return false;
        }
        setUri(ResourceLibrary.URI_RES_AUTO);
        return true;
    }
    private boolean isEmpty(String txt){
        if(txt==null){
            return true;
        }
        txt=txt.trim();
        return txt.length()==0;
    }
}
