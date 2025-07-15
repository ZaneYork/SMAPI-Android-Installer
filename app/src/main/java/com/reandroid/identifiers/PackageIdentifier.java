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
package com.reandroid.identifiers;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.io.IOUtil;
import com.reandroid.arsc.value.Entry;
import com.reandroid.json.JSONObject;
import com.reandroid.xml.XMLFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PackageIdentifier extends IdentifierMap<TypeIdentifier>{
    private PackageBlock mPackageBlock;
    private int mPackageLoadStamp;
    public PackageIdentifier(int id, String name){
        super(id, name);
    }
    public PackageIdentifier(){
        this(0, null);
    }

    private void initializePackageJson(PackageBlock packageBlock){
        File jsonFile = searchPackageJsonFromTag();
        if(jsonFile == null){
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(new FileInputStream(jsonFile));
            packageBlock.fromJson(jsonObject);
            if(getName() == null){
                setName(packageBlock.getName());
            }
        } catch (FileNotFoundException ignored) {
        }
    }
    // public.xml file is assumed to be stored via setTag during loadPublicXml(File)
    private File searchPackageJsonFromTag(){
        Object tag = getTag();
        if(!(tag instanceof File)){
            return null;
        }
        File publicXml = (File) tag;
        File dir = publicXml.getParentFile();
        //values
        if(dir == null || !"values".equals(dir.getName())){
            return null;
        }
        dir = dir.getParentFile();
        //res
        if(dir == null){
            return null;
        }
        dir = dir.getParentFile();
        if(dir == null){
            return null;
        }
        File json = new File(dir, "package.json");
        if(!json.isFile()){
            return null;
        }
        return json;
    }
    public List<ResourceIdentifier> listDuplicateResources(){
        List<ResourceIdentifier> results = new ArrayList<>();
        for(TypeIdentifier typeIdentifier : list()){
            results.addAll(typeIdentifier.listDuplicates());
        }
        return results;
    }
    public boolean hasDuplicateResources(){
        for(TypeIdentifier typeIdentifier : getItems()){
            if(typeIdentifier.hasDuplicates()){
                return true;
            }
        }
        return false;
    }
    public List<ResourceIdentifier> ensureUniqueResourceNames(){
        List<ResourceIdentifier> results = new ArrayList<>();
        for(TypeIdentifier typeIdentifier : list()){
            results.addAll(typeIdentifier.ensureUniqueResourceNames());
        }
        return results;
    }
    public void setResourceNamesToPackage(){
        setResourceNamesToPackage(getPackageBlock());
    }
    public void setResourceNamesToPackage(PackageBlock packageBlock){
        if(packageBlock == null){
            return;
        }
        for(SpecTypePair specTypePair:packageBlock.listSpecTypePairs()){
            Iterator<ResourceEntry> itr = specTypePair.getResources();
            while (itr.hasNext()){
                setResourceNamesToEntry(itr.next());
            }
        }
    }
    public void setResourceNamesToEntry(ResourceEntry resourceEntry){
        ResourceIdentifier ri = getResourceIdentifier(resourceEntry.getResourceId());
        if(ri == null){
            return;
        }
        resourceEntry.setName(ri.getName());
    }
    public ResourceIdentifier getResourceIdentifier(int resourceId){
        TypeIdentifier typeIdentifier = get((resourceId >> 16) & 0xff);
        if(typeIdentifier != null){
            return typeIdentifier.get(resourceId & 0xffff);
        }
        return null;
    }
    public ResourceIdentifier getResourceIdentifier(String type, String name){
        TypeIdentifier typeIdentifier = get(type);
        if(typeIdentifier != null){
            return typeIdentifier.get(name);
        }
        return null;
    }
    public int getResourcesCount(){
        int result = 0;
        for(TypeIdentifier ti : getItems()){
            result += ti.size();
        }
        return result;
    }

    public void writePublicXml(File file) throws IOException {
        XmlSerializer serializer = XMLFactory.newSerializer(file);
        write(serializer);
        IOUtil.close(serializer);
    }
    public void writePublicXml(OutputStream outputStream) throws IOException {
        XmlSerializer serializer = XMLFactory.newSerializer(outputStream);
        serializer.setOutput(outputStream, StandardCharsets.UTF_8.name());
        write(serializer);
    }
    public void write(XmlSerializer serializer) throws IOException {
        serializer.startDocument("utf-8", null);
        serializer.text("\n");
        serializer.startTag(null, XML_TAG_RESOURCES);
        writePackageInfo(serializer);
        writeTypes(serializer);
        serializer.text("\n");
        serializer.endTag(null, XML_TAG_RESOURCES);
        serializer.endDocument();
        closeSerializer(serializer);
    }
    private void writePackageInfo(XmlSerializer serializer) throws IOException {
        String name = getName();
        if(name != null){
            serializer.attribute(null, XML_ATTRIBUTE_PACKAGE, name);
        }
        int id = getId();
        if(id != 0){
            serializer.attribute(null, XML_ATTRIBUTE_ID, HexUtil.toHex2((byte)id));
        }
    }
    private void writeTypes(XmlSerializer serializer) throws IOException {
        for(TypeIdentifier typeIdentifier : list()){
            typeIdentifier.write(serializer);
        }
    }
    public void load(PackageBlock packageBlock){
        setId(packageBlock.getId());
        setName(packageBlock.getName());
        loadEntryGroups(packageBlock);
        setPackageBlock(packageBlock);
    }
    private void loadEntryGroups(PackageBlock packageBlock){
        for(SpecTypePair specTypePair:packageBlock.listSpecTypePairs()){
            Iterator<ResourceEntry> itr = specTypePair.getResources();
            while (itr.hasNext()){
                add(itr.next());
            }
        }
    }
    public void loadPublicXml(File file) throws IOException, XmlPullParserException {
        XmlPullParser parser = XMLFactory.newPullParser(file);
        loadPublicXml(parser);
    }
    public void loadPublicXml(InputStream inputStream) throws IOException, XmlPullParserException {
        XmlPullParser parser = XMLFactory.newPullParser(inputStream);
        loadPublicXml(parser);
    }
    public void loadPublicXml(Reader reader) throws IOException, XmlPullParserException {
        XmlPullParser parser = XMLFactory.newPullParser(reader);
        loadPublicXml(parser);
    }
    public void loadPublicXml(XmlPullParser parser) throws IOException, XmlPullParserException {
        boolean resourcesFound = false;
        int event;
        while ((event = parser.nextToken()) != XmlPullParser.END_DOCUMENT){
            if(event != XmlPullParser.START_TAG){
                continue;
            }
            if(!resourcesFound){
                resourcesFound = parser.getName().equals(XML_TAG_RESOURCES);
                if(!resourcesFound){
                    throw new XmlPullParserException("Invalid public.xml, expecting first tag '"
                            + getName() + "' " + parser.getPositionDescription());
                }
                loadPackageInfo(parser);
                continue;
            }
            parseEntry(parser);
        }
        closeParser(parser);
    }
    private void closeParser(XmlPullParser parser){
        if(!(parser instanceof Closeable)){
            return;
        }
        Closeable closeable = (Closeable)parser;
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }
    private void closeSerializer(XmlSerializer serializer){
        if(!(serializer instanceof Closeable)){
            return;
        }
        Closeable closeable = (Closeable)serializer;
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }
    private void loadPackageInfo(XmlPullParser parser){
        int count = parser.getAttributeCount();
        for(int i = 0; i < count; i++){
            if(XML_ATTRIBUTE_PACKAGE.equals(parser.getAttributeName(i))){
                setName(parser.getAttributeValue(i));
            }else if(XML_ATTRIBUTE_ID.equals(parser.getAttributeName(i))){
                int id = Integer.decode(parser.getAttributeValue(i));
                if(id != 0){
                    setId(id);
                }
            }
        }
    }
    private void parseEntry(XmlPullParser parser) throws XmlPullParserException {
        if(!XML_TAG_PUBLIC.equals(parser.getName())){
            throw new XmlPullParserException("Invalid tag, expecting '"
                    + XML_TAG_PUBLIC + "' " + parser.getPositionDescription());
        }
        String resourceIdStr = null;
        String typeName = null;
        String entryName = null;
        int count = parser.getAttributeCount();
        for(int i = 0; i < count; i++){
            String attrName = parser.getAttributeName(i);
            String value = parser.getAttributeValue(i);
            if(XML_ATTRIBUTE_ID.equals(attrName)){
                resourceIdStr = value;
            }else if(XML_ATTRIBUTE_TYPE.equals(attrName)){
                typeName = value;
            }else if(XML_ATTRIBUTE_NAME.equals(attrName)){
                entryName = value;
            }
        }
        if(typeName == null){
            throw new XmlPullParserException("Missing attribute '"
                    + XML_ATTRIBUTE_TYPE + "' " + parser.getPositionDescription());
        }
        if(resourceIdStr == null){
            throw new XmlPullParserException("Missing attribute '"
                    + XML_ATTRIBUTE_ID + "' " + parser.getPositionDescription());
        }
        if(entryName == null){
            throw new XmlPullParserException("Missing attribute '"
                    + XML_ATTRIBUTE_NAME + "' " + parser.getPositionDescription());
        }

        int resourceId = (int) Long.decode(resourceIdStr).longValue();
        int packageId = (resourceId >> 24) & 0xff;
        int typeId = (resourceId >> 16) & 0xff;
        int entryId = resourceId & 0xffff;

        TypeIdentifier typeIdentifier = getOrCreate(typeId, typeName);
        ResourceIdentifier entry = new ResourceIdentifier(entryId, entryName);
        typeIdentifier.add(entry);
        if(getId() == 0){
            setId(packageId);
        }
    }

    public PackageBlock getPackageBlock() {
        return mPackageBlock;
    }
    public void setPackageBlock(PackageBlock packageBlock) {
        this.mPackageBlock = packageBlock;
    }

    public void add(ResourceEntry resourceEntry){
        add(resourceEntry.get());
    }
    public void add(Entry entry){
        if(entry == null || entry.isNull()){
            return;
        }
        TypeBlock typeBlock = entry.getTypeBlock();
        TypeIdentifier typeIdentifier = getOrCreate(typeBlock.getId(), typeBlock.getTypeName());
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier(entry.getId(), entry.getName());
        typeIdentifier.add(resourceIdentifier);
    }
    private TypeIdentifier getOrCreate(int typeId, String typeName){
        TypeIdentifier identifier = get(typeId);
        if(identifier == null){
            return super.add(new TypeIdentifier(typeId, typeName));
        }
        if(typeName !=null && identifier.getName() == null){
            identifier.setName(typeName);
            identifier = super.add(identifier);
        }
        return identifier;
    }
    @Override
    public void clear(){
        for(TypeIdentifier identifier : getItems()){
            identifier.clear();
        }
        super.clear();
    }

    public int renameSpecs(){
        int result = 0;
        for(TypeIdentifier ti : getItems()){
            int renamed = ti.renameSpecs();
            result = result + renamed;
        }
        return result;
    }
    public int renameDuplicateSpecs(){
        int result = 0;
        for(TypeIdentifier ti : getItems()){
            int renamed = ti.renameDuplicateSpecs();
            result = result + renamed;
        }
        return result;
    }
    public int renameBadSpecs(){
        int result = 0;
        for(TypeIdentifier ti : getItems()){
            int renamed = ti.renameBadSpecs();
            result = result + renamed;
        }
        return result;
    }
    public String validateSpecNames(){
        int duplicates = renameDuplicateSpecs();
        int bad = renameBadSpecs();
        if(duplicates == 0 && bad == 0){
            return null;
        }
        return "Spec names validated, duplicates = " + duplicates
                + ", bad = " + bad;
    }
    @Override
    void setCaseInsensitive(boolean caseInsensitive){
        super.setCaseInsensitive(caseInsensitive);
        for(TypeIdentifier ti : getItems()){
            ti.setCaseInsensitive(caseInsensitive);
        }
    }
}
