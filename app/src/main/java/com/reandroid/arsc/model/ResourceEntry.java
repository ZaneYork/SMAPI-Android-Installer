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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.item.SpecString;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.arsc.value.ResValue;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.arsc.value.attribute.AttributeBag;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.collection.*;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

public class ResourceEntry implements Iterable<Entry>{
    private final int resourceId;
    private final PackageBlock packageBlock;

    public ResourceEntry(PackageBlock packageBlock, int resourceId){
        this.resourceId = resourceId;
        this.packageBlock = packageBlock;
    }

    public Iterator<String> getStringValues() {
        return this.getStringValues(this.iterator());
    }
    Iterator<String> getStringValues(Iterator<Entry> iterator) {
        return new IterableIterator<Entry, String>(iterator) {
            public Iterator<String> iterator(Entry element) {
                return getStringValues(element);
            }
        };
    }
    Iterator<String> getStringValues(Entry entry) {
        ResValue resValue = entry.getResValue();
        if (resValue == null) {
            return EmptyIterator.of();
        }
        ValueType valueType = resValue.getValueType();
        if (valueType == ValueType.STRING) {
            return SingleIterator.of(resValue.getValueAsString());
        }
        if(!valueType.isReference()) {
            return EmptyIterator.of();
        }
        TableBlock tableBlock = getPackageBlock().getTableBlock();
        if(tableBlock == null) {
            return EmptyIterator.of();
        }
        return this.getStringValues(tableBlock.resolveReference(resValue.getData()).iterator());
    }

    public ResourceEntry previous(){
        int id = getResourceId();
        int entryId = id & 0xffff;
        if(entryId == 0){
            return null;
        }
        entryId = entryId - 1;
        id = id & 0xffff0000;
        id = id | entryId;
        return new ResourceEntry(getPackageBlock(), id);
    }
    public ResourceEntry next(){
        int id = getResourceId();
        int entryId = id & 0xffff;
        if(entryId == 0xffff){
            return null;
        }
        PackageBlock packageBlock = getPackageBlock();
        SpecTypePair specTypePair = packageBlock.getSpecTypePair((id >> 16) & 0xff);
        if(specTypePair == null){
            return null;
        }
        entryId = entryId + 1;
        return specTypePair.getResource(entryId);
    }
    public ResourceEntry getLast(){
        PackageBlock packageBlock = getPackageBlock();
        SpecTypePair specTypePair = packageBlock.getSpecTypePair((getResourceId() >> 16) & 0xff);
        if(specTypePair != null){
            return specTypePair.getResource(specTypePair.getHighestEntryId());
        }
        return null;
    }
    public ResourceEntry resolveReference(){
        Set<Integer> processedIds = new HashSet<>();
        processedIds.add(0);
        processedIds.add(getResourceId());
        ResourceEntry resolved = resolveReference(processedIds);
        if(resolved != null){
            return resolved;
        }
        return this;
    }
    private ResourceEntry resolveReference(Set<Integer> processedIds){
        Entry entry = get();
        if(entry == null){
            return this;
        }
        ResValue resValue = entry.getResValue();
        if(resValue == null){
            return this;
        }
        ValueType valueType = resValue.getValueType();
        if(valueType == null || !valueType.isReference()){
            return this;
        }
        int id = resValue.getData();
        if(id == 0 || processedIds.contains(id)){
            return null;
        }
        processedIds.add(id);
        ResourceEntry resourceEntry = getResourceEntry(id);
        if(resourceEntry != null){
            ResourceEntry resolved = resourceEntry.resolveReference(processedIds);
            if(resolved != null){
                resourceEntry = resolved;
            }
        }
        return resourceEntry;
    }
    private ResourceEntry getResourceEntry(int id){
        PackageBlock packageBlock = getPackageBlock();
        TableBlock tableBlock = packageBlock.getTableBlock();
        if(tableBlock == null){
            return null;
        }
        return tableBlock.getResource(packageBlock, id);
    }

