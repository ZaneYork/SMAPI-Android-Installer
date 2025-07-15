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
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.IterableIterator;
import com.reandroid.utils.collection.UniqueIterator;
import com.reandroid.xml.XMLFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

public class RSet implements Iterable<R> {

    private final ArrayCollection<R> rList;

    public RSet() {
        rList = new ArrayCollection<>();
    }

    @Override
    public Iterator<R> iterator() {
        return getRList().iterator();
    }
    public void clear() {
        getRList().clear();
    }
    public int size() {
        return getRList().size();
    }

    public List<DeclareStyleable> listDeclareStyleables(TableBlock tableBlock) {
        ArrayCollection<DeclareStyleable> styleableList = new ArrayCollection<>();
        styleableList.addAll(getDeclareStyleables(tableBlock));
        styleableList.sort(CompareUtil.getComparableComparator());
        return styleableList;
    }
    public Iterator<DeclareStyleable> getDeclareStyleables(TableBlock tableBlock) {
        return ComputeIterator.of(getRDeclareStyleables(),
                styleable -> styleable.toDeclareStyleable(tableBlock));
    }
    public Iterator<RDeclareStyleable> getRDeclareStyleables() {
        Iterator<RDeclareStyleable> iterator = new IterableIterator<R, RDeclareStyleable>(
                iterator()) {
            @Override
            public Iterator<RDeclareStyleable> iterator(R element) {
                return element.getRDeclareStyleables();
            }
        };
        return new UniqueIterator<>(iterator);
    }
    private ArrayCollection<R> getRList() {
        return rList;
    }

    public void load(DexClassRepository repository) {
        rList.addAll(R.findAll(repository));
    }

    public String toXml(TableBlock tableBlock) throws IOException {
        StringWriter writer = new StringWriter();
        XmlSerializer serializer = XMLFactory.newSerializer(writer);
        serialize(tableBlock, serializer);
        serializer.flush();
        writer.close();
        return writer.toString();
    }
    public void serialize(TableBlock tableBlock, XmlSerializer serializer) throws IOException {
        serializer.startDocument("utf8", null);
        XmlHelper.setIndent(serializer, true);
        String tag = "resources";
        serializer.startTag(null, tag);
        List<DeclareStyleable> styleableList = listDeclareStyleables(tableBlock);
        for (DeclareStyleable styleable : styleableList) {
            styleable.serialize(serializer);
        }
        XmlHelper.setIndent(serializer, true);
        serializer.endTag(null, tag);
        serializer.endDocument();
    }

    public static List<DeclareStyleable> readStyleables(DexClassRepository repository, TableBlock tableBlock) {
        RSet rSet = new RSet();
        rSet.load(repository);
        return rSet.listDeclareStyleables(tableBlock);
    }
}
