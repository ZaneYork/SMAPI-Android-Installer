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

import com.reandroid.arsc.container.BlockList;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.FilterIterator;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public interface ResXmlNodeTree extends Iterable<ResXmlNode>, JSONConvert<JSONObject> {

    BlockList<ResXmlNode> getNodeListBlockInternal();
    default Iterator<ResXmlNode> iterator() {
        return getNodeListBlockInternal().clonedIterator();
    }
    default ResXmlNode get(int i){
        return getNodeListBlockInternal().get(i);
    }
    default int size(){
        return getNodeListBlockInternal().size();
    }
    default void add(ResXmlNode resXmlNode) {
        if(resXmlNode == null || resXmlNode == this) {
            return;
        }
        BlockList<ResXmlNode> blockList = getNodeListBlockInternal();
        if(!blockList.containsExact(resXmlNode)) {
            blockList.add(resXmlNode);
        }
    }
    default void add(int index, ResXmlNode resXmlNode) {
        if(resXmlNode == null || resXmlNode == this) {
            return;
        }
        BlockList<ResXmlNode> blockList = getNodeListBlockInternal();
        if(!blockList.containsExact(resXmlNode)) {
            blockList.add(index, resXmlNode);
        }
    }
    default int removeIf(Predicate<? super ResXmlNode> predicate) {
        int count = 0;
        Iterator<ResXmlNode> iterator = getNodeListBlockInternal().clonedIterator();
        while (iterator.hasNext()) {
            ResXmlNode node = iterator.next();
            if(predicate.test(node) && remove(node)) {
                count ++;
            }
        }
        return count;
    }
    default boolean remove(ResXmlNode xmlNode) {
        if(xmlNode != null && xmlNode.getParent() != null){
            xmlNode.onRemoved();
        }
        return getNodeListBlockInternal().remove(xmlNode);
    }
    default void clear() {
        for(ResXmlNode xmlNode : this) {
            if(xmlNode.getParent() != null){
                xmlNode.onRemoved();
            }
        }
        getNodeListBlockInternal().clearChildes();
    }


    default Iterator<ResXmlNode> iterator(Predicate<? super ResXmlNode> predicate){
        return getNodeListBlockInternal().iterator(predicate);
    }
    default <T1 extends ResXmlNode> Iterator<T1> iterator(Class<T1> instance){
        return getNodeListBlockInternal().iterator(instance);
    }
    default Iterator<ResXmlElement> getElements(){
        return iterator(ResXmlElement.class);
    }
    default Iterator<ResXmlElement> getElements(Predicate<? super ResXmlElement> filter){
        return FilterIterator.of(getElements(), filter);
    }
    default Iterator<ResXmlElement> getElements(String name){
        return getElements(element -> element.equalsName(name));
    }
    default ResXmlElement getElement(String name){
        return CollectionUtil.getFirst(getElements(name));
    }
    default List<ResXmlElement> listElements(){
        return CollectionUtil.toList(getElements());
    }
    default List<ResXmlElement> listElements(String name){
        return CollectionUtil.toList(getElements(name));
    }

}
