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
package com.reandroid.arsc.item;

import com.reandroid.arsc.base.Block;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.collection.ArrayCollection;

import java.util.List;

public class ResXmlID extends IntegerItem implements Comparable<ResXmlID>{

    private List<ReferenceItem> mReferencedList;
    private ResXmlString mResXmlString;

    public ResXmlID(int resId){
        super(resId);
    }
    public ResXmlID(){
        this(0);
    }

    public boolean removeReference(ReferenceItem ref) {
        boolean removed = false;
        List<ReferenceItem> referencedList = this.mReferencedList;
        if(referencedList != null) {
            removed = referencedList.remove(ref);
            if(referencedList.size() == 0) {
                this.mReferencedList = null;
            }
        }
        return removed;
    }
    public void addReference(ReferenceItem ref){
        if(ref != null) {
            List<ReferenceItem> referencedList = this.mReferencedList;
            if(referencedList == null) {
                referencedList = new ArrayCollection<>();
                this.mReferencedList = referencedList;
            }
            referencedList.add(ref);
        }
    }
    public boolean hasReference() {
        List<ReferenceItem> referencedList = this.mReferencedList;
        if(referencedList != null) {
            return referencedList.size() != 0;
        }
        return false;
    }
    public boolean hasReference(Block block) {
        if(block == null) {
            return false;
        }
        List<ReferenceItem> referencedList = this.mReferencedList;
        if(referencedList != null) {
            for(ReferenceItem item : referencedList) {
                if(item.getReferredParent(block.getClass()) == block) {
                    return true;
                }
            }
        }
        return false;
    }
    public int getReferenceCount() {
        List<ReferenceItem> referencedList = this.mReferencedList;
        if(referencedList != null) {
            return referencedList.size();
        }
        return 0;
    }
    @Override
    public void onIndexChanged(int oldIndex, int newIndex) {
    }
    public String getName(){
        ResXmlString xmlString = getResXmlString();
        if(xmlString == null){
            return null;
        }
        return xmlString.getHtml();
    }
    public ResXmlString getResXmlString() {
        ResXmlString resXmlString = this.mResXmlString;
        if(resXmlString == null || resXmlString.isNull()) {
            return null;
        }
        return resXmlString;
    }
    void setResXmlStringInternal(ResXmlString xmlString) {
        this.mResXmlString = xmlString;
    }

    public boolean isEmpty() {
        StringItem stringItem = getResXmlString();
        if(stringItem == null) {
            return true;
        }
        return get() == 0;
    }
    @Override
    public int compareTo(ResXmlID resXmlID) {
        if(resXmlID == null) {
            return -1;
        }
        if(resXmlID == this) {
            return 0;
        }
        ResXmlString xmlString1 = this.getResXmlString();
        ResXmlString xmlString2 = resXmlID.getResXmlString();
        int i = CompareUtil.compare(xmlString1 == null, xmlString2 == null);
        if(i != 0) {
            return i;
        }
        if(xmlString1 == null || xmlString2 == null) {
            return 0;
        }
        return CompareUtil.compare(xmlString1.getIndex(), xmlString2.getIndex());
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("USED-BY=");
        builder.append(getReferenceCount());
        builder.append('{');
        String name = getName();
        if(name != null){
            builder.append(name);
        }else {
            builder.append(getIndex());
        }
        builder.append(':');
        builder.append(HexUtil.toHex8(get()));
        builder.append('}');
        return builder.toString();
    }
}
