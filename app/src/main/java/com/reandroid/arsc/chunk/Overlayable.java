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

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.header.OverlayableHeader;
import com.reandroid.arsc.io.BlockReader;
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

public class Overlayable extends Chunk<OverlayableHeader> implements
        Iterable<OverlayablePolicy>, JSONConvert<JSONObject> {

    private final BlockList<OverlayablePolicy> policyList;

    public Overlayable() {
        super(new OverlayableHeader(), 2);
        this.policyList = new BlockList<>();
        addChild(this.policyList);
    }

    @Override
    public Iterator<OverlayablePolicy> iterator() {
        return policyList.iterator();
    }

    public OverlayablePolicy createNext() {
        OverlayablePolicy overlayablePolicy = new OverlayablePolicy();
        this.policyList.add(overlayablePolicy);
        return overlayablePolicy;
    }

    public OverlayablePolicy getByFlag(int flags) {
        for (OverlayablePolicy policy : this) {
            if(flags == policy.getFlags()){
                return policy;
            }
        }
        return null;
    }
    public void addPolicy(OverlayablePolicy overlayablePolicy){
        this.policyList.add(overlayablePolicy);
    }

    public String getName(){
        return getHeaderBlock().getName().get();
    }
    public void setName(String str){
        getHeaderBlock().getName().set(str);
    }
    public String getActor(){
        return getHeaderBlock().getActor().get();
    }
    public void setActor(String str){
        getHeaderBlock().getActor().set(str);
    }

    @Override
    protected void onChunkRefreshed() {
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock = reader.readHeaderBlock();
        checkInvalidChunk(headerBlock);

        int size = headerBlock.getChunkSize();
        BlockReader chunkReader = reader.create(size);
        headerBlock = getHeaderBlock();
        headerBlock.readBytes(chunkReader);

        readOverlayablePolicies(chunkReader);

        reader.offset(size);
        chunkReader.close();
        onChunkLoaded();
    }
    private void readOverlayablePolicies(BlockReader reader) throws IOException {
        HeaderBlock headerBlock = reader.readHeaderBlock();
        BlockList<OverlayablePolicy> policyList = this.policyList;
        while (headerBlock!=null && headerBlock.getChunkType()==ChunkType.OVERLAYABLE_POLICY){
            OverlayablePolicy policy = new OverlayablePolicy();
            policyList.add(policy);
            policy.readBytes(reader);
            headerBlock = reader.readHeaderBlock();
        }
    }

    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        XMLUtil.ensureStartTag(parser);
        if (!TAG_overlayable.equals(parser.getName())) {
            throw new XmlPullParserException("Expecting tag '" + TAG_overlayable +
                    "', but found '" + parser.getName() + "'");
        }
        setName(parser.getAttributeValue(null, NAME_name));
        setActor(parser.getAttributeValue(null, NAME_actor));
        parser.next();
        XMLUtil.ensureTag(parser);
        while (parser.getEventType() != XmlPullParser.END_TAG && parser.getEventType() != XmlPullParser.END_DOCUMENT) {
            OverlayablePolicy overlayablePolicy = createNext();
            overlayablePolicy.parse(parser);
            XMLUtil.ensureTag(parser);
        }
        if (parser.getEventType() == XmlPullParser.END_TAG) {
            parser.next();
            XMLUtil.ensureTag(parser);
        }
    }
    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, TAG_overlayable);
        String name = getName();
        if (!TextUtils.isEmpty(name)) {
            serializer.attribute(null, NAME_name, name);
        }
        String actor = getActor();
        if (!TextUtils.isEmpty(actor)) {
            serializer.attribute(null, NAME_actor, actor);
        }
        for (OverlayablePolicy overlayablePolicy : this) {
            overlayablePolicy.serialize(serializer);
        }
        serializer.endTag(null, TAG_overlayable);
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NAME_name, getName());
        jsonObject.put(NAME_actor, getActor());
        JSONArray jsonArray = new JSONArray();
        for (OverlayablePolicy policy : this) {
            jsonArray.put(policy.toJson());
        }
        jsonObject.put(NAME_policies, jsonArray);
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setName(json.optString(NAME_name, null));
        setActor(json.optString(NAME_actor, null));
        JSONArray jsonArray = json.getJSONArray(NAME_policies);
        int length = jsonArray.length();
        for(int i = 0; i < length; i++) {
            createNext().fromJson(jsonArray.getJSONObject(i));
        }
    }
    public void merge(Overlayable overlayable){
        if(overlayable == null || overlayable == this){
            return;
        }
        setName(overlayable.getName());
        setActor(overlayable.getActor());
        for(OverlayablePolicy policy : overlayable) {
            OverlayablePolicy exist = getByFlag(policy.getFlags());
            if (exist == null) {
                exist = createNext();
            }
            exist.merge(policy);
        }
    }

    @Override
    public String toString() {
        return "name='" + getName()
                +"', actor='" + getActor()
                +"', policies=" + policyList.size();
    }

    public static final String NAME_name = ObjectsUtil.of("name");
    public static final String NAME_actor = ObjectsUtil.of("actor");
    public static final String NAME_policies = ObjectsUtil.of("policies");
    public static final String TAG_overlayable = ObjectsUtil.of("overlayable");
    public static final String FILE_NAME_XML = ObjectsUtil.of("overlayable.xml");

}
