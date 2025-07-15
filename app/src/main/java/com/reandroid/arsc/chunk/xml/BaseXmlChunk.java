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
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.Chunk;
import com.reandroid.arsc.header.XmlNodeHeader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ResXmlString;
import com.reandroid.arsc.pool.ResXmlStringPool;

class BaseXmlChunk extends Chunk<XmlNodeHeader> {
    private final IntegerItem mNamespaceReference;
    private final IntegerItem mStringReference;

    BaseXmlChunk(ChunkType chunkType, int initialChildesCount) {
        super(new XmlNodeHeader(chunkType), initialChildesCount+2);

        this.mNamespaceReference=new IntegerItem(-1);
        this.mStringReference=new IntegerItem(-1);

        addChild(mNamespaceReference);
        addChild(mStringReference);
    }
    void onRemoved(){
        ResXmlStringPool stringPool = getStringPool();
        if(stringPool==null){
            return;
        }
        stringPool.removeReference(getHeaderBlock().getCommentReference());
        stringPool.removeReference(mNamespaceReference);
        stringPool.removeReference(mStringReference);
    }
    void linkStringReferences(){
        linkStringReference(getHeaderBlock().getCommentReference());
        linkStringReference(mNamespaceReference);
        linkStringReference(mStringReference);
    }
    private void linkStringReference(IntegerItem item){
        ResXmlString xmlString = getResXmlString(item.get());
        if(xmlString!=null){
            xmlString.addReferenceIfAbsent(item);
        }
    }
    void unLinkStringReference(IntegerItem item){
        ResXmlString xmlString = getResXmlString(item.get());
        if(xmlString!=null){
            xmlString.removeReference(item);
        }
    }
    public void setLineNumber(int val){
        getHeaderBlock().getLineNumber().set(val);
    }
    public int getLineNumber(){
        return getHeaderBlock().getLineNumber().get();
    }
    public void setCommentReference(int val){
        if(val == getCommentReference()){
            return;
        }
        IntegerItem comment=getHeaderBlock().getCommentReference();
        unLinkStringReference(comment);
        getHeaderBlock().getCommentReference().set(val);
        linkStringReference(comment);
    }
    public int getCommentReference(){
        return getHeaderBlock().getCommentReference().get();
    }
    void setNamespaceReference(int val){
        if(val == getNamespaceReference()){
            return;
        }
        unLinkStringReference(mNamespaceReference);
        mNamespaceReference.set(val);
        linkStringReference(mNamespaceReference);
    }
    int getNamespaceReference(){
        return mNamespaceReference.get();
    }
    void setStringReference(int val){
        if(val == getStringReference()){
            return;
        }
        unLinkStringReference(mStringReference);
        mStringReference.set(val);
        linkStringReference(mStringReference);
    }
    int getStringReference(){
        return mStringReference.get();
    }
    ResXmlString setString(String str){
        ResXmlStringPool pool = getStringPool();
        if(pool==null){
            return null;
        }
        ResXmlString xmlString = pool.getOrCreate(str);
        setStringReference(xmlString.getIndex());
        return xmlString;
    }
    ResXmlStringPool getStringPool(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof ResXmlDocument){
                return ((ResXmlDocument)parent).getStringPool();
            }
            if(parent instanceof ResXmlElement){
                return ((ResXmlElement)parent).getStringPool();
            }
            parent=parent.getParent();
        }
        return null;
    }
    ResXmlString getResXmlString(int ref){
        if(ref<0){
            return null;
        }
        ResXmlStringPool stringPool=getStringPool();
        if(stringPool!=null){
            return stringPool.get(ref);
        }
        return null;
    }
    ResXmlString getOrCreateResXmlString(String str){
        ResXmlStringPool stringPool=getStringPool();
        if(stringPool!=null){
            return stringPool.getOrCreate(str);
        }
        return null;
    }
    String getString(int ref){
        ResXmlString xmlString=getResXmlString(ref);
        if(xmlString!=null){
            return xmlString.get();
        }
        return null;
    }
    ResXmlString getOrCreateString(String str){
        ResXmlStringPool stringPool=getStringPool();
        if(stringPool==null){
            return null;
        }
        return stringPool.getOrCreate(str);
    }

    public String getName(){
        return getString(getStringReference());
    }
    public String getUri(){
        return getString(getNamespaceReference());
    }
    public String getComment(){
        return getString(getCommentReference());
    }
    public void setComment(String comment){
        if(comment==null||comment.length()==0){
            setCommentReference(-1);
        }else {
            String old=getComment();
            if(comment.equals(old)){
                return;
            }
            ResXmlString xmlString = getOrCreateResXmlString(comment);
            setCommentReference(xmlString.getIndex());
        }
    }
    public ResXmlElement getParentResXmlElement(){
        return getParent(ResXmlElement.class);
    }
    @Override
    protected void onChunkRefreshed() {

    }
    @Override
    public String toString(){
        ChunkType chunkType=getHeaderBlock().getChunkType();
        if(chunkType==null){
            return super.toString();
        }
        StringBuilder builder=new StringBuilder();
        builder.append(chunkType.toString());
        builder.append(": line=");
        builder.append(getLineNumber());
        builder.append(" {");
        builder.append(getName());
        builder.append("}");
        return builder.toString();
    }
}
