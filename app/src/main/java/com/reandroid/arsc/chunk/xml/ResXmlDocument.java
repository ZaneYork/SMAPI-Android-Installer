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
package com.reandroid.arsc.chunk.xml;

import com.reandroid.archive.InputSource;
import com.reandroid.arsc.ApkFile;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.*;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.header.InfoHeader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.common.BytesOutputStream;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.IterableIterator;
import com.reandroid.utils.collection.SingleIterator;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

public class ResXmlDocument extends Chunk<HeaderBlock>
        implements ResXmlNodeTree, MainChunk, ParentChunk, JSONConvert<JSONObject> {

    private final ResXmlStringPool mResXmlStringPool;
    private final ResXmlIDMap mResXmlIDMap;
    private final SingleBlockContainer<Block> mUnexpectedBlockContainer;
    private final BlockList<ResXmlNode> mNodeList;
    private ApkFile mApkFile;
    private PackageBlock mPackageBlock;
    private boolean mDestroyed;

    public ResXmlDocument() {
        super(new HeaderBlock(ChunkType.XML),3);

        this.mResXmlStringPool = new ResXmlStringPool(true);
        this.mResXmlIDMap = new ResXmlIDMap();
        this.mUnexpectedBlockContainer = new SingleBlockContainer<>();
        this.mNodeList = new BlockList<>();

        addChild(mResXmlStringPool);
        addChild(mResXmlIDMap);
        addChild(mUnexpectedBlockContainer);
        addChild(mNodeList);
        this.mNodeList.add(new ResXmlElement());
    }

    public SingleBlockContainer<Block> getUnexpectedBlockContainer() {
        return mUnexpectedBlockContainer;
    }

    @Override
    public BlockList<ResXmlNode> getNodeListBlockInternal() {
        return mNodeList;
    }

    /**
     * Iterates every attribute on root element and on child elements recursively
     * */
    public Iterator<ResXmlAttribute> recursiveAttributes() throws ConcurrentModificationException{
        return new IterableIterator<ResXmlElement, ResXmlAttribute>(getElements()) {
            @Override
            public Iterator<ResXmlAttribute> iterator(ResXmlElement element) {
                return element.recursiveAttributes();
            }
        };
    }
    /**
     * Iterates every xml node and child node recursively
     * */
    public Iterator<ResXmlNode> recursiveXmlNodes() throws ConcurrentModificationException{
        return new IterableIterator<ResXmlNode, ResXmlNode>(iterator()) {
            @Override
            public Iterator<ResXmlNode> iterator(ResXmlNode resXmlNode) {
                if(resXmlNode instanceof ResXmlElement) {
                    return ((ResXmlElement) resXmlNode).recursiveXmlNodes();
                }
                return SingleIterator.of(resXmlNode);
            }
        };
    }
    /**
     * Iterates every element and child elements recursively
     * */
    public Iterator<ResXmlElement> recursiveElements() throws ConcurrentModificationException{
        return new IterableIterator<ResXmlElement, ResXmlElement>(getElements()) {
            @Override
            public Iterator<ResXmlElement> iterator(ResXmlElement element) {
                return element.recursiveElements();
            }
        };
    }

    public int autoSetAttributeNamespaces() {
        return autoSetAttributeNamespaces(true);
    }
    public int autoSetAttributeNamespaces(boolean removeNoIdPrefix) {
        int changedCount = 0;
        Iterator<ResXmlElement> iterator = getElements();
        while (iterator.hasNext()) {
            ResXmlElement element = iterator.next();
            changedCount += element.autoSetAttributeNamespaces(removeNoIdPrefix);
        }
        if(changedCount > 0){
            removeUnusedNamespaces();
            getStringPool().removeUnusedStrings();
        }
        return changedCount;
    }
    public int autoSetAttributeNames() {
        return autoSetAttributeNames(true);
    }
    public int autoSetAttributeNames(boolean removeNoIdPrefix) {
        int changedCount = 0;
        Iterator<ResXmlElement> iterator = getElements();
        while (iterator.hasNext()) {
            changedCount += iterator.next().autoSetAttributeNames(removeNoIdPrefix);
        }
        if(changedCount > 0){
            removeUnusedNamespaces();
            getStringPool().removeUnusedStrings();
        }
        return changedCount;
    }
    public void autoSetLineNumber(){
        Iterator<ResXmlElement> iterator = getElements();
        while (iterator.hasNext()) {
           iterator.next().autoSetLineNumber();
        }
    }
    public int removeUnusedNamespaces() {
        int count = 0;
        Iterator<ResXmlElement> iterator = getElements();
        while (iterator.hasNext()) {
            count += iterator.next().removeUnusedNamespaces();
        }
        return count;
    }
    public String refreshFull(){
        int sizeOld = getHeaderBlock().getChunkSize();
        StringBuilder message = new StringBuilder();
        boolean appendOnce = false;
        int count;
        getStringPool().compressDuplicates();
        Iterator<ResXmlElement> iterator = getElements();
        while (iterator.hasNext()){
            ResXmlElement element = iterator.next();
            count = element.removeUndefinedAttributes();
            if(count != 0){
                message.append("Removed undefined attributes = ");
                message.append(count);
                appendOnce = true;
            }
        }
        count = removeUnusedNamespaces();
        if(count != 0){
            if(appendOnce){
                message.append("\n");
            }
            message.append("Removed unused namespaces = ");
            message.append(count);
            appendOnce = true;
        }
        if(getStringPool().removeUnusedStrings()){
            if(appendOnce){
                message.append("\n");
            }
            message.append("Removed unused xml strings");
            appendOnce = true;
        }
        refresh();
        int sizeNew = getHeaderBlock().getChunkSize();
        if(sizeOld != sizeNew){
            if(appendOnce){
                message.append("\n");
            }
            message.append("Xml size changed = ");
            message.append(sizeOld);
            message.append(", ");
            message.append(sizeNew);
            appendOnce = true;
        }
        if(appendOnce){
            return message.toString();
        }
        return null;
    }
    public void destroy(){
        synchronized (this){
            if(mDestroyed){
                return;
            }
            mDestroyed = true;
            mNodeList.clearChildes();
            getResXmlIDMap().destroy();
            getStringPool().clear();
            refresh();
        }
    }
    public void setAttributesUnitSize(int size, boolean setToAll){
        Iterator<ResXmlElement> iterator = getElements();
        while (iterator.hasNext()) {
            iterator.next().setAttributesUnitSize(size, setToAll);
        }
    }
    public ResXmlElement getOrCreateElement(String tag){
        ResXmlElement element = getElement(tag);
        if(element == null){
            element = createRootElement(tag);
        }else if(tag != null){
            element.setName(tag);
        }
        return element;
    }
    public ResXmlElement createRootElement(String tag){
        int lineNo = 1;
        ResXmlElement resXmlElement = newElement();
        resXmlElement.newStartElement(lineNo);

        if(tag != null){
            resXmlElement.setName(tag);
        }
        return resXmlElement;
    }
    void linkStringReferences(){
        Iterator<ResXmlElement> iterator = getElements();
        while (iterator.hasNext()) {
            iterator.next().linkStringReferences();
        }
    }
    @Override
    public byte[] getBytes(){
        BytesOutputStream outputStream = new BytesOutputStream(
                getHeaderBlock().getChunkSize());
        try {
            writeBytes(outputStream);
            outputStream.close();
        } catch (IOException ignored) {
        }
        return outputStream.toByteArray();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock = reader.readHeaderBlock();
        if(headerBlock == null){
            throw new IOException("Not bin xml: " + reader);
        }
        int chunkSize = headerBlock.getChunkSize();
        if(chunkSize < 0){
            throw new IOException("Negative chunk size: " + chunkSize);
        }
        if(chunkSize > reader.available()){
            throw new IOException("Higher chunk size: " + chunkSize
                    + ", available = " + reader.available());
        }
        if(chunkSize < headerBlock.getHeaderSize()){
            throw new IOException("Higher header size: " + headerBlock);
        }
        BlockReader chunkReader = reader.create(chunkSize);
        headerBlock = getHeaderBlock();
        headerBlock.readBytes(chunkReader);
        // android/aapt2 accepts 0x0000 (NULL) chunk type as XML, it could
        // be android's bug and might be fixed in the future until then lets fix it ourselves
        headerBlock.setType(ChunkType.XML);
        clear();
        while (chunkReader.isAvailable()){
            boolean readOk = readNext(chunkReader);
            if(!readOk){
                break;
            }
        }
        reader.offset(headerBlock.getChunkSize());
        chunkReader.close();
        onChunkLoaded();
    }
    @Override
    public void onChunkLoaded(){
        super.onChunkLoaded();
        linkStringReferences();
    }
    private boolean readNext(BlockReader reader) throws IOException {
        if(!reader.isAvailable()){
            return false;
        }
        int position=reader.getPosition();
        HeaderBlock headerBlock=reader.readHeaderBlock();
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        if (chunkType == ChunkType.STRING && mResXmlStringPool.size() == 0) {
            // If the string pool is not empty then it will be assumed that
            // it is already loaded and consume bytes as unexpected chunk,
            // same goes for ResXmlIDMap below
            mResXmlStringPool.readBytes(reader);
        } else if(chunkType == ChunkType.XML_RESOURCE_MAP && mResXmlIDMap.size() == 0) {
            mResXmlIDMap.readBytes(reader);
        } else if(isElementChunk(chunkType)){
            newElement().readBytes(reader);
            return reader.isAvailable();
        } else {
            readUnexpectedChunk(reader);
            // TODO find a way to warn or log that unexpected chunk is present
        }
        return reader.isAvailable() && position!=reader.getPosition();
    }
    @SuppressWarnings("unchecked")
    private void readUnexpectedChunk(BlockReader reader) throws IOException {
        UnknownChunk unknownChunk = new UnknownChunk();
        SingleBlockContainer<Block> container = getUnexpectedBlockContainer();
        Block prevUnknown = container.getItem();
        if (prevUnknown == null) {
            container.setItem(unknownChunk);
        } else {
            if (prevUnknown instanceof BlockList) {
                ((BlockList<Block>) prevUnknown).add(unknownChunk);
            } else {
                BlockList<Block> blockList = new BlockList<>();
                blockList.add(prevUnknown);
                blockList.add(unknownChunk);
                container.setItem(blockList);
            }
        }
        unknownChunk.readBytes(reader);
    }
    private boolean isElementChunk(ChunkType chunkType){
        if(chunkType==ChunkType.XML_START_ELEMENT){
            return true;
        }
        if(chunkType==ChunkType.XML_END_ELEMENT){
            return true;
        }
        if(chunkType==ChunkType.XML_START_NAMESPACE){
            return true;
        }
        if(chunkType==ChunkType.XML_END_NAMESPACE){
            return true;
        }
        if(chunkType==ChunkType.XML_CDATA){
            return true;
        }
        if(chunkType==ChunkType.XML_LAST_CHUNK){
            return true;
        }
        return false;
    }
    @Override
    public ResXmlStringPool getStringPool(){
        return mResXmlStringPool;
    }
    @Override
    public ApkFile getApkFile(){
        return mApkFile;
    }
    @Override
    public void setApkFile(ApkFile apkFile){
        this.mApkFile = apkFile;
    }
    @Override
    public PackageBlock getPackageBlock(){
        ApkFile apkFile = this.mApkFile;
        PackageBlock packageBlock = this.mPackageBlock;
        if(apkFile == null || packageBlock != null){
            return packageBlock;
        }
        TableBlock tableBlock = apkFile.getLoadedTableBlock();
        if(tableBlock != null){
            packageBlock = selectPackageBlock(tableBlock);
            mPackageBlock = packageBlock;
        }
        return packageBlock;
    }
    public void setPackageBlock(PackageBlock packageBlock) {
        this.mPackageBlock = packageBlock;
    }
    PackageBlock selectPackageBlock(TableBlock tableBlock){
        PackageBlock packageBlock = tableBlock.pickOne();
        if(packageBlock == null){
            packageBlock = tableBlock.pickOrEmptyPackage();
        }
        return packageBlock;
    }
    @Override
    public TableBlock getTableBlock(){
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock != null){
            TableBlock tableBlock = packageBlock.getTableBlock();
            if(tableBlock != null){
                return tableBlock;
            }
        }
        ApkFile apkFile = getApkFile();
        if(apkFile != null){
            return apkFile.getLoadedTableBlock();
        }
        return null;
    }
    @Override
    public StringPool<?> getSpecStringPool() {
        return null;
    }
    @Override
    public MainChunk getMainChunk(){
        return this;
    }
    public ResXmlIDMap getResXmlIDMap(){
        return mResXmlIDMap;
    }
    public ResXmlElement getDocumentElement(){
        return CollectionUtil.getFirst(getElements());
    }
    public ResXmlElement newElement() {
        clearEmptyElements();
        ResXmlElement element = new ResXmlElement();
        add(element);
        return element;
    }
    public void addElement(int index, ResXmlElement element) {
        clearEmptyElements();
        this.add(index, element);
    }
    public void clearEmptyElements() {
        this.removeIf(xmlNode -> {
            if(xmlNode instanceof ResXmlElement) {
                return ((ResXmlElement) xmlNode).isUndefined();
            }
            return false;
        });
    }
    @Override
    protected void onPreRefresh(){
        clearEmptyElements();
        getNodeListBlockInternal().refresh();
        super.onPreRefresh();
    }
    @Override
    protected void onChunkRefreshed() {

    }
    public void readBytes(File file) throws IOException{
        BlockReader reader=new BlockReader(file);
        super.readBytes(reader);
    }
    public void readBytes(InputStream inputStream) throws IOException{
        BlockReader reader=new BlockReader(inputStream);
        super.readBytes(reader);
    }
    public final int writeBytes(File file) throws IOException{
        if(isNull()){
            throw new IOException("Can NOT save null block");
        }
        File dir=file.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        OutputStream outputStream=new FileOutputStream(file);
        int length = super.writeBytes(outputStream);
        outputStream.close();
        return length;
    }
    public void mergeWithName(ResourceMergeOption mergeOption, ResXmlDocument document) {
        if(document == this){
            return;
        }
        Iterator<ResXmlElement> iterator = document.getElements();
        while (iterator.hasNext()){
            ResXmlElement coming = iterator.next();
            ResXmlElement element = getOrCreateElement(coming.getName());
            element.mergeWithName(mergeOption, coming);
        }
    }
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        if(mDestroyed){
            throw new IOException("Destroyed document");
        }
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock == null){
            throw new IOException("Can not decode without package");
        }
        setPackageBlock(packageBlock);
        int event = parser.getEventType();
        if(event == XmlPullParser.START_DOCUMENT){
            clear();
            parser.next();
        }
        while (parseNext(parser)){
            parser.next();
        }
        refreshFull();
    }
    private boolean parseNext(XmlPullParser parser) throws IOException, XmlPullParserException {
        int event = parser.getEventType();
        while (event != XmlPullParser.START_TAG && event != XmlPullParser.END_DOCUMENT){
            event = parser.next();
        }
        if(event == XmlPullParser.START_TAG){
            newElement().parse(parser);
            return true;
        }
        return false;
    }
    public String serializeToXml() throws IOException {
        StringWriter writer = new StringWriter();
        XmlSerializer serializer = XMLFactory.newSerializer(writer);
        serialize(serializer);
        serializer.flush();
        writer.flush();
        writer.close();
        return writer.toString();
    }
    public void serialize(XmlSerializer serializer) throws IOException {
        serialize(serializer, true);
    }
    public void serialize(XmlSerializer serializer, boolean decode) throws IOException {
        if(mDestroyed){
            throw new IOException("Destroyed document");
        }
        PackageBlock packageBlock = getPackageBlock();
        if(decode && packageBlock == null) {
            throw new IOException("Can not decode without package");
        }
        ResXmlElement.setIndent(serializer, true);
        serializer.startDocument("utf-8", null);
        autoSetAttributeNamespaces();
        for (ResXmlNode xmlNode : this) {
            xmlNode.serialize(serializer, decode);
        }
        serializer.endDocument();
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ResXmlDocument.NAME_element, getDocumentElement().toJson());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        ResXmlElement xmlElement = getDocumentElement();
        xmlElement.fromJson(json.optJSONObject(ResXmlDocument.NAME_element));
        refresh();
    }
    public XMLDocument decodeToXml() {
        return toXml(true);
    }
    public XMLDocument toXml() {
        return toXml(false);
    }
    public XMLDocument toXml(boolean decode) {
        XMLDocument xmlDocument = new XMLDocument();
        xmlDocument.setEncoding("utf-8");
        for (ResXmlNode node : this) {
            xmlDocument.add(node.toXml(decode));
        }
        return xmlDocument;
    }
    void addEvents(ParserEventList parserEventList){
        ResXmlElement xmlElement = getDocumentElement();
        parserEventList.add(new ParserEvent(ParserEvent.START_DOCUMENT, xmlElement));
        Iterator<ResXmlElement> iterator = getElements();
        while (iterator.hasNext()) {
            ResXmlElement element = iterator.next();
            element.addEvents(parserEventList);
        }
        parserEventList.add(new ParserEvent(ParserEvent.END_DOCUMENT, xmlElement));
    }

    public static boolean isResXmlBlock(File file){
        if(file==null){
            return false;
        }
        try {
            InfoHeader infoHeader = InfoHeader.readHeaderBlock(file);
            return isResXmlBlock(infoHeader);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(InputSource inputSource) {
        boolean result = false;
        try {
            InputStream inputStream = inputSource.openStream();
            result = isResXmlBlock(inputStream);
            inputStream.close();
        } catch (IOException ignored) {
        }
        return result;
    }
    public static boolean isResXmlBlock(InputStream inputStream) {
        try {
            HeaderBlock headerBlock = BlockReader.readHeaderBlock(inputStream);
            return isResXmlBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(byte[] bytes){
        try {
            HeaderBlock headerBlock = BlockReader.readHeaderBlock(bytes);
            return isResXmlBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(BlockReader blockReader){
        if(blockReader==null){
            return false;
        }
        try {
            HeaderBlock headerBlock = blockReader.readHeaderBlock();
            return isResXmlBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(HeaderBlock headerBlock){
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        return chunkType==ChunkType.XML;
    }
    private static final String NAME_element = "element";
}
