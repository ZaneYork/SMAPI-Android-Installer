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
package com.reandroid.arsc.list;

import com.reandroid.arsc.chunk.Overlayable;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.xml.XMLUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class OverlayableList extends BlockList<Overlayable> implements
        Iterable<Overlayable>, JSONConvert<JSONArray> {

    public OverlayableList(){
        super();
    }

    public boolean isEmpty() {
        return size() == 0;
    }
    public Overlayable get(String name) {
        for(Overlayable overlayable : this){
            if(ObjectsUtil.equals(name, overlayable.getName())){
                return overlayable;
            }
        }
        return null;
    }
    public Overlayable get(String name, String actor) {
        for (Overlayable overlayable : this) {
            if (ObjectsUtil.equals(name, overlayable.getName()) ||
                    ObjectsUtil.equals(actor, overlayable.getActor())) {
                return overlayable;
            }
        }
        return null;
    }

    @Override
    public Overlayable createNext() {
        Overlayable overlayable = new Overlayable();
        add(overlayable);
        return overlayable;
    }

    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.getEventType() == XmlPullParser.START_DOCUMENT) {
            parser.next();
        }
        XMLUtil.ensureStartTag(parser);
        if (PackageBlock.TAG_resources.equals(parser.getName())) {
            parser.next();
            XMLUtil.ensureStartTag(parser);
        }
        while (parser.getEventType() != XmlPullParser.END_TAG &&
                parser.getEventType() != XmlPullParser.END_DOCUMENT) {
            Overlayable overlayable = createNext();
            overlayable.parse(parser);
            XMLUtil.ensureTag(parser);
        }
        if (parser.getEventType() == XmlPullParser.END_TAG) {
            parser.next();
            XMLUtil.ensureTag(parser);
        }
        if (parser.getEventType() == XmlPullParser.END_TAG && PackageBlock.TAG_resources.equals(parser.getName())) {
            parser.next();
            XMLUtil.ensureTag(parser);
        }
    }
    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.startDocument("utf-8", null);
        serializer.startTag(null, PackageBlock.TAG_resources);
        for (Overlayable overlayable : this) {
            overlayable.serialize(serializer);
        }
        serializer.endTag(null, PackageBlock.TAG_resources);
        serializer.endDocument();
    }
    @Override
    public JSONArray toJson() {
        if (isEmpty()) {
            return null;
        }
        JSONArray jsonArray = new JSONArray();
        for (Overlayable overlayable : this) {
            jsonArray.put(overlayable.toJson());
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        if(json == null) {
            return;
        }
        int length = json.length();
        for (int i = 0; i < length; i++) {
            createNext().fromJson(json.getJSONObject(i));
        }
    }
    public void merge(OverlayableList overlayableList) {
        if(overlayableList == null || overlayableList == this) {
            return;
        }
        for(Overlayable overlayable : overlayableList){
            Overlayable exist = get(overlayable.getName(), overlayable.getActor());
            if( exist == null) {
                exist = get(overlayable.getName());
            }
            if( exist == null) {
                exist = createNext();
            }
            exist.merge(overlayable);
        }
    }
}
