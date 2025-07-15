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
package com.reandroid.arsc.model;

import android.content.res.XmlResourceParser;
import com.reandroid.arsc.ApkFile;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.ResXmlPullParser;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

/**
 * A front end class to deliver similar functionality to android.content.res.Resources
 * */
public class ResourceTable implements Iterable<ResourcePackage> {

    private final TableBlock tableBlock;

    public ResourceTable(TableBlock tableBlock) {
        this.tableBlock = tableBlock;
    }

    public XmlResourceParser getLayout(int resourceId) throws IOException {
        return getParser(resourceId);
    }
    public XmlResourceParser getXml(int resourceId) throws IOException {
        return getParser(resourceId);
    }
    public XmlResourceParser getParser(int resourceId) throws IOException {
        ApkFile apkFile = getApkFile();
        if(apkFile == null) {
            throw new FileNotFoundException("Missing apk file");
        }
        Iterator<String> iterator = getStrings(resourceId);
        while (iterator.hasNext()) {
            String path = iterator.next();
            if(apkFile.containsFile(path)) {
                return loadXml(path);
            }
        }
        throw new FileNotFoundException("No resource found for: " + HexUtil.toHex8(resourceId));
    }
    public Iterator<XmlResourceParser> getParsers(int resourceId) {
        return ComputeIterator.of(getStrings(resourceId), path -> {
            try {
                return loadXml(path);
            } catch (IOException ignored) {
                return null;
            }
        });
    }
    XmlResourceParser loadXml(String path) throws IOException {
        ApkFile apkFile = getApkFile();
        if(apkFile == null || !apkFile.containsFile(path)) {
            throw new FileNotFoundException("Missing apk file");
        }
        ResXmlPullParser parser = new ResXmlPullParser();
        parser.setResXmlDocument(apkFile.loadResXmlDocument(path));
        return parser;
    }
    public String getString(int resourceId) {
        Iterator<String> iterator = getStrings(resourceId);
        if(iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }
    public Iterator<String> getStrings(int resourceId) {
        ResourceEntry resourceEntry = getResource(resourceId);
        if(resourceEntry != null) {
            return resourceEntry.getStringValues();
        }
        return EmptyIterator.of();
    }
    public int getIdentifier(String name, String type, String packageName) {
        ResourceEntry resourceEntry = getResource(packageName, type, name);
        if(resourceEntry != null) {
            return resourceEntry.getResourceId();
        }
        return 0;
    }
    public ResourceEntry getResource(String name, String type, String packageName) {
        return getTableBlock().getResource(packageName, type, name);
    }
    public ResourceEntry getResource(int resourceId) {
        return getTableBlock().getResource(resourceId);
    }

    @Override
    public Iterator<ResourcePackage> iterator() {
        return ComputeIterator.of(getTableBlock().iterator(), ResourcePackage::new);
    }
    public int size() {
        return getTableBlock().size();
    }
    private ApkFile getApkFile() {
        return getTableBlock().getApkFile();
    }
    public TableBlock getTableBlock() {
        return tableBlock;
    }
    @Override
    public int hashCode() {
        return getTableBlock().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ResourceTable)) {
            return false;
        }
        ResourceTable table = (ResourceTable) obj;
        if(size() != table.size()) {
            return false;
        }
        Iterator<ResourcePackage> iterator = iterator();
        Iterator<ResourcePackage> others = table.iterator();
        while (iterator.hasNext() && others.hasNext()) {
            if(!iterator.next().equals(others.next())) {
                return false;
            }
        }
        return !iterator.hasNext() && others.hasNext();
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("packages = ");
        builder.append(size());
        builder.append('[');
        boolean append = false;
        for(ResourcePackage resourcePackage : this) {
            if(append) {
                builder.append(", ");
            }
            builder.append(resourcePackage.getName());
            append = true;
        }
        builder.append(']');
        return builder.toString();
    }

}
