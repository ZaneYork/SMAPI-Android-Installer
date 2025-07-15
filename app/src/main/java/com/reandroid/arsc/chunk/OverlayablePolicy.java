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
package com.reandroid.arsc.chunk;

import android.text.TextUtils;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.CountedBlockList;
import com.reandroid.arsc.header.OverlayablePolicyHeader;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.xml.XMLUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;

public class OverlayablePolicy extends Chunk<OverlayablePolicyHeader> implements
        Iterable<PolicyItem>, JSONConvert<JSONObject> {

    private final CountedBlockList<PolicyItem> itemList;

    public OverlayablePolicy() {
        super(new OverlayablePolicyHeader(), 1);
        this.itemList = new CountedBlockList<>(CREATOR, getHeaderBlock().getEntryCount());

        addChild(this.itemList);
    }

    public PolicyItem get(int i) {
        return getItemList().get(i);
    }
    public void add(PolicyItem policyItem) {
        getItemList().add(policyItem);
    }
    public PolicyItem createNext() {
        return getItemList().createNext();
    }
    @Override
    public Iterator<PolicyItem> iterator() {
        return itemList.iterator();
    }

    @Override
    public boolean isNull() {
        return getReferenceCount()==0;
    }
    public int getReferenceCount(){
        return itemList.size();
    }
    public int getFlags() {
        return getHeaderBlock().getFlags().get();
    }
    public void setFlags(int flags){
        getHeaderBlock().getFlags().set(flags);
    }
    public void setFlags(PolicyFlag[] policyFlags){
        setFlags(PolicyFlag.sum(policyFlags));
    }
    public void addFlag(PolicyFlag policyFlag){
        int i = policyFlag == null ? 0 : policyFlag.flag();
        setFlags(getFlags() | i);
    }
    public PolicyFlag[] getPolicyFlags(){
        return PolicyFlag.valuesOf(getFlags());
    }

    public CountedBlockList<PolicyItem> getItemList() {
        return itemList;
    }

    @Override
    protected void onChunkRefreshed() {
    }

    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("Parser not in START_TAG event: " + parser.getEventType());
        }
        if (!TAG_policy.equals(parser.getName())) {
            throw new XmlPullParserException("Expecting tag '" + TAG_policy +
                    "', but found '" + parser.getName() + "'");
        }
        setFlags(PolicyFlag.parse(parser));
        parser.next();
        XMLUtil.ensureTag(parser);
        while (parser.getEventType() != XmlPullParser.END_TAG &&
                parser.getEventType() != XmlPullParser.END_DOCUMENT) {
            PolicyItem policyItem = createNext();
            policyItem.parse(parser);
            XMLUtil.ensureTag(parser);
        }
        if (parser.getEventType() == XmlPullParser.END_TAG) {
            parser.next();
        }
    }
    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, TAG_policy);
        String flags = PolicyFlag.toString(getPolicyFlags());
        if (!TextUtils.isEmpty(flags)) {
            serializer.attribute(null, "type", flags);
        }
        for (PolicyItem reference : this) {
            reference.serialize(serializer);
        }
        serializer.endTag(null, TAG_policy);
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NAME_flags, getFlags());
        JSONArray jsonArray = new JSONArray();
        for(PolicyItem reference : this) {
            jsonArray.put(reference.get());
        }
        jsonObject.put(NAME_references, jsonArray);
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setFlags(json.getInt(NAME_flags));
        JSONArray jsonArray = json.getJSONArray(NAME_references);
        int length = jsonArray.length();
        CountedBlockList<PolicyItem> referenceList = getItemList();
        referenceList.setSize(length);

        for(int i = 0; i < length; i++) {
            PolicyItem ref = referenceList.get(i);
            ref.set(jsonArray.getInt(i));
        }
    }
    public void merge(OverlayablePolicy policy){
        if(policy == null || policy == this){
            return;
        }
        setFlags(this.getFlags() | policy.getFlags());

        CountedBlockList<PolicyItem> exist = this.getItemList();
        Iterator<PolicyItem> iterator = policy.getItemList()
                .iterator();

        while (iterator.hasNext()){
            PolicyItem coming = iterator.next();
            if(!exist.contains(coming)) {
                PolicyItem reference = new PolicyItem();
                reference.set(coming.get());
                exist.add(reference);
            }
        }
    }

    @Override
    public String toString(){
        return getClass().getSimpleName()+
                ": flags="+ PolicyFlag.toString(getPolicyFlags())
                +"', count="+ getReferenceCount();
    }

    private static final Creator<PolicyItem> CREATOR = new Creator<PolicyItem>() {
        @Override
        public PolicyItem[] newArrayInstance(int length) {
            return new PolicyItem[length];
        }
        @Override
        public PolicyItem newInstance() {
            return new PolicyItem();
        }
    };

    public static final String NAME_flags = "flags";
    public static final String NAME_references = "references";

    public static final String TAG_policy = ObjectsUtil.of("policy");
}
