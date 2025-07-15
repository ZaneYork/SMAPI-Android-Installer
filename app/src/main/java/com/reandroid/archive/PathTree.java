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
package com.reandroid.archive;

import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.MergingIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.util.*;

public class PathTree<T> implements Comparable<PathTree<?>>, Iterable<PathTree<T>>, JSONConvert<JSONObject> {
    private T item;
    private String name;
    private PathTree<T> parent;
    private final LinkedHashMap<String, PathTree<T>> elementsMap;

    public PathTree(T item, String name){
        this.item = item;
        this.name = name;
        this.elementsMap = new LinkedHashMap<>();
    }
    public PathTree(String name){
        this(null, name);
    }

    public PathTree<T> copy(){
        return copy(getName());
    }
    public PathTree<T> copy(String newName){
        PathTree<T> pathTree = new PathTree<>(getItem(), newName);
        pathTree.setItem(getItem());
        for(PathTree<T> element : this){
            pathTree.add(element.copy());
        }
        return pathTree;
    }
    public void setName(String name){
        String old = this.name;
        if(old.equals(name)){
            return;
        }
        this.name = name;
        PathTree<T> parent = this.parent;
        if(parent != null){
            parent.elementsMap.remove(old);
            parent.elementsMap.put(name, this);
        }
    }

