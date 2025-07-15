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
package com.reandroid.arsc.value;

import com.reandroid.arsc.array.EntryArray;
import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.SpecBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.SpecFlag;
import com.reandroid.arsc.item.SpecString;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.model.ResourceName;
import com.reandroid.arsc.pool.SpecStringPool;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.graphics.AndroidColor;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.xml.StyleDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class Entry extends Block implements JSONConvert<JSONObject> {
    private TableEntry<?, ?> mTableEntry;
    private IntegerItem mNullSpecReference;

    public Entry(){
        super();
    }

    public Iterator<ValueItem> allValues(){
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry != null){
            return tableEntry.allValues();
        }
        return EmptyIterator.of();
    }
    public void linkTableStringsInternal(TableStringPool tableStringPool){
        TableEntry<?, ?> tableEntry = getTableEntry();
        tableEntry.linkTableStringsInternal(tableStringPool);
    }
    public void linkSpecStringsInternal(SpecStringPool specStringPool){
        TableEntry<?, ?> tableEntry = getTableEntry();
        ValueHeader header = tableEntry.getHeader();
        header.linkSpecStringsInternal(specStringPool);
    }
    public ResValue getResValue(){
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry instanceof ResTableEntry){
            return ((ResTableEntry)tableEntry).getValue();
        }
        return null;
    }
    public ResValueMapArray getResValueMapArray(){
        ResTableMapEntry resTableMapEntry = getResTableMapEntry();
        if(resTableMapEntry != null){
            return resTableMapEntry.getValue();
        }
        return null;
    }
    public ResTableMapEntry getResTableMapEntry(){
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry instanceof ResTableMapEntry){
            return ((ResTableMapEntry)tableEntry);
        }
        return null;
    }
    public String getXmlTag(){
        String tag = TypeString.toXmlTagName(getTypeName());
        if(tag == null || !tag.contains("array")){
            return tag;
        }
        ResTableMapEntry mapEntry = getResTableMapEntry();
        if(mapEntry == null){
            return tag;
        }
        ValueType allValueType = mapEntry.isAllSameValueType();
        if(allValueType == null){
            return tag;
        }
        if(allValueType == ValueType.STRING){
            return "string-" + tag;
        }
        if(allValueType == ValueType.DEC){
            return "integer-" + tag;
        }
        return tag;
    }
    public SpecFlag getSpecFlag(){
        SpecBlock specBlock = getSpecBlock();
        if(specBlock == null){
            return null;
        }
        return specBlock.getSpecFlag(getId());
    }
    public void ensureComplex(boolean isComplex){
        ensureTableEntry(isComplex);
    }
    public int getId(){
        int id = getIndex();
        EntryArray entryArray = getParentInstance(EntryArray.class);
        if(entryArray != null){
            id = entryArray.getEntryId(id);
        }
        return id;
    }
    /**
     * renames this entry and all configuration on this package
     * */
    public SpecString reName(String name){
        SpecTypePair specTypePair = getSpecTypePair();
        if(specTypePair == null){
            return null;
        }
        SpecString specString = null;
        Iterator<Entry> iterator = specTypePair.getEntries(getId(), false);
        while (iterator.hasNext()){
            Entry entry = iterator.next();
            if(specString == null){
                specString = entry.setName(name);
            }else {
                entry.updateSpecReference(specString);
            }
        }
        return specString;
    }
    public SpecString setName(String name){
        return setName(name, false);
    }
    /**
     * Sets resource entry name
     * */
    public SpecString setName(String name, boolean holdIfNull){
        if(name == null){
            unlinkNullSpecString();
            TableEntry<?, ?> tableEntry = getTableEntry();
            if(tableEntry != null){
                tableEntry.getHeader().setKey(null);
            }
            return null;
        }
        if(!holdIfNull && isNull()){
            return null;
        }
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock == null){
            return null;
        }
        SpecStringPool specStringPool = packageBlock.getSpecStringPool();
        int ref = getSpecReference();
        SpecString specString = specStringPool.get(ref);
        if(specString != null && name.equals(specString.get())){
            return null;
        }
        specString = specStringPool.getOrCreate(name);
        setSpecReference(specString);
        return specString;
    }
    public String getName(){
        SpecString specString = getSpecString();
        if(specString!=null){
            return specString.get();
        }
        return null;
    }
    public String getTypeName(){
        TypeBlock typeBlock = getTypeBlock();
        if(typeBlock!=null){
            return typeBlock.getTypeName();
        }
        return null;
    }
    public int getTypeId(){
        TypeBlock typeBlock = getTypeBlock();
        if(typeBlock != null){
            return typeBlock.getId();
        }
        return 0;
    }
    public int getResourceId(){
        TypeBlock typeBlock = getTypeBlock();
        if(typeBlock==null){
            return 0;
        }
        PackageBlock packageBlock = typeBlock.getPackageBlock();
        if(packageBlock == null){
            return 0;
        }
        return (packageBlock.getId()<<24)
                | (typeBlock.getId() << 16)
                | getId();
    }
    public int getSpecReference(){
        IntegerItem nullReference = this.mNullSpecReference;
        if(nullReference != null){
            return nullReference.get();
        }
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry != null){
            return tableEntry.getHeader().getKey();
        }
        return -1;
    }
    public TypeString getTypeString(){
        TypeBlock typeBlock = getTypeBlock();
        if(typeBlock!=null){
            return typeBlock.getTypeString();
        }
        return null;
    }
    public boolean isDefined(){
        return getSpecReference() != -1;
    }
    public boolean isDefault(){
        ResConfig resConfig = getResConfig();
        if(resConfig!=null){
            return resConfig.isDefault();
        }
        return false;
    }
    public void setSpecReference(SpecString specString){
        if(isSameSpecString(specString)){
            return;
        }
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry != null){
            tableEntry.getHeader().setKey(specString);
            unlinkNullSpecString();
            return;
        }
        linkNullSpecString(specString);
    }
    public void updateSpecReference(SpecString specString){
        if(isSameSpecString(specString)){
            return;
        }
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry != null){
            tableEntry.getHeader().setKey(specString);
            unlinkNullSpecString();
        }else if(mNullSpecReference != null){
            linkNullSpecString(specString);
        }else if(specString == null){
            unlinkNullSpecString();
        }
    }
    public void setSpecReference(int ref){
        if(ref == getSpecReference()){
            return;
        }
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry == null){
            linkNullSpecString(ref);
            return;
        }
        unlinkNullSpecString();
        tableEntry.getHeader().setKey(ref);
    }
    private boolean isSameSpecString(SpecString specString){
        int ref = getSpecReference();
        if(specString == null){
            return ref < 0;
        }
        if(ref != specString.getIndex()){
            return false;
        }
        return mNullSpecReference == null
                || getTableEntry() == null;
    }
    private void linkNullSpecString(int ref){
        if(ref < 0){
            unlinkNullSpecString();
            return;
        }
        SpecStringPool specStringPool = getSpecStringPool();
        if(specStringPool == null){
            unlinkNullSpecString();
            return;
        }
        linkNullSpecString(specStringPool.get(ref));
    }
    private void linkNullSpecString(SpecString specString){
        if(specString == null){
            unlinkNullSpecString();
            return;
        }
        IntegerItem nullReference = this.mNullSpecReference;
        if(nullReference != null && nullReference.get() == specString.getIndex()){
            return;
        }
        unlinkNullSpecString();
        nullReference = new IntegerItem();
        nullReference.setParent(this);
        nullReference.setIndex(1);
        nullReference.set(specString.getIndex());
        specString.addReference(nullReference);
        this.mNullSpecReference = nullReference;
    }
    private void unlinkNullSpecString(){
        IntegerItem nullReference = this.mNullSpecReference;
        if(nullReference == null){
            return;
        }
        SpecStringPool specStringPool = getSpecStringPool();
        if(specStringPool != null){
            specStringPool.removeReference(nullReference);
        }
        nullReference.setParent(null);
        nullReference.setIndex(-1);
        this.mNullSpecReference = null;
    }
    private SpecStringPool getSpecStringPool(){
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock != null){
            return packageBlock.getSpecStringPool();
        }
        return null;
    }
    public ValueType getValueType() {
        ResValue resValue = getResValue();
        if(resValue != null) {
            return resValue.getValueType();
        }
        return null;
    }
    public String getValueAsString() {
        ResValue resValue = getResValue();
        if(resValue != null) {
            return resValue.getValueAsString();
        }
        return null;
    }
    public StyleDocument getValueAsStyleDocument() {
        ResValue resValue = getResValue();
        if(resValue != null) {
            return resValue.getValueAsStyleDocument();
        }
        return null;
    }
    public Boolean getValueAsBoolean() {
        ResValue resValue = getResValue();
        if(resValue != null) {
            return resValue.getValueAsBoolean();
        }
        return null;
    }
    public AndroidColor getValueAsColor() {
        ResValue resValue = getResValue();
        if(resValue != null) {
            return resValue.getValueAsColor();
        }
        return null;
    }
    public Float getValueAsFloat() {
        ResValue resValue = getResValue();
        if(resValue != null) {
            return resValue.getValueAsFloat();
        }
        return null;
    }
    public Integer getValueAsInteger() {
        ResValue resValue = getResValue();
        if(resValue != null) {
            return resValue.getValueAsInteger();
        }
        return null;
    }
    public ResourceEntry getValueAsReference() {
        ResValue resValue = getResValue();
        if(resValue != null) {
            return resValue.getValueAsReference();
        }
        return null;
    }
    public ResValue setValueAsRaw(ValueType valueType, int data){
        ResValue resValue = ensureScalar();
        resValue.setTypeAndData(valueType, data);
        return resValue;
    }
    public ResValue setValueAsBoolean(boolean value){
        ResValue resValue = ensureScalar();
        resValue.setValueAsBoolean(value);
        return resValue;
    }
    public ResValue setValueAsReference(int resourceId){
        return setValueAsRaw(ValueType.REFERENCE, resourceId);
    }
    public ResValue setValueAsString(StyleDocument styledString){
        TableEntry<?, ?> tableEntry = ensureTableEntry(false);
        ResValue resValue = (ResValue) tableEntry.getValue();
        resValue.setValueAsString(styledString);
        return resValue;
    }
    public ResValue setValueAsString(String str){
        ResValue resValue = ensureScalar();
        resValue.setValueAsString(str);
        return resValue;
    }
    public ResValue setValueAsColor(AndroidColor color){
        ResValue resValue = ensureScalar();
        resValue.setValue(color);
        return resValue;
    }

    public SpecString getSpecString(){
        int ref = getSpecReference();
        if(ref < 0){
            return null;
        }
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock == null){
            return null;
        }
        SpecStringPool specStringPool = packageBlock.getSpecStringPool();
        return specStringPool.get(ref);
    }
    public ResConfig getResConfig(){
        TypeBlock typeBlock = getTypeBlock();
        if(typeBlock!=null){
            return typeBlock.getResConfig();
        }
        return null;
    }
    public SpecBlock getSpecBlock(){
        SpecTypePair specTypePair = getSpecTypePair();
        if(specTypePair != null){
            return specTypePair.getSpecBlock();
        }
        return null;
    }
    private SpecTypePair getSpecTypePair(){
        TypeBlock typeBlock = getTypeBlock();
        if(typeBlock != null){
            return typeBlock.getParentSpecTypePair();
        }
        return null;
    }
    public TypeBlock getTypeBlock(){
        return getParent(TypeBlock.class);
    }
    private String getPackageName(){
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock!=null){
            return packageBlock.getName();
        }
        return null;
    }
    public PackageBlock getPackageBlock(){
        return getParent(PackageBlock.class);
    }
    public ResourceEntry getResourceEntry() {
        return new ResourceEntry(getPackageBlock(), getResourceId());
    }
    public ResourceName getResourceName() {
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock == null) {
            return null;
        }
        String name = getName();
        if(name == null) {
            return null;
        }
        return new ResourceName(packageBlock.getName(), getTypeName(), name);
    }
    private ResValue ensureScalar() {
        return (ResValue) ensureTableEntry(false).getValue();
    }
    private TableEntry<?, ?> ensureTableEntry(boolean is_complex){
        TableEntry<?, ?> tableEntry = getTableEntry();

        boolean is_correct_type = (is_complex && tableEntry instanceof ResTableMapEntry) || (!is_complex && tableEntry instanceof ResTableEntry);
        if (tableEntry == null || !is_correct_type) {
            tableEntry = createTableEntry(is_complex);
            setTableEntry(tableEntry);
        }
        return tableEntry;
    }

    public TableEntry<?, ?> getTableEntry(){
        return mTableEntry;
    }
    public ValueHeader getHeader(){
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry!=null){
            return tableEntry.getHeader();
        }
        return null;
    }

    @Override
    public boolean isNull(){
        return getTableEntry()==null;
    }
    @Override
    public void setNull(boolean is_null){
        if(is_null){
            setTableEntry(null);
        }
    }
    @Override
    public byte[] getBytes() {
        if(isNull()){
            return null;
        }
        return getTableEntry().getBytes();
    }
    @Override
    public int countBytes() {
        if(isNull()){
            return 0;
        }
        return getTableEntry().countBytes();
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        counter.setCurrent(this);
        if(counter.END==this){
            counter.FOUND=true;
            return;
        }
        if(isNull()){
            return;
        }
        getTableEntry().onCountUpTo(counter);
    }
    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        if(isNull()){
            return 0;
        }
        return getTableEntry().writeBytes(stream);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        TableEntry<?, ?> tableEntry = createTableEntry(reader);
        setTableEntry(tableEntry);
        tableEntry.readBytes(reader);
    }

    public boolean isComplex(){
        return getTableEntry() instanceof CompoundEntry;
    }
    public boolean isScalar(){
        return getTableEntry() instanceof ResTableEntry;
    }
    public void setTableEntry(TableEntry<?, ?> tableEntry){
        if(tableEntry == this.mTableEntry){
            return;
        }
        onTableEntryRemoved();
        if(tableEntry==null){
            return;
        }
        tableEntry.setIndex(0);
        tableEntry.setParent(this);
        this.mTableEntry = tableEntry;
        transferSpecReference(tableEntry);
    }
    private void transferSpecReference(TableEntry<?, ?> tableEntry){
        IntegerItem nullSpecReference = this.mNullSpecReference;
        if(nullSpecReference == null){
            return;
        }
        int ref = nullSpecReference.get();
        unlinkNullSpecString();
        ValueHeader valueHeader = tableEntry.getHeader();
        if(valueHeader.getKey() < 0){
            valueHeader.setKey(ref);
        }
    }
    private void onTableEntryRemoved(){
        TableEntry<?, ?> exist = this.mTableEntry;
        if(exist == null){
            return;
        }
        exist.onRemoved();
        exist.setIndex(-1);
        exist.setParent(null);
        this.mTableEntry = null;
    }
    private TableEntry<?, ?> createTableEntry(BlockReader reader) throws IOException {
        int startPosition = reader.getPosition();
        reader.offset(2);
        boolean is_complex = (0x0001 & reader.readShort()) == 0x0001;
        reader.seek(startPosition);
        return createTableEntry(is_complex);
    }
    private TableEntry<?, ?> createTableEntry(boolean is_complex) {
        if(is_complex){
            return new ResTableMapEntry();
        }else {
            return new ResTableEntry();
        }
    }

    @Override
    public JSONObject toJson() {
        if(isNull()){
            return null;
        }
        return getTableEntry().toJson();
    }
    @Override
    public void fromJson(JSONObject json) {
        if(json==null){
            setNull(true);
            return;
        }
        boolean is_complex = json.optBoolean(ValueHeader.NAME_is_complex, false);
        TableEntry<?, ?> entry = createTableEntry(is_complex);
        setTableEntry(entry);
        entry.fromJson(json);
    }

    public ResourceEntry resolve(int resourceId) {
        PackageBlock packageBlock = getPackageBlock();
        return packageBlock.getTableBlock()
                .getResource(packageBlock, resourceId);
    }
    public void merge(Entry entry){
        if(canMerge(entry)){
            TableEntry<?, ?> tableEntry = entry.getTableEntry();
            TableEntry<?, ?> existEntry = ensureTableEntry(tableEntry instanceof ResTableMapEntry);
            existEntry.merge(tableEntry);
        }
    }
    public void mergeWithName(ResourceMergeOption mergeOption, Entry entry) {
        if(canMerge(entry)) {
            unlinkNullSpecString();
            TableEntry<?, ?> tableEntry = entry.getTableEntry();
            TableEntry<?, ?> existEntry = ensureTableEntry(tableEntry instanceof ResTableMapEntry);
            existEntry.mergeWithName(mergeOption, tableEntry);
        }
    }
    private boolean canMerge(Entry coming){
        if(coming == null || coming == this || coming.isNull()){
            return false;
        }
        if(this.isNull()){
            return true;
        }
        return getTableEntry().canMerge(coming.getTableEntry());
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(HexUtil.toHex8(getResourceId()));
        builder.append(' ');
        ResConfig resConfig = getResConfig();
        if(resConfig!=null){
            builder.append(resConfig);
            builder.append(' ');
        }
        SpecFlag specFlag = getSpecFlag();
        if(specFlag!=null){
            builder.append(specFlag);
            builder.append(' ');
        }
        if(isNull()){
            builder.append("NULL ");
        }
        builder.append('@');
        builder.append(getTypeName());
        builder.append('/');
        builder.append(getName());
        return builder.toString();
    }
    public static final String NAME_id = "id";
}
