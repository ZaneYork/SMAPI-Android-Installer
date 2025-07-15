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

import android.text.TextUtils;

import com.reandroid.arsc.array.ResXmlAttributeArray;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ResXmlString;
import com.reandroid.arsc.model.ResourceLibrary;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.common.Namespace;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.*;
import com.reandroid.xml.XMLAttribute;
import com.reandroid.xml.XMLComment;
import com.reandroid.xml.XMLElement;
import com.reandroid.xml.XMLUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class ResXmlElement extends ResXmlNode implements
        ResXmlNodeTree, JSONConvert<JSONObject>, Comparator<ResXmlNode> {

    private final BlockList<ResXmlStartNamespace> mStartNamespaceList;
    private final SingleBlockContainer<ResXmlStartElement> mStartElementContainer;
    private final BlockList<ResXmlNode> mBody;
    private final SingleBlockContainer<ResXmlEndElement> mEndElementContainer;
    private final BlockList<ResXmlEndNamespace> mEndNamespaceList;

    public ResXmlElement() {
        super(5);

        this.mStartNamespaceList = new BlockList<>();
        this.mStartElementContainer= new SingleBlockContainer<>();
        this.mBody = new BlockList<>();
        this.mEndElementContainer = new SingleBlockContainer<>();
        this.mEndNamespaceList = new BlockList<>();

        addChild(0, mStartNamespaceList);
        addChild(1, mStartElementContainer);
        addChild(2, mBody);
        addChild(3, mEndElementContainer);
        addChild(4, mEndNamespaceList);
    }

    @Override
    public BlockList<ResXmlNode> getNodeListBlockInternal() {
        return mBody;
    }

    public boolean isUndefined() {
        if (size() != 0 ||
                this.getAttributeCount() != 0 ||
                this.getNamespaceCount() != 0) return false;
        String text = getName();
        return TextUtils.isEmpty(text);
    }
    public ResXmlElement getParentElement(){
        return getParentInstance(ResXmlElement.class);
    }
    public ResXmlDocument getParentDocument(){
        return getParentInstance(ResXmlDocument.class);
    }
    public ResXmlElement getRootElement(){
        ResXmlElement parent = getParentElement();
        if(parent != null){
            return parent.getRootElement();
        }
        return this;
    }
    public ResXmlNamespace getNamespace(){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getResXmlStartNamespace();
        }
        return null;
    }
    public void addIndent(int scale){
        if(hasText()){
            return;
        }
        int depth = getDepth();
        if(depth > MAX_INDENT_DEPTH){
            depth = MAX_INDENT_DEPTH;
        }
        int indent = (depth + 1) * scale;
        ResXmlTextNode textNode = null;
        for (ResXmlElement element : listElements()){
            textNode = createResXmlTextNode(element.getIndex());
            textNode.makeIndent(indent);
            element.addIndent(scale);
        }
        if(textNode != null){
            indent = depth * scale;
            textNode = new ResXmlTextNode();
            addNode(textNode);
            textNode.makeIndent(indent);
        }
    }
    public int clearIndents(){
        return removeIf(resXmlNode -> {
            if(resXmlNode instanceof ResXmlTextNode){
                return  ((ResXmlTextNode) resXmlNode).isIndent();
            }
            return false;
        });
    }
    /**
     * Iterates every xml-nodes (ResXmlElement & ResXmlTextNode) and child nodes recursively
     *
     * */
    public Iterator<ResXmlNode> recursiveXmlNodes() throws ConcurrentModificationException{
        return CombiningIterator.of(SingleIterator.of(this),
                ComputeIterator.of(iterator(), xmlNode -> {
                    if(xmlNode instanceof ResXmlElement){
                        return ((ResXmlElement) xmlNode).recursiveXmlNodes();
                    }
                    return SingleIterator.of(xmlNode);
                }));
    }
    /**
     * Iterates every attribute on this element and on child elements recursively
     * */
    public Iterator<ResXmlAttribute> recursiveAttributes() throws ConcurrentModificationException {
        return RecursiveIterator.compute(this, ResXmlElement::getElements, ResXmlElement::getAttributes);
    }
    /**
     * Iterates every element and child elements recursively
     * */
    public Iterator<ResXmlElement> recursiveElements(){
        return RecursiveIterator.of(this, ResXmlElement::getElements);
    }
    public ResXmlAttribute getIdAttribute(){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getIdAttribute();
        }
        return null;
    }
    public ResXmlAttribute getClassAttribute(){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getClassAttribute();
        }
        return null;
    }
    public ResXmlAttribute getStyleAttribute(){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getStyleAttribute();
        }
        return null;
    }
    public ResXmlNamespace getNamespaceAt(int i){
        return mStartNamespaceList.get(i);
    }
    public int getNamespaceCount(){
        return mStartNamespaceList.size();
    }
    public ResXmlNamespace getNamespace(String uri, String prefix){
        return getXmlStartNamespace(uri, prefix);
    }
    public ResXmlNamespace getOrCreateNamespace(String uri, String prefix){
        return getOrCreateXmlStartNamespace(uri, prefix);
    }
    public ResXmlNamespace newNamespace(String uri, String prefix){
        return createXmlStartNamespace(uri, prefix);
    }
    public ResXmlNamespace getNamespaceByUri(String uri){
        return getStartNamespaceByUri(uri);
    }
    public ResXmlNamespace getNamespaceByPrefix(String prefix){
        return getStartNamespaceByPrefix(prefix, null);
    }
    public ResXmlNamespace getOrCreateNamespaceByPrefix(String prefix){
        if(prefix == null || prefix.trim().length() == 0){
            return null;
        }
        ResXmlNamespace namespace = getNamespaceByPrefix(prefix);
        if(namespace != null){
            return namespace;
        }
        String uri;
        if(ResourceLibrary.PREFIX_ANDROID.equals(prefix)){
            uri = ResourceLibrary.URI_ANDROID;
        }else {
            uri = ResourceLibrary.URI_RES_AUTO;
        }
        return getOrCreateNamespace(uri, prefix);
    }
    public int autoSetAttributeNamespaces() {
        return autoSetAttributeNamespaces(true);
    }
    public int autoSetAttributeNamespaces(boolean removeNoIdPrefix) {
        int changedCount = 0;
        Iterator<ResXmlAttribute> attributes = getAttributes();
        while (attributes.hasNext()){
            ResXmlAttribute attribute = attributes.next();
            boolean changed = attribute.autoSetNamespace(removeNoIdPrefix);
            if(changed){
                changedCount ++;
            }
        }
        Iterator<ResXmlElement> iterator = getElements();
        while (iterator.hasNext()) {
            changedCount += iterator.next().autoSetAttributeNamespaces(removeNoIdPrefix);
        }
        if(removeNoIdPrefix && fixEmptyNamespaces()){
            changedCount ++;
        }
        return changedCount;
    }
    public boolean fixEmptyNamespaces(){
        boolean changed = false;
        for(ResXmlStartNamespace ns : getStartNamespaceList()){
            if(ns.fixEmpty()){
                changed = true;
            }
        }
        return changed;
    }
    public int autoSetAttributeNames() {
        return autoSetAttributeNames(true);
    }
    public int autoSetAttributeNames(boolean removeNoIdPrefix) {
        int changedCount = 0;
        Iterator<ResXmlAttribute> attributes = getAttributes();
        while (attributes.hasNext()){
            ResXmlAttribute attribute = attributes.next();
            boolean changed = attribute.autoSetName(removeNoIdPrefix);
            if(changed){
                changedCount ++;
            }
        }
        Iterator<ResXmlElement> iterator = getElements();
        while (iterator.hasNext()) {
            changedCount += iterator.next().autoSetAttributeNames(removeNoIdPrefix);
        }
        return changedCount;
    }
    @Override
    int autoSetLineNumber(int start){
        start ++;
        setLineNumber(start);
        int attrCount = getAttributeCount();
        if(attrCount != 0){
            start +=(attrCount - 1);
        }
        boolean haveElement = false;
        for(ResXmlNode xmlNode : this){
            start = xmlNode.autoSetLineNumber(start);
            if(!haveElement && xmlNode instanceof ResXmlElement){
                haveElement = true;
            }
        }
        if(haveElement){
            start ++;
        }
        return start;
    }
    public void clearNullNodes(){
        clearNullNodes(true);
    }
    private void clearNullNodes(boolean recursive){
        for(ResXmlNode node:listXmlNodes()){
            if(node.isNull()){
                remove(node);
            }
            if(!recursive || !(node instanceof ResXmlElement)){
                continue;
            }
            ((ResXmlElement)node).clearNullNodes(true);
        }
    }
    int removeUnusedNamespaces(){
        int count = 0;
        List<ResXmlStartNamespace> nsList = new ArrayCollection<>(getStartNamespaceList());
        for(ResXmlStartNamespace ns : nsList){
            boolean removed = ns.removeIfNoReference();
            if(removed){
                count ++;
            }
        }
        Iterator<ResXmlElement> iterator = getElements();
        while (iterator.hasNext()) {
            count += iterator.next().removeUnusedNamespaces();
        }
        return count;
    }
    public int removeUndefinedAttributes(){
        int count = 0;
        ResXmlStartElement start = getStartElement();
        if(start != null){
            count += start.removeUndefinedAttributes();
        }
        Iterator<ResXmlElement> iterator = getElements();
        while (iterator.hasNext()) {
            count += iterator.next().removeUndefinedAttributes();
        }
        return count;
    }
    public void changeIndex(ResXmlElement element, int index){
        getNodeListBlockInternal().moveTo(element, index);
    }
    public int indexOf(String tagName){
        ResXmlElement element = getElement(tagName);
        if(element != null){
            return element.getIndex();
        }
        return -1;
    }
    public int lastIndexOf(String tagName){
        return lastIndexOf(tagName, -1);
    }
    public int lastIndexOf(String tagName, int def){
        ResXmlElement last = CollectionUtil.getLast(getElements(tagName));
        if(last != null){
            def = indexOf(last, def);
        }
        return def;
    }
    public int indexOf(ResXmlElement element){
        return indexOf(element, -1);
    }
    public int indexOf(ResXmlNode resXmlNode, int def){
        int index = 0;
        for (ResXmlNode xmlNode : this) {
            if (xmlNode == resXmlNode) {
                return index;
            }
            index ++;
        }
        return def;
    }
    public void setAttributesUnitSize(int size, boolean setToAll){
        ResXmlStartElement startElement = getStartElement();
        startElement.setAttributesUnitSize(size);
        if(setToAll){
            for(ResXmlElement child:listElements()){
                child.setAttributesUnitSize(size, setToAll);
            }
        }
    }
    public String getStartComment(){
        ResXmlStartElement start = getStartElement();
        if(start!=null){
            return start.getComment();
        }
        return null;
    }
    String getEndComment(){
        ResXmlEndElement end = getEndElement();
        if(end!=null){
            return end.getComment();
        }
        return null;
    }
    public int getLineNumber(){
        ResXmlStartElement start = getStartElement();
        if(start != null){
            return start.getLineNumber();
        }
        return 0;
    }
    public void setLineNumber(int lineNumber){
        ResXmlStartElement start = getStartElement();
        if(start != null){
            start.setLineNumber(lineNumber);
            start.getResXmlEndElement().setLineNumber(lineNumber);
        }
        int count = getNamespaceCount();
        for(int i = 0; i < count; i++){
           getNamespaceAt(i).setLineNumber(lineNumber);
        }
    }
    public int getStartLineNumber(){
        ResXmlStartElement start = getStartElement();
        if(start!=null){
            return start.getLineNumber();
        }
        return 0;
    }
    public int getEndLineNumber(){
        ResXmlEndElement end = getEndElement();
        if(end!=null){
            return end.getLineNumber();
        }
        return 0;
    }
    public void setStartLineNumber(int lineNumber){
        ResXmlStartElement start = getStartElement();
        if(start!=null){
            start.setLineNumber(lineNumber);
        }
    }
    public void setEndLineNumber(int lineNumber){
        ResXmlEndElement end = getEndElement();
        if(end != null){
            end.setLineNumber(lineNumber);
        }
    }
    public void setComment(String comment){
        getStartElement().setComment(comment);
    }
    public void calculateAttributesOrder(){
        ResXmlStartElement start = getStartElement();
        if(start != null){
            start.calculatePositions();
        }
    }
    public ResXmlAttribute newAttribute(){
        return getStartElement().newAttribute();
    }
    @Override
    void onRemoved(){
        for(ResXmlStartNamespace startNamespace:getStartNamespaceList()){
            startNamespace.onRemoved();
        }
        ResXmlStartElement start = getStartElement();
        if(start != null){
            start.onRemoved();
        }
        for(ResXmlNode xmlNode : listXmlNodes()){
            xmlNode.onRemoved();
        }
    }
    @Override
    void linkStringReferences(){
        for(ResXmlStartNamespace startNamespace:getStartNamespaceList()){
            startNamespace.linkStringReferences();
        }
        ResXmlStartElement start = getStartElement();
        if(start != null){
            start.linkStringReferences();
        }
        Iterator<ResXmlElement> iterator = getElements();
        while (iterator.hasNext()) {
            iterator.next().linkStringReferences();
        }
    }
    public ResXmlElement createChildElement(){
        return createChildElement(-1, null);
    }
    public ResXmlElement createChildElement(int position){
        return createChildElement(position, null);
    }
    public ResXmlElement createChildElement(String name){
        return createChildElement(-1, name);
    }
    public ResXmlElement createChildElement(int position, String name){
        int lineNo = getStartElement().getLineNumber() + 1;
        ResXmlElement resXmlElement = new ResXmlElement();
        resXmlElement.newStartElement(lineNo);
        if(position >= 0){
            addNode(position, resXmlElement);
        }else {
            addNode(resXmlElement);
        }
        if(name != null){
            resXmlElement.setName(name);
        }
        return resXmlElement;
    }
    public ResXmlTextNode createResXmlTextNode(){
        return createResXmlTextNode(-1, null);
    }
    public ResXmlTextNode createResXmlTextNode(int position){
        return createResXmlTextNode(position, null);
    }
    public ResXmlTextNode createResXmlTextNode(String text){
        return createResXmlTextNode(-1, text);
    }
    public ResXmlTextNode createResXmlTextNode(int position, String text){
        ResXmlTextNode xmlTextNode = new ResXmlTextNode();
        if(position >= 0){
            addNode(position, xmlTextNode);
        }else {
            addNode(xmlTextNode);
        }
        if(text != null){
            xmlTextNode.setText(text);
        }
        return xmlTextNode;
    }
    public void addResXmlText(String text){
        if(text == null){
            return;
        }
        createResXmlTextNode(text);
    }
    public ResXmlAttribute getOrCreateAndroidAttribute(String name, int resourceId){
        return getOrCreateAttribute(
                ResourceLibrary.URI_ANDROID,
                ResourceLibrary.PREFIX_ANDROID,
                name,
                resourceId);
    }
    public ResXmlAttribute getOrCreateAttribute(String uri, String prefix, String name, int resourceId){
        ResXmlAttribute attribute = searchAttribute(name, resourceId);
        if(attribute == null) {
            attribute = createAttribute(name, resourceId);
            attribute.setNamespace(uri, prefix);
        }
        return attribute;
    }
    public ResXmlAttribute getOrCreateAttribute(String name, int resourceId){
        ResXmlAttribute attribute=searchAttribute(name, resourceId);
        if(attribute==null){
            attribute=createAttribute(name, resourceId);
        }
        return attribute;
    }
    public ResXmlAttribute createAndroidAttribute(String name, int resourceId){
        ResXmlAttribute attribute=createAttribute(name, resourceId);
        ResXmlStartNamespace ns = getOrCreateXmlStartNamespace(ResourceLibrary.URI_ANDROID,
                ResourceLibrary.PREFIX_ANDROID);
        attribute.setNamespaceReference(ns.getUriReference());
        return attribute;
    }
    public ResXmlAttribute createAttribute(String name, int resourceId){
        ResXmlAttribute attribute=new ResXmlAttribute();
        addAttribute(attribute);
        attribute.setName(name, resourceId);
        return attribute;
    }
    public void addAttribute(ResXmlAttribute attribute){
        getStartElement().getResXmlAttributeArray().add(attribute);
    }
    private ResXmlAttribute searchAttribute(String name, int resourceId){
        if(resourceId==0){
            return searchAttributeByName(name);
        }
        return searchAttributeByResourceId(resourceId);
    }
    // Searches attribute with resource id = 0
    public ResXmlAttribute searchAttributeByName(String name){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.searchAttributeByName(name);
        }
        return null;
    }
    public ResXmlAttribute searchAttributeByResourceId(int resourceId){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.searchAttributeByResourceId(resourceId);
        }
        return null;
    }
    public String getName(){
        return getName(false);
    }
    public String getName(boolean includePrefix){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getTagName(includePrefix);
        }
        return null;
    }
    public void setName(String uri, String prefix, String name){
        setName(name);
        setNamespace(uri, prefix);
    }
    public void setName(String name){
        ResXmlStringPool stringPool = getStringPool();
        if(stringPool == null){
            return;
        }
        ensureStartEndElement();
        ResXmlStartElement startElement = getStartElement();
        if(name == null){
            startElement.setName(null);
            return;
        }
        String prefix = null;
        int i = name.lastIndexOf(':');
        if(i >= 0){
            prefix = name.substring(0, i);
            i++;
            name = name.substring(i);
        }
        startElement.setName(name);
        if(prefix == null){
            return;
        }
        ResXmlNamespace namespace = getOrCreateNamespaceByPrefix(prefix);
        if(namespace != null){
            startElement.setNamespaceReference(namespace.getUriReference());
        }
    }
    public boolean equalsName(String name){
        if(name == null){
            return getName() == null;
        }
        name = XMLUtil.splitName(name);
        return name.equals(getName(false));
    }
    public String getUri(){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getUri();
        }
        return null;
    }
    public String getPrefix(){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getPrefix();
        }
        return null;
    }
    public void setNamespace(Namespace namespace){
        if(namespace != null){
            setNamespace(namespace.getUri(), namespace.getPrefix());
        }else {
            setNamespace(null, null);
        }
    }
    public void setNamespace(String uri, String prefix){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            startElement.setTagNamespace(uri, prefix);
        }
    }
    public int removeAttributesWithId(int resourceId){
        return removeAttributes(getAttributesWithId(resourceId));
    }
    public int removeAttributesWithName(String name){
        return removeAttributes(getAttributesWithName(name));
    }
    public int removeAttributes(Predicate<? super ResXmlAttribute> predicate){
        return removeAttributes(getAttributes(predicate));
    }
    public int removeAttributes(Iterator<? extends ResXmlAttribute> attributes){
        Iterator<ResXmlAttribute> iterator = CollectionUtil.copyOf(attributes);
        int count = 0;
        while (iterator.hasNext()){
            boolean removed = removeAttribute(iterator.next());
            if(removed){
                count ++;
            }
        }
        return count;
    }
    public Iterator<ResXmlAttribute> getAttributes(){
        ResXmlAttributeArray attributeArray = getAttributeArray();
        if(attributeArray != null){
            if(attributeArray.size() == 0){
                return EmptyIterator.of();
            }
            return attributeArray.iterator();
        }
        return EmptyIterator.of();
    }
    public Iterator<ResXmlAttribute> getAttributes(Predicate<? super ResXmlAttribute> filter){
        ResXmlAttributeArray attributeArray = getAttributeArray();
        if(attributeArray != null){
            if(attributeArray.size() == 0){
                return EmptyIterator.of();
            }
            return attributeArray.iterator(filter);
        }
        return EmptyIterator.of();
    }
    public Iterator<ResXmlAttribute> getAttributesWithId(int resourceId){
        return getAttributes(attribute -> attribute.getNameId() == resourceId);
    }
    public Iterator<ResXmlAttribute> getAttributesWithName(String name){
        return getAttributes(attribute ->
                attribute.getNameId() == 0 && attribute.equalsName(name));
    }
    public int getAttributeCount() {
        ResXmlStartElement startElement=getStartElement();
        if(startElement != null){
            return startElement.getResXmlAttributeArray().size();
        }
        return 0;
    }
    public ResXmlAttribute getAttributeAt(int index){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getResXmlAttributeArray().get(index);
        }
        return null;
    }
    public ResXmlStringPool getStringPool(){
        Block parent = getParent();
        while (parent != null){
            if(parent instanceof ResXmlDocument){
                return ((ResXmlDocument)parent).getStringPool();
            }
            parent = parent.getParent();
        }
        return null;
    }
    public ResXmlIDMap getResXmlIDMap(){
        ResXmlDocument resXmlDocument = getParentDocument();
        if(resXmlDocument!=null){
            return resXmlDocument.getResXmlIDMap();
        }
        return null;
    }

    @Override
    public int getDepth(){
        ResXmlElement parent = getParentElement();
        if(parent != null){
            return parent.getDepth() + 1;
        }
        return 0;
    }
    @Override
    void addEvents(ParserEventList parserEventList){
        String comment = getStartComment();
        if(comment!=null){
            parserEventList.add(
                    new ParserEvent(ParserEvent.COMMENT, this, comment, false));
        }
        parserEventList.add(new ParserEvent(ParserEvent.START_TAG, this));
        for(ResXmlNode xmlNode: this){
            xmlNode.addEvents(parserEventList);
        }
        comment = getEndComment();
        if(comment != null){
            parserEventList.add(
                    new ParserEvent(ParserEvent.COMMENT, this, comment, true));
        }
        parserEventList.add(new ParserEvent(ParserEvent.END_TAG, this));
    }
    public void addElement(ResXmlElement element){
        addNode(element);
    }
    public boolean removeAttribute(ResXmlAttribute resXmlAttribute){
        if(resXmlAttribute != null){
            resXmlAttribute.onRemoved();
        }
        return getStartElement().getResXmlAttributeArray().remove(resXmlAttribute);
    }
    public boolean removeSelf(){
        ResXmlElement parent = getParentElement();
        if(parent != null){
            return parent.remove(this);
        }
        return false;
    }
    public int countElements(){
        return CollectionUtil.count(getElements());
    }
    public boolean hasText(){
        return iterator(ResXmlTextNode.class).hasNext();
    }
    public boolean hasElement(){
        return iterator(ResXmlElement.class).hasNext();
    }
    public List<ResXmlNode> listXmlNodes(){
        return CollectionUtil.toList(iterator());
    }
    public Iterator<ResXmlTextNode> getTextNodes(){
        return iterator(ResXmlTextNode.class);
    }
    public List<ResXmlTextNode> listXmlTextNodes(){
        return CollectionUtil.toList(getTextNodes());
    }
    public void removeElements(Predicate<? super ResXmlElement> predicate){
        removeIf(node -> {
            if(node instanceof ResXmlElement) {
                return predicate.test((ResXmlElement) node);
            }
            return false;
        });
    }
    ResXmlStartNamespace getStartNamespaceByUriRef(int uriRef){
        if(uriRef<0){
            return null;
        }
        Iterator<ResXmlStartNamespace> iterator = mStartNamespaceList.iterator();
        while (iterator.hasNext()){
            ResXmlStartNamespace ns = iterator.next();
            if(uriRef == ns.getUriReference()){
                return ns;
            }
        }
        ResXmlElement xmlElement= getParentElement();
        if(xmlElement!=null){
            return xmlElement.getStartNamespaceByUriRef(uriRef);
        }
        return null;
    }
    ResXmlStartNamespace getXmlStartNamespace(String uri, String prefix){
        if(uri == null || prefix == null){
            return null;
        }
        for(ResXmlStartNamespace ns : mStartNamespaceList.getChildes()){
            if(uri.equals(ns.getUri()) && prefix.equals(ns.getPrefix())){
                return ns;
            }
        }
        ResXmlElement xmlElement = getParentElement();
        if(xmlElement != null){
            return xmlElement.getXmlStartNamespace(uri, prefix);
        }
        return null;
    }
    private ResXmlStartNamespace getOrCreateXmlStartNamespace(String uri, String prefix) {
        return getOrCreateXmlStartNamespace(uri, prefix, 0);
    }
    private ResXmlStartNamespace getOrCreateXmlStartNamespace(String uri, String prefix, int lineNumber){
        ResXmlStartNamespace namespace = getXmlStartNamespace(uri, prefix);
        if(namespace == null) {
            namespace = getRootElement().createXmlStartNamespace(uri, prefix);
            namespace.setLineNumber(lineNumber);
        }
        return namespace;
    }
    private ResXmlStartNamespace createXmlStartNamespace(String uri, String prefix){
        ResXmlStartNamespace startNamespace = new ResXmlStartNamespace();
        ResXmlEndNamespace endNamespace = new ResXmlEndNamespace();
        startNamespace.setEnd(endNamespace);

        addStartNamespace(startNamespace);
        addEndNamespace(endNamespace, true);
        ResXmlStringPool stringPool = getStringPool();
        ResXmlString xmlString = stringPool.getOrCreate(uri);
        startNamespace.setUriReference(xmlString.getIndex());
        startNamespace.setPrefix(prefix);

        return startNamespace;
    }
    ResXmlStartNamespace getStartNamespaceByUri(String uri){
        if(uri==null){
            return null;
        }
        for(ResXmlStartNamespace ns:mStartNamespaceList.getChildes()){
            if(uri.equals(ns.getUri())){
                return ns;
            }
        }
        ResXmlElement xmlElement= getParentElement();
        if(xmlElement!=null){
            return xmlElement.getStartNamespaceByUri(uri);
        }
        return null;
    }
    private ResXmlStartNamespace getStartNamespaceByPrefix(String prefix, ResXmlStartNamespace result){
        if(prefix == null){
            return result;
        }
        for(ResXmlStartNamespace ns:getStartNamespaceList()){
            if(!prefix.equals(ns.getPrefix())){
                continue;
            }
            String uri = ns.getUri();
            if(uri != null && uri.length() != 0){
                return ns;
            }
            result = ns;
        }
        ResXmlElement xmlElement = getParentElement();
        if(xmlElement != null){
            return xmlElement.getStartNamespaceByPrefix(prefix, result);
        }
        return result;
    }
    private List<ResXmlStartNamespace> getStartNamespaceList(){
        return mStartNamespaceList.getChildes();
    }
    private void addStartNamespace(ResXmlStartNamespace item){
        mStartNamespaceList.add(item);
    }
    private void addEndNamespace(ResXmlEndNamespace item, boolean at_first){
        if(at_first){
            mEndNamespaceList.add(0, item);
        }else {
            mEndNamespaceList.add(item);
        }
    }
    void removeNamespace(ResXmlStartNamespace startNamespace){
        if(startNamespace == null){
            return;
        }
        startNamespace.onRemoved();
        mStartNamespaceList.remove(startNamespace);
        mEndNamespaceList.remove(startNamespace.getEnd());
    }

    ResXmlStartElement newStartElement(int lineNo){
        ResXmlStartElement startElement=new ResXmlStartElement();
        setStartElement(startElement);

        ResXmlEndElement endElement=new ResXmlEndElement();
        startElement.setResXmlEndElement(endElement);

        setEndElement(endElement);
        endElement.setResXmlStartElement(startElement);

        startElement.setLineNumber(lineNo);
        endElement.setLineNumber(lineNo);

        return startElement;
    }
    private ResXmlAttributeArray getAttributeArray(){
        ResXmlStartElement startElement = getStartElement();
        if(startElement != null){
            return startElement.getResXmlAttributeArray();
        }
        return null;
    }

    private ResXmlStartElement getStartElement(){
        return mStartElementContainer.getItem();
    }
    private void setStartElement(ResXmlStartElement item){
        mStartElementContainer.setItem(item);
    }

    private ResXmlEndElement getEndElement(){
        return mEndElementContainer.getItem();
    }
    private void setEndElement(ResXmlEndElement item){
        mEndElementContainer.setItem(item);
    }

    private ResXmlTextNode getOrCreateResXmlText(){
        ResXmlNode last = get(size() - 1);
        if(last instanceof ResXmlTextNode){
            return (ResXmlTextNode) last;
        }
        return createResXmlTextNode();
    }
    public void addNode(ResXmlNode xmlNode){
        mBody.add(xmlNode);
    }
    public void addNode(int position, ResXmlNode xmlNode){
        mBody.add(position, xmlNode);
    }

    private boolean isBalanced(){
        return isElementBalanced() && isNamespaceBalanced();
    }
    private boolean isNamespaceBalanced(){
        return (mStartNamespaceList.size()==mEndNamespaceList.size());
    }
    private boolean isElementBalanced(){
        return (hasStartElement() && hasEndElement());
    }
    private boolean hasStartElement(){
        return mStartElementContainer.hasItem();
    }
    private boolean hasEndElement(){
        return mEndElementContainer.hasItem();
    }

    private void linkStartEnd(){
        linkStartEndElement();
        linkStartEndNameSpaces();
    }
    private void linkStartEndElement(){
        ResXmlStartElement start=getStartElement();
        ResXmlEndElement end=getEndElement();
        if(start==null || end==null){
            return;
        }
        start.setResXmlEndElement(end);
        end.setResXmlStartElement(start);
    }
    private void ensureStartEndElement(){
        ResXmlStartElement start=getStartElement();
        ResXmlEndElement end=getEndElement();
        if(start!=null && end!=null){
            return;
        }
        if(start==null){
            start=new ResXmlStartElement();
            setStartElement(start);
        }
        if(end==null){
            end=new ResXmlEndElement();
            setEndElement(end);
        }
        linkStartEndElement();
    }
    private void linkStartEndNameSpaces(){
        if(!isNamespaceBalanced()){
            return;
        }
        int max=mStartNamespaceList.size();
        for(int i=0;i<max;i++){
            ResXmlStartNamespace start=mStartNamespaceList.get(i);
            ResXmlEndNamespace end=mEndNamespaceList.get(max-i-1);
            start.setEnd(end);
        }
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int pos = reader.getPosition();
        while (readNext(reader)){
            if(pos==reader.getPosition()){
                break;
            }
            pos=reader.getPosition();
        }
    }
    private boolean readNext(BlockReader reader) throws IOException {
        int pos = reader.getPosition();
        if(isBalanced()){
            return false;
        }
        HeaderBlock headerBlock=reader.readHeaderBlock();
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType==null){
            unknownChunk(headerBlock);
            return false;
        }
        if(chunkType==ChunkType.XML_START_ELEMENT){
            onStartElement(reader);
        }else if(chunkType==ChunkType.XML_END_ELEMENT){
            onEndElement(reader);
        }else if(chunkType==ChunkType.XML_START_NAMESPACE){
            onStartNamespace(reader);
        }else if(chunkType==ChunkType.XML_END_NAMESPACE){
            onEndNamespace(reader);
        }else if(chunkType==ChunkType.XML_CDATA){
            onXmlText(reader);
        }else{
            unexpectedChunk(headerBlock);
        }
        if(!isBalanced()){
            if(!reader.isAvailable()){
                unBalancedFinish();
            }else if(pos!=reader.getPosition()){
                return true;
            }
        }
        linkStartEnd();
        return false;
    }
    private void onStartElement(BlockReader reader) throws IOException{
        if(hasStartElement()){
            ResXmlElement childElement=new ResXmlElement();
            addElement(childElement);
            childElement.readBytes(reader);
        }else{
            ResXmlStartElement startElement=new ResXmlStartElement();
            setStartElement(startElement);
            startElement.readBytes(reader);
        }
    }
    private void onEndElement(BlockReader reader) throws IOException{
        if(hasEndElement()){
            multipleEndElement(reader);
            return;
        }
        ResXmlEndElement endElement=new ResXmlEndElement();
        setEndElement(endElement);
        endElement.readBytes(reader);
    }
    private void onStartNamespace(BlockReader reader) throws IOException{
        ResXmlStartNamespace startNamespace=new ResXmlStartNamespace();
        addStartNamespace(startNamespace);
        startNamespace.readBytes(reader);
    }
    private void onEndNamespace(BlockReader reader) throws IOException{
        ResXmlEndNamespace endNamespace=new ResXmlEndNamespace();
        addEndNamespace(endNamespace, false);
        endNamespace.readBytes(reader);
    }
    private void onXmlText(BlockReader reader) throws IOException{
        ResXmlTextNode textNode = createResXmlTextNode();
        textNode.getResXmlText().readBytes(reader);
    }

    private void unknownChunk(HeaderBlock headerBlock) throws IOException{
        throw new IOException("Unknown chunk: "+headerBlock.toString());
    }
    private void multipleEndElement(BlockReader reader) throws IOException{
        throw new IOException("Multiple end element: "+reader.toString());
    }
    private void unexpectedChunk(HeaderBlock headerBlock) throws IOException{
        throw new IOException("Unexpected chunk: "+headerBlock.toString());
    }
    private void unBalancedFinish() throws IOException{
        if(!isNamespaceBalanced()){
            throw new IOException("Unbalanced namespace: start="
                    +mStartNamespaceList.size()+", end="+mEndNamespaceList.size());
        }

        if(!isElementBalanced()){
            // Should not happen unless corrupted file, auto corrected above
            StringBuilder builder=new StringBuilder();
            builder.append("Unbalanced element: start=");
            ResXmlStartElement startElement=getStartElement();
            if(startElement!=null){
                builder.append(startElement);
            }else {
                builder.append("null");
            }
            builder.append(", end=");
            ResXmlEndElement endElement=getEndElement();
            if(endElement!=null){
                builder.append(endElement);
            }else {
                builder.append("null");
            }
            throw new IOException(builder.toString());
        }
    }

    public void mergeWithName(ResourceMergeOption mergeOption, ResXmlElement element) {

        setName(element.getName(false));
        setNamespace(element.getNamespace());
        Iterator<ResXmlAttribute> attributes = element.getAttributes();

        while (attributes.hasNext()){
            ResXmlAttribute attribute = attributes.next();
            ResXmlAttribute resXmlAttribute = newAttribute();
            resXmlAttribute.mergeWithName(mergeOption, attribute);
        }
        for (ResXmlNode node : element) {
            if (node instanceof ResXmlElement) {
                createChildElement().mergeWithName(mergeOption, (ResXmlElement) node);
            } else if (node instanceof ResXmlTextNode) {
                createResXmlTextNode().mergeWithName(mergeOption, (ResXmlTextNode) node);
            }
        }
        this.getStartElement().setComment(element.getStartComment());
        this.getEndElement().setComment(element.getEndComment());
        this.getStartElement().setLineNumber(element.getStartElement().getLineNumber());
        this.getEndElement().setLineNumber(element.getEndElement().getLineNumber());
        clearNullNodes(false);
        calculateAttributesOrder();
    }
    @Override
    public void serialize(XmlSerializer serializer, boolean decode) throws IOException {
        int count = getNamespaceCount();
        for(int i = 0; i < count; i++){
            ResXmlNamespace namespace = getNamespaceAt(i);
            serializer.setPrefix(namespace.getPrefix(), namespace.getUri());
        }
        String comment = getStartComment();
        if(comment != null){
            serializer.comment(comment);
        }
        boolean indent = getFeatureSafe(serializer, FEATURE_INDENT_OUTPUT);
        setIndent(serializer, indent);
        boolean indentChanged = indent;
        serializer.startTag(getUri(), getName());
        count = getAttributeCount();
        for(int i = 0; i < count; i++){
            ResXmlAttribute attribute = getAttributeAt(i);
            attribute.serialize(serializer, decode);
        }
        for(ResXmlNode xmlNode : this) {
            if(indentChanged && xmlNode instanceof ResXmlTextNode){
                indentChanged = false;
                setIndent(serializer, false);
            }
            xmlNode.serialize(serializer, decode);
        }
        serializer.endTag(getUri(), getName());
        if(indent != indentChanged){
            setIndent(serializer, true);
        }
        serializer.flush();
    }
    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        if(parser.getEventType() != XmlPullParser.START_TAG){
            throw new XmlPullParserException("Invalid state START_TAG != "
                    + parser.getEventType());
        }
        String name = parser.getName();
        String prefix = splitPrefix(name);
        name = splitName(name);
        setName(name);
        String uri = parser.getNamespace();
        if(prefix == null){
            prefix = parser.getPrefix();
        }
        parseNamespaces(parser);
        setLineNumber(parser.getLineNumber());
        parseAttributes(parser);
        parseChildes(parser);
        if(prefix != null){
            if(uri == null || uri.length() == 0){
                ResXmlNamespace ns = getNamespaceByPrefix(prefix);
                if(ns != null){
                    uri = ns.getUri();
                }
            }
            setNamespace(uri, prefix);
        }
        clearNullNodes(false);
        calculateAttributesOrder();
    }
    private void parseChildes(XmlPullParser parser) throws IOException, XmlPullParserException {
        ResXmlElement currentElement = this;
        int event = parser.next();
        while (event != XmlPullParser.END_TAG && event != XmlPullParser.END_DOCUMENT){
            if(event == XmlPullParser.START_TAG){
                ResXmlElement element = createChildElement();
                element.parse(parser);
                currentElement = element;
            }else if(ResXmlTextNode.isTextEvent(event)){
                ResXmlTextNode textNode = getOrCreateResXmlText();
                textNode.parse(parser);
            }else if(event == XmlPullParser.COMMENT){
                currentElement.setComment(parser.getText());
            }
            event = parser.next();
        }
    }
    private void parseNamespaces(XmlPullParser parser) throws XmlPullParserException {
        int count = parser.getNamespaceCount(parser.getDepth());
        for(int i = 0; i < count; i++) {
            getOrCreateXmlStartNamespace(
                    parser.getNamespaceUri(i),
                    parser.getNamespacePrefix(i),
                    parser.getLineNumber());
        }
        count = parser.getAttributeCount();
        for(int i = 0; i < count; i++) {
            String name = parser.getAttributeName(i);
            String prefix = splitPrefix(name);
            name = splitName(name);
            String value = parser.getAttributeValue(i);
            if(looksNamespace(value, prefix)){
                getOrCreateNamespace(value, name);
            }
        }
    }
    private void parseAttributes(XmlPullParser parser) throws IOException {
        int count = parser.getAttributeCount();
        for(int i = 0; i < count; i++){
            String name = parser.getAttributeName(i);
            String prefix = splitPrefix(name);
            name = splitName(name);
            String value = parser.getAttributeValue(i);
            if(looksNamespace(value, prefix)){
                continue;
            }
            if(prefix == null){
                prefix = parser.getAttributePrefix(i);
                if(prefix != null && prefix.length() == 0){
                    prefix = null;
                }
            }
            String uri;
            if(prefix != null){
                uri = parser.getAttributeNamespace(i);
                if(uri.length() == 0){
                    ResXmlNamespace ns = getNamespaceByPrefix(prefix);
                    if(ns != null){
                        uri = ns.getUri();
                    }
                }
            }else {
                uri = null;
            }
            ResXmlAttribute attribute = newAttribute();
            attribute.encode(false, uri, prefix, name, value);
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(NAME_node_type, NAME_element);
        jsonObject.put(NAME_name, getName(false));
        jsonObject.put(NAME_namespace_uri, getUri());
        jsonObject.put(NAME_namespace_prefix, getPrefix());
        int lineStart = getStartLineNumber();
        int lineEnd = getEndLineNumber();
        jsonObject.put(NAME_line, lineStart);
        if(lineStart != lineEnd){
            jsonObject.put(NAME_line_end, lineEnd);
        }
        JSONArray nsList = new JSONArray();
        for(ResXmlStartNamespace namespace : getStartNamespaceList()){
            JSONObject ns=new JSONObject();
            ns.put(NAME_namespace_uri, namespace.getUri());
            ns.put(NAME_namespace_prefix, namespace.getPrefix());
            nsList.put(ns);
        }
        if(!nsList.isEmpty()){
            jsonObject.put(NAME_namespaces, nsList);
        }
        jsonObject.put(NAME_comment, getStartComment());
        ResXmlAttributeArray attributeArray = getAttributeArray();
        if(attributeArray != null){
            JSONArray attrArray = attributeArray.toJson();
            if(!attrArray.isEmpty()){
                jsonObject.put(NAME_attributes, attrArray);
            }
        }
        JSONArray childes = new JSONArray();
        for(ResXmlNode xmlNode : this){
            childes.put(xmlNode.toJson());
        }
        if(!childes.isEmpty()){
            jsonObject.put(NAME_childes, childes);
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        ensureStartEndElement();
        int startLineNumber = json.optInt(NAME_line, 0);
        int endLineNo = json.optInt(NAME_line_end, 0);
        if(endLineNo == 0 && startLineNumber != 0){
            endLineNo = startLineNumber;
        }
        setStartLineNumber(startLineNumber);
        setEndLineNumber(endLineNo);
        for(ResXmlStartNamespace startNamespace : getStartNamespaceList()){
            startNamespace.setLineNumber(startLineNumber);
        }
        JSONArray nsArray = json.optJSONArray(NAME_namespaces);
        if(nsArray != null){
            int length = nsArray.length();
            for(int i=0; i<length; i++){
                JSONObject nsObject = nsArray.getJSONObject(i);
                String uri = nsObject.optString(NAME_namespace_uri, "");
                String prefix = nsObject.optString(NAME_namespace_prefix, "");
                newNamespace(uri, prefix);
            }
        }
        setName(json.getString(NAME_name));
        setNamespace(json.optString(NAME_namespace_uri, null),
                json.optString(NAME_namespace_prefix, null));
        setComment(json.optString(NAME_comment));
        addResXmlText(json.optString(NAME_text, null));
        ResXmlAttributeArray attributeArray = getAttributeArray();
        if(attributeArray != null){
            attributeArray.fromJson(json.optJSONArray(NAME_attributes));
        }
        JSONArray childArray = json.optJSONArray(NAME_childes);
        if(childArray != null){
            int length = childArray.length();
            for(int i = 0; i < length; i++){
                JSONObject childObject = childArray.getJSONObject(i);
                if(isTextNode(childObject)){
                    createResXmlTextNode().fromJson(childObject);
                }else {
                    createChildElement().fromJson(childObject);
                }
            }
        }
        getStartElement().calculatePositions();
    }
    private boolean isTextNode(JSONObject childObject){
        String type=childObject.optString(NAME_node_type, null);
        if(ResXmlTextNode.NAME_text.equals(type)){
            return true;
        }
        if(NAME_element.equals(type)){
            return false;
        }
        // support older ARSCLib versions
        return childObject.has(NAME_text);
    }

    public XMLElement toXml() {
        return toXml(null, false);
    }
    public XMLElement toXml(boolean decode) {
        return toXml(null, decode);
    }
    public XMLElement decodeToXml() {
        return toXml(null, true);
    }
    private XMLElement toXml(XMLElement parent, boolean decode) {
        XMLElement xmlElement;
        if(parent == null){
            xmlElement = new XMLElement();
        }else {
            xmlElement = parent.newElement();
        }
        xmlElement.setName(getName(false));
        xmlElement.setLineNumber(getStartElement().getLineNumber());
        for(ResXmlStartNamespace startNamespace : getStartNamespaceList()){
            xmlElement.addNamespace(startNamespace.decodeToXml());
        }
        xmlElement.setNamespace(getNamespace());
        Iterator<ResXmlAttribute> attributes = getAttributes();
        while (attributes.hasNext()){
            ResXmlAttribute resXmlAttribute = attributes.next();
            XMLAttribute xmlAttribute = resXmlAttribute.toXml(decode);
            xmlElement.addAttribute(xmlAttribute);
            if(decode) {
                xmlAttribute.setNamespace(resXmlAttribute.decodeUri(), resXmlAttribute.decodePrefix());
            }else {
                xmlAttribute.setNamespace(resXmlAttribute.getUri(), resXmlAttribute.decodePrefix());
            }
        }
        String comment = getStartComment();
        if(comment != null){
            xmlElement.add(new XMLComment(comment));
        }
        comment = getEndComment();
        if(comment != null){
            xmlElement.add(new XMLComment(comment));
        }
        for(ResXmlNode xmlNode: this){
            if(xmlNode instanceof ResXmlElement){
                ResXmlElement childResXmlElement = (ResXmlElement)xmlNode;
                childResXmlElement.toXml(xmlElement, decode);
            }else {
                xmlElement.add(xmlNode.toXml(decode));
            }
        }
        return xmlElement;
    }
    @Override
    public int compare(ResXmlNode node1, ResXmlNode node2) {
        return CompareUtil.compare(node1.getIndex(), node2.getIndex());
    }
    @Override
    public String toString(){
        ResXmlStartElement start = getStartElement();
        if(start!=null){
            StringBuilder builder=new StringBuilder();
            builder.append("(");
            builder.append(getStartLineNumber());
            builder.append(":");
            builder.append(getEndLineNumber());
            builder.append(") ");
            builder.append("<");
            builder.append(start.toString());
            if(hasText() && !hasElement()){
                builder.append(">");
                for(ResXmlTextNode textNode : listXmlTextNodes()){
                    builder.append(textNode.getText());
                }
                builder.append("</");
                builder.append(start.getTagName());
                builder.append(">");
            }else {
                builder.append("/>");
            }
            return builder.toString();
        }
        return "NULL";
    }


    private static boolean looksNamespace(String uri, String prefix){
        return uri.length() != 0 && "xmlns".equals(prefix);
    }
    private static boolean getFeatureSafe(XmlSerializer serializer, String name){
        try{
            return serializer.getFeature(name);
        }catch (Throwable ignored){
            return false;
        }
    }
    private static String splitPrefix(String name){
        int i = name.indexOf(':');
        if(i >= 0){
            return name.substring(0, i);
        }
        return null;
    }
    private static String splitName(String name){
        int i = name.indexOf(':');
        if(i >= 0){
            return name.substring(i + 1);
        }
        return name;
    }
    static void setIndent(XmlSerializer serializer, boolean state){
        setFeatureSafe(serializer, FEATURE_INDENT_OUTPUT, state);
    }
    private static void setFeatureSafe(XmlSerializer serializer, String name, boolean state){
        try{
            serializer.setFeature(name, state);
        }catch (Throwable ignored){
        }
    }
    public static final String NAME_element = ObjectsUtil.of("element");
    public static final String NAME_name = ObjectsUtil.of("name");
    public static final String NAME_comment = ObjectsUtil.of("comment");
    public static final String NAME_text = ObjectsUtil.of("text");
    public static final String NAME_namespaces = ObjectsUtil.of("namespaces");
    public static final String NAME_namespace_uri = ObjectsUtil.of("namespace_uri");
    public static final String NAME_namespace_prefix = ObjectsUtil.of("namespace_prefix");
    public static final String NAME_line = ObjectsUtil.of("line");
    public static final String NAME_line_end = ObjectsUtil.of("line_end");
    public static final String NAME_attributes = ObjectsUtil.of("attributes");
    public static final String NAME_childes = ObjectsUtil.of("childes");

    private static final String FEATURE_INDENT_OUTPUT = "http://xmlpull.org/v1/doc/features.html#indent-output";

    private static final int MAX_INDENT_DEPTH = 25;
}
