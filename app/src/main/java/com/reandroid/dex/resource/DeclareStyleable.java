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
package com.reandroid.dex.resource;

import com.reandroid.apk.XmlHelper;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.model.ResourceName;
import com.reandroid.arsc.value.AttributeDataFormat;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ArrayCollection;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class DeclareStyleable implements Comparable<DeclareStyleable>, Iterable<DeclareStyleable.Attr> {

    private final String name;
    private final ArrayCollection<Attr> attrList;

    public DeclareStyleable(String name) {
        this.name = name;
        this.attrList = new ArrayCollection<>();
    }

    public String getName() {
        return name;
    }
    public List<Attr> getAttrList() {
        return attrList;
    }
    @Override
    public Iterator<Attr> iterator() {
        return getAttrList().iterator();
    }
    public int size() {
        return getAttrList().size();
    }
    public boolean isEmpty() {
        return getAttrList().isEmpty();
    }
    public void add(Attr attr) {
        if(attr != null) {
            List<Attr> attrList = this.attrList;
            if(!attrList.contains(attr)) {
                attrList.add(attr);
            }
        }
    }

    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, TAG);
        serializer.attribute(null, "name", getName());
        for(Attr attr : this) {
            attr.serialize(serializer);
        }
        XmlHelper.setIndent(serializer, true);
        serializer.endTag(null, TAG);
    }
    @Override
    public int compareTo(DeclareStyleable declareStyleable) {
        if(declareStyleable == this) {
            return 0;
        }
        return CompareUtil.compare(getName(), declareStyleable.getName());
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DeclareStyleable that = (DeclareStyleable) obj;
        return ObjectsUtil.equals(this.getName(), that.getName()) &&
                this.getAttrList().equals(that.getAttrList());
    }

    @Override
    public int hashCode() {
        int result = ObjectsUtil.hash(getName());
        result = 31 * result + getAttrList().hashCode();
        return result;
    }

    public static void serialize(List<DeclareStyleable> styleableList, XmlSerializer serializer) throws IOException {
        serializer.startDocument("utf8", null);
        XmlHelper.setIndent(serializer, true);
        String tag = "resources";
        serializer.startTag(null, tag);
        for (DeclareStyleable styleable : styleableList) {
            styleable.serialize(serializer);
        }
        XmlHelper.setIndent(serializer, true);
        serializer.endTag(null, tag);
        serializer.endDocument();
    }
    public static class Attr {

        private final DeclareStyleable parent;
        private final int id;
        private final String name;
        private final String format;

        public Attr(DeclareStyleable parent, int id, String name, String format) {
            this.parent = parent;
            this.id = id;
            this.name = name;
            this.format = format;
        }

        public DeclareStyleable getParent() {
            return parent;
        }
        public int getId() {
            return id;
        }
        public String getName() {
            return name;
        }
        public String getFormat() {
            return format;
        }
        public int getFormatFlags() {
            return AttributeDataFormat.sum(AttributeDataFormat.parseValueTypes(getFormat()));
        }
        public int getIndex() {
            return getParent().getAttrList().indexOf(this);
        }
        public ResourceName toResourceName() {
            String name = getName();
            String packageName = null;
            int i = name.indexOf(':');
            if(i > 0) {
                packageName = name.substring(0, i);
                name = name.substring(i + 1);
            }
            return new ResourceName(packageName, "attr", name);
        }


        public void serialize(XmlSerializer serializer) throws IOException {
            String tag = "attr";
            serializer.startTag(null, tag);

            XmlHelper.setIndent(serializer, false);
            serializer.attribute(null, "name", getName());
            serializer.attribute(null, NAME_format, getFormat());
            XmlHelper.setIndent(serializer, true);

            serializer.endTag(null, tag);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Attr attr = (Attr) obj;
            return id == attr.id;
        }
        @Override
        public int hashCode() {
            return this.id;
        }
        @Override
        public String toString() {
            return "id=" + HexUtil.toHex8(id) +
                    ", name='" + name + '\'' +
                    ", format='" + format + '\'';
        }
    }
    public static final String TAG = ObjectsUtil.of("declare-styleable");
    public static final String NAME_format = ObjectsUtil.of("format");
}
