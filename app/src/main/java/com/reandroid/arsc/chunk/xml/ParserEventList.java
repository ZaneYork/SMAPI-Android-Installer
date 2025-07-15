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


import com.reandroid.arsc.coder.XmlSanitizer;
import com.reandroid.utils.collection.ArrayCollection;

import java.util.Iterator;
import java.util.List;

public class ParserEventList implements Iterator<ParserEvent> {
    private final List<ParserEvent> eventList;
    private int index;
    private ParserEvent mCurrent;
    private int type = -1;
    public ParserEventList(){
        this.eventList = new ArrayCollection<>();
    }
    public void clear(){
        this.eventList.clear();
        reset();
    }
    public int getType(){
        return type;
    }
    public String getText(){
        if(type == ParserEvent.COMMENT){
            return mCurrent.getComment();
        }
        if(type == ParserEvent.START_TAG || type == ParserEvent.END_TAG){
            return getElement().getName();
        }
        if(type == ParserEvent.TEXT){
            String text = ((ResXmlTextNode)getXmlNode()).getText();
            if(text == null){
                text = "";
            }
            return XmlSanitizer.escapeSpecialCharacter(text);
        }
        return null;
    }
    public int getLineNumber(){
        if(type != ParserEvent.COMMENT
                && type != ParserEvent.TEXT
                && type != ParserEvent.START_TAG
                && type != ParserEvent.END_TAG){
            return -1;
        }
        ResXmlNode xmlNode = getXmlNode();
        if(mCurrent.isEndComment() || type == ParserEvent.END_TAG){
            return ((ResXmlElement)xmlNode).getEndLineNumber();
        }
        if(type == ParserEvent.TEXT){
            return ((ResXmlTextNode)xmlNode).getLineNumber();
        }
        return ((ResXmlElement)xmlNode).getStartLineNumber();
    }
    public ResXmlNode getXmlNode(){
        return mCurrent.getXmlNode();
    }
    public ResXmlElement getElement(){
        return (ResXmlElement) mCurrent.getXmlNode();
    }
    @Override
    public ParserEvent next(){
        if(!hasNext()){
            return null;
        }
        ParserEvent event = get(index);
        index++;
        mCurrent = event;
        type = event.getEvent();
        return event;
    }
    @Override
    public boolean hasNext(){
        return index < size();
    }
    public int size(){
        return eventList.size();
    }
    public int getIndex() {
        return index;
    }
    public void reset(){
        index = 0;
        mCurrent = null;
        type = -1;
    }
    void add(ParserEvent parserEvent){
        if(parserEvent==null){
            return;
        }
        eventList.add(parserEvent);
    }
    private ParserEvent get(int i){
        return eventList.get(i);
    }
}