    public void sort(){
        sort(true);
    }
    public void sort(boolean recursive){
        sort(CompareUtil.getComparableComparator(), recursive);
    }
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super PathTree<?>> comparator, boolean recursive){
        LinkedHashMap<String, PathTree<T>> elementsMap = this.elementsMap;
        if(elementsMap.size() == 0){
            return;
        }
        PathTree<?>[] elements = elementsMap.values().toArray(new PathTree<?>[0]);
        Arrays.sort(elements, comparator);
        elementsMap.clear();
        for(PathTree<?> pathTree : elements){
            elementsMap.put(pathTree.getName(), (PathTree<T>) pathTree);
        }
        if(!recursive){
            return;
        }
        for(PathTree<?> pathTree : elements){
            pathTree.sort(comparator, true);
        }
    }
    public List<PathTree<T>> toList(){
        return ArrayCollection.of(elementsMap.values());
    }
    @Override
    public Iterator<PathTree<T>> iterator(){
        return elementsMap.values().iterator();
    }
    public Iterator<PathTree<T>> getFiles(){
        Iterator<Iterator<PathTree<T>>> iteratorIterator = ComputeIterator.of(iterator(),
                pathTree -> {
                    if(pathTree.isFile()){
                        return SingleIterator.of(pathTree);
                    }
                    return pathTree.getFiles();
        });
        return new MergingIterator<>(iteratorIterator);
    }
    public Iterator<T> getFileItems(){
        return ComputeIterator.of(getFiles(), PathTree::getItem);
    }

    public int size(){
        return elementsMap.size();
    }
    public boolean contains(String name){
        return elementsMap.containsKey(name);
    }
    public PathTree<T> remove(String name){
        return elementsMap.remove(name);
    }
    public PathTree<T> getOrCreate(String name){
        PathTree<T> pathTree = get(name);
        if(pathTree == null){
            pathTree = new PathTree<>(name);
            add(pathTree);
        }
        return pathTree;
    }
    public PathTree<T> get(String name){
        return elementsMap.get(name);
    }
    public PathTree<T> find(String path){
        if(path.equals(getName())){
            return this;
        }
        int i = path.indexOf('/');
        if(i >= 0){
            i++;
            String name = path.substring(0, i);
            path = path.substring(i);
            PathTree<T> pathTree = get(name);
            if(pathTree != null && path.length() != 0){
                return pathTree.find(path);
            }
            return pathTree;
        }else if(path.length() > 0){
            return get(path);
        }
        return null;
    }
    public PathTree<T> add(String path, T item){
        int i = path.indexOf('/');
        if(i >= 0){
            i++;
            String name = path.substring(0, i);
            path = path.substring(i);
            PathTree<T> pathTree = getOrCreate(name);
            if(path.length() == 0){
                pathTree.setItemNonNull(item);
                return pathTree;
            }
            return pathTree.add(path, item);
        }else if(path.length() > 0){
            PathTree<T> pathTree = getOrCreate(path);
            pathTree.setItem(item);
            return pathTree;
        }
        return this;
    }
    public boolean add(PathTree<T> element){
        if(element == null){
            return false;
        }
        element.setParent(this);
        this.elementsMap.put(element.getName(), element);
        return true;
    }
    public T getItem() {
        return item;
    }
    public void setItem(T item) {
        this.item = item;
    }
    private void setItemNonNull(T item) {
        if(item != null){
            setItem(item);
        }
    }
    public boolean isDirectory(){
        return getName().endsWith("/");
    }
    public boolean isFile(){
        return !isDirectory();
    }
    public boolean isRoot(){
        return isDirectory() && getParent() == null;
    }
    public String getName() {
        return name;
    }
    public PathTree<T> getParent(){
        return parent;
    }
    public void setParent(PathTree<T> parent) {
        if(parent != this){
            this.parent = parent;
        }
    }

    public int getDepth(){
        int result = 0;
        PathTree<T> parent = this;
        while (parent != null && !parent.isRoot()){
            result ++;
            parent = parent.getParent();
        }
        return result;
    }
    private PathTree<?>[] getParentElements(){
        int length = getDepth();
        PathTree<?>[] results = new PathTree<?>[length];
        PathTree<T> parent = this;
        int i = length - 1;
        while (parent != null && !parent.isRoot()){
            results[i] = parent;
            i --;
            parent = parent.getParent();
        }
        return results;
    }
    public String getPath(){
        StringBuilder builder = new StringBuilder();
        PathTree<?>[] elements = getParentElements();
        for(int i = 0; i < elements.length; i++){
            builder.append(elements[i].getName());
        }
        return builder.toString();
    }

    @Override
    public int compareTo(PathTree<?> pathTree) {
        boolean is_directory = this.isDirectory();
        if(is_directory == pathTree.isDirectory()){
            String name1 = this.getName();
            String name2 = pathTree.getName();
            if(is_directory){
                name1 = name1.substring(0, name1.length() - 1);
                name2 = name2.substring(0, name2.length() - 1);
            }
            name1 = StringsUtil.toLowercase(name1);
            name2 = StringsUtil.toLowercase(name2);
            return name1.compareTo(name2);
        }
        return is_directory ? -1 : 1;
    }
    @Override
    public void fromJson(JSONObject jsonObject){
        JSONArray jsonArray = jsonObject.optJSONArray(NAME_elements);
        if(jsonArray == null){
            return;
        }
        int count = jsonArray.length();
        for(int i = 0; i < count; i++){
            JSONObject child = jsonArray.getJSONObject(i);
            String path = child.optString(NAME_path, "");
            String name = getName(path);
            PathTree<T> pathTree = getOrCreate(name);
            pathTree.fromJson(child);
        }
    }
    @Override
    public JSONObject toJson(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NAME_path, getPath());
        JSONArray jsonArray = new JSONArray();
        for (PathTree<T> pathTree : this) {
            jsonArray.put(pathTree.toJson());
        }
        if(!jsonArray.isEmpty()){
            jsonObject.put(NAME_elements, jsonArray);
        }
        return jsonObject;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PathTree)) {
            return false;
        }
        PathTree<?> pathTree = (PathTree<?>) obj;
        return Objects.equals(getName(), pathTree.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return getPath();
    }

    public static<T1> PathTree<T1> newRoot(){
        return new PathTree<>("/");
    }

    public static Iterator<String> sortPaths(Iterator<String> iterator){
        PathTree<String> pathTree = newRoot();
        while (iterator.hasNext()){
            String path = iterator.next();
            pathTree.add(path, path);
        }
        pathTree.sort();
        return pathTree.getFileItems();
    }
    private static String getName(String path){
        int i = path.length();
        if(i < 2){
            return path;
        }
        i --;
        boolean hasPostfix = false;
        if(path.charAt(i) == '/'){
            path = path.substring(0, i);
            hasPostfix = true;
        }
        i = path.lastIndexOf('/');
        if(i >= 0){
            i++;
            path = path.substring(i);
        }
        if(hasPostfix){
            path = path + '/';
        }
        return path;
    }

    public static final String NAME_path = "path";
    public static final String NAME_elements = "elements";

}