    public Entry getOrCreate(String qualifiers){
        return getOrCreate(ResConfig.parse(qualifiers));
    }
    public Entry getOrCreate(ResConfig resConfig){
        int resourceId = this.getResourceId();
        byte typeId = (byte)((resourceId >> 16) & 0xff);
        short entryId = (short)(resourceId & 0xffff);
        Entry entry = getPackageBlock().getOrCreateEntry(typeId, entryId, resConfig);
        String name = getName();
        if(name != null && entry.getName() ==  null){
            entry.setName(name, true);
        }
        return entry;
    }
    public Entry get(String qualifiers){
        return get(ResConfig.parse(qualifiers));
    }
    public Entry get(ResConfig resConfig){
        for(Entry entry : this){
            if(resConfig.equals(entry.getResConfig())){
                return entry;
            }
        }
        return null;
    }
    public int getConfigsCount(){
        return CollectionUtil.count(iterator(true));
    }
    public Entry getEqualsOrMoreSpecific(ResConfig resConfig){
        Entry result = null;
        for(Entry entry : this){
            if(resConfig.equals(entry.getResConfig())){
                return entry;
            }
            if(result != null){
                continue;
            }
            if(entry.getResConfig().isEqualOrMoreSpecificThan(resConfig)){
                result = entry;
            }
        }
        return result;
    }
    public Entry get(){
        Entry result = null;
        for(Entry entry : this){
            if(entry.isDefault()){
                return entry;
            }
            if(result == null){
                result = entry;
            }
        }
        return result;
    }
    public Entry any(){
        Iterator<Entry> iterator = iterator(true);
        if(iterator.hasNext()){
            return iterator.next();
        }
        return null;
    }
    public boolean isEmpty() {
        return CollectionUtil.isEmpty(iterator(true));
    }
    public boolean isDefined() {
        return iterator(true).hasNext();
    }
    public boolean isDeclared() {
        return getName() != null;
    }
    public PackageBlock getPackageBlock(){
        return packageBlock;
    }
    public boolean isContext(Block block) {
        if(block == null){
            return false;
        }
        if(block instanceof TableBlock) {
            PackageBlock packageBlock = getPackageBlock();
            return packageBlock != null && block == packageBlock.getTableBlock();
        }
        if(block instanceof PackageBlock) {
            return isContext((PackageBlock) block);
        }
        return isContext(block.getParentInstance(PackageBlock.class));
    }
    public boolean isContext(PackageBlock packageBlock) {
        if(packageBlock == null){
            return false;
        }
        PackageBlock context = getPackageBlock();
        if(context == null){
            return false;
        }
        return context == packageBlock ||
                context.getTableBlock() == packageBlock.getTableBlock();
    }
    public int getResourceId() {
        return resourceId;
    }
    public String getPackageName(){
        return getPackageBlock().getName();
    }
    public String getType(){
        return getPackageBlock().typeNameOf((getResourceId() >> 16) & 0xff);
    }
    public void setName(String name){
        boolean hasEntry = false;
        SpecString specString = null;
        for (Entry entry : this) {
            if (specString != null) {
                entry.setSpecReference(specString);
                continue;
            }
            specString = entry.setName(name);
            hasEntry = true;
        }
        if(hasEntry){
            return;
        }
        Iterator<Entry> itr = iterator(false);
        if(!itr.hasNext()){
            return;
        }
        Entry entry = itr.next();
        entry.setName(name, true);
    }
    public String getName(){
        Iterator<Entry> itr = iterator(false);
        while (itr.hasNext()) {
            Entry entry = itr.next();
            String name = entry.getName();
            if (name != null) {
                return name;
            }
        }
        return null;
    }

    @Override
    public Iterator<Entry> iterator(){
        return iterator(true);
    }
    public Iterator<Entry> iterator(boolean skipNull){
        return getPackageBlock().getEntries(getResourceId(), skipNull);
    }
    public Iterator<Entry> iterator(Predicate<? super Entry> filter) {
        return new FilterIterator<>(getPackageBlock().getEntries(getResourceId()), filter);
    }
    public Iterator<ResConfig> getConfigs(){
        return new ComputeIterator<>(iterator(false), Entry::getResConfig);
    }
    public String getHexId(){
        return HexUtil.toHex8(getResourceId());
    }
    public ResourceName toResourceName() {
        String name = getName();
        if(name == null) {
            return null;
        }
        return new ResourceName(getPackageName(), getType(), name);
    }
    public String buildReference(){
        return buildReference(getPackageBlock(), null);
    }
    public String buildReference(PackageBlock context){
        return buildReference(context, null);
    }
    public String buildReference(PackageBlock context, ValueType referenceType){
        StringBuilder builder = new StringBuilder();
        if(referenceType != null){
            if(referenceType == ValueType.REFERENCE){
                builder.append('@');
            }else {
                builder.append('?');
            }
        }
        PackageBlock packageBlock = getPackageBlock();
        if(context != packageBlock && !packageBlock.isEmpty()){
            String packageName = getPackageName();
            if(packageName != null){
                builder.append(packageName);
                builder.append(':');
            }
        }
        builder.append(getType());
        builder.append('/');
        builder.append(getName());
        return builder.toString();
    }
    public String decodeAttributeData(int data){
        Entry entry = get();
        if(entry == null){
            return null;
        }
        AttributeBag attributeBag = AttributeBag.create(entry.getResValueMapArray());
        if(attributeBag != null){
            return attributeBag.decodeAttributeValue(data);
        }
        return null;
    }

    public boolean serializePublicXml(XmlSerializer serializer) throws IOException {
        String name = getName();
        if(name == null){
            return false;
        }
        serializer.text("\n  ");
        serializer.startTag(null, PackageBlock.TAG_public);
        serializer.attribute(null, "id", getHexId());
        serializer.attribute(null, "type", getType());
        serializer.attribute(null, "name", name);
        serializer.endTag(null, PackageBlock.TAG_public);
        return true;
    }

    @Override
    public int hashCode(){
        return getResourceId();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ResourceEntry)) {
            return false;
        }
        ResourceEntry other = (ResourceEntry) obj;
        return this.getResourceId() == other.getResourceId();
    }

    @Override
    public String toString(){
        String packageName = getPackageName();
        if(packageName == null){
            return getHexId() + " @" + getType() + "/" + getName();
        }
        return getHexId() + " @" + packageName
                + ":" + getType() + "/" + getName();
    }

}
