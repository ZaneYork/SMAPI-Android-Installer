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
package com.reandroid.xml;

import com.reandroid.utils.collection.*;
import com.reandroid.xml.base.NodeTree;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Predicate;

public abstract class XMLNodeTree extends XMLNode implements
        NodeTree<XMLNode>, Iterable<XMLNode>, SizedSupplier<XMLNode> {
    private ArrayCollection<XMLNode> mChildes;
    private int lastTrimSize;

    public XMLNodeTree(){
        super();
        this.mChildes = EMPTY;
    }

    public XMLNode getLast(){
        int size = size();
        if(size == 0){
            return null;
        }
        return mChildes.get(size - 1);
    }
    public void clear(){
        if(size() == 0){
            return;
        }
        synchronized (this){
            mChildes.clear();
            mChildes.trimToSize();
            lastTrimSize = 0;
        }
    }
    public Iterator<XMLNode> iterator(Predicate<? super XMLNode> filter){
        return new IndexIterator<>(this, filter);
    }
    @Override
    public Iterator<XMLNode> iterator(){
        return new IndexIterator<>(this);
    }
    public Iterator<XMLNode> recursiveNodes(){
        return RecursiveIterator.of(this, XMLNode::iterator);
    }
    @Override
    public int size(){
        return mChildes.size();
    }
    @Override
    public XMLNode get(int index){
        return mChildes.get(index);
    }
    public void addAll(Iterable<? extends XMLNode> iterable){
        Iterator<? extends XMLNode> itr = iterable.iterator();
        while (itr.hasNext()){
            add(itr.next());
        }
    }
    public boolean add(XMLNode xmlNode) {
        if(xmlNode == null || xmlNode == this){
            return false;
        }
        synchronized (this){
            if(mChildes == EMPTY){
                mChildes = new ArrayCollection<>();
            }
            boolean added = mChildes.add(xmlNode);
            xmlNode.setParentNode(this);
            if(mChildes.size() - lastTrimSize > TRIM_INTERVAL){
                mChildes.trimToSize();
                lastTrimSize = mChildes.size();
            }
            return added;
        }
    }
    public void add(int i, XMLNode xmlNode) {
        if(xmlNode == null || xmlNode == this){
            return;
        }
        synchronized (this){
            if(mChildes == EMPTY){
                mChildes = new ArrayCollection<>();
            }
            mChildes.add(i, xmlNode);
            xmlNode.setParentNode(this);
            if(mChildes.size() - lastTrimSize > TRIM_INTERVAL){
                mChildes.trimToSize();
                lastTrimSize = mChildes.size();
            }
        }
    }
    public int indexOf(XMLNode node){
        return mChildes.indexOf(node);
    }
    public boolean remove(XMLNode xmlNode){
        synchronized (this){
            if(xmlNode != null && mChildes.remove(xmlNode)){
                xmlNode.setParentNode(null);
                return true;
            }
            return false;
        }
    }
    public XMLNode remove(int i){
        synchronized (this){
            XMLNode xmlNode = mChildes.remove(i);
            if(xmlNode != null){
                xmlNode.setParentNode(null);
            }
            return xmlNode;
        }
    }
    /**
     * Use removeIf
     * */
    @Deprecated
    public boolean remove(Predicate<? super XMLNode> filter){
        return removeIf(filter);
    }
    public boolean removeIf(Predicate<? super XMLNode> filter){
        synchronized (this){
            return mChildes.removeIf(filter);
        }
    }
    public boolean sort(Comparator<? super XMLNode> comparator){
        synchronized (this){
            return mChildes.sortItems(comparator);
        }
    }
    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        startSerialize(serializer);
        serializeChildes(serializer);
        endSerialize(serializer);
    }
    abstract void startSerialize(XmlSerializer serializer) throws IOException;
    private void serializeChildes(XmlSerializer serializer) throws IOException {
        Iterator<XMLNode> itr = iterator();
        while (itr.hasNext()){
            itr.next().serialize(serializer);
        }
    }
    abstract void endSerialize(XmlSerializer serializer) throws IOException;

    public XMLElement newElement(){
        return new XMLElement();
    }
    public XMLText newText(){
        return new XMLText();
    }
    public XMLComment newComment(){
        return new XMLComment();
    }
    public XMLAttribute newAttribute(){
        return new XMLAttribute();
    }
    public XMLNamespace newNamespace(String uri, String prefix) {
        return new XMLNamespace(uri, prefix);
    }

    private static final int TRIM_INTERVAL = 1000;
    private static final ArrayCollection<XMLNode> EMPTY = ArrayCollection.empty();
}
