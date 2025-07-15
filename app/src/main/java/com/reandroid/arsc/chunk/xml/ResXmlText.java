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
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ResXmlString;
import com.reandroid.arsc.pool.ResXmlStringPool;

public class ResXmlText extends BaseXmlChunk {
    private final IntegerItem mReserved;
    public ResXmlText() {
        super(ChunkType.XML_CDATA, 1);
        this.mReserved=new IntegerItem();
        addChild(mReserved);
        setStringReference(0);
    }
    public String getText(){
        ResXmlString xmlString=getResXmlString(getTextReference());
        if(xmlString!=null){
            return xmlString.getHtml();
        }
        return null;
    }
    public int getTextReference(){
        return getNamespaceReference();
    }
    public void setTextReference(int ref){
        setNamespaceReference(ref);
    }
    public void setText(String text){
        ResXmlStringPool stringPool=getStringPool();
        if(stringPool==null){
            return;
        }
        ResXmlString resXmlString = stringPool.getOrCreate(text);
        int ref=resXmlString.getIndex();
        setTextReference(ref);
    }
    @Override
    public boolean isNull() {
        return getText() == null
                || super.isNull();
    }

    @Override
    public String toString(){
        String txt=getText();
        if(txt!=null){
            return txt;
        }
        return super.toString();
    }
}
