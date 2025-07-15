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
import com.reandroid.arsc.item.ResXmlString;

import java.util.Objects;

abstract class ResXmlNamespaceChunk extends BaseXmlChunk implements ResXmlNamespace{
    private ResXmlNamespaceChunk mPair;
    ResXmlNamespaceChunk(ChunkType chunkType) {
        super(chunkType, 0);
    }
    @Override
    public String getUri(){
        return getString(getUriReference());
    }
    @Override
    public void setUri(String uri){
        if(uri == null){
            setUriReference(-1);
            return;
        }
        ResXmlString xmlString = getOrCreateString(uri);
        if(xmlString == null){
            throw new IllegalArgumentException("Null ResXmlString, add to parent element first");
        }
        setUriReference(xmlString.getIndex());
    }
    @Override
    public String getPrefix(){
        return getString(getPrefixReference());
    }
    @Override
    public void setPrefix(String prefix){
        if(prefix == null){
            setPrefixReference(-1);
            return;
        }
        ResXmlString xmlString = getOrCreateString(prefix);
        if(xmlString == null){
            throw new IllegalArgumentException("Null ResXmlString, add to parent element first");
        }
        setPrefixReference(xmlString.getIndex());
    }
    @Override
    public int getUriReference(){
        return getStringReference();
    }
    void setUriReference(int ref){
        int old = getUriReference();
        setStringReference(ref);
        ResXmlNamespaceChunk pair=getPair();
        if(pair!=null && pair.getUriReference()!=ref){
            pair.setUriReference(ref);
        }
        if(old != ref){
            onUriReferenceChanged(old, ref);
        }
    }
    void onUriReferenceChanged(int old, int uriReference){

    }
    public int getPrefixReference(){
        return getNamespaceReference();
    }
    public void setPrefixReference(int ref){
        setNamespaceReference(ref);
        ResXmlNamespaceChunk pair=getPair();
        if(pair!=null && pair.getPrefixReference()!=ref){
            pair.setPrefixReference(ref);
        }
    }
    ResXmlNamespaceChunk getPair(){
        return mPair;
    }
    void setPair(ResXmlNamespaceChunk pair){
        if(pair==this){
            return;
        }
        this.mPair=pair;
        if(pair !=null && pair.getPair()!=this){
            pair.setPair(this);
        }
    }
    @Override
    public void setLineNumber(int lineNumber){
        if(lineNumber == getLineNumber()){
            return;
        }
        super.setLineNumber(lineNumber);
        ResXmlNamespaceChunk pair = getPair();
        if(pair != null){
            pair.setLineNumber(lineNumber);
        }
    }
    @Override
    public String toString(){
        String uri=getUri();
        if(uri==null){
            return super.toString();
        }
        return "xmlns:"+getPrefix()+"=\""+getUri()+"\"";
    }
}
