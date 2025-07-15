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
package com.reandroid.dex.key;

import com.reandroid.dex.data.TypeList;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArrayIterator;
import com.reandroid.utils.collection.ComputeIterator;

import java.util.Iterator;

public class TypeListKey implements Key, Iterable<String> {

    private final String[] parameters;

    public TypeListKey(String[] parameters){
        this.parameters = parameters;
    }

    public TypeListKey remove(int index){
        String[] parameters = getParameterNames();
        if(parameters == null){
            return this;
        }
        int length = parameters.length;
        if(index < 0 || index >= length){
            return this;
        }
        String[] results;
        if(length == 1){
            results = null;
        }else {
            results = new String[length - 1];
        }
        int count = 0;
        for(int i = 0; i < length; i++){
            if(i != index){
                results[count] = parameters[i];
                count ++;
            }
        }
        return new TypeListKey(results);
    }
    public TypeListKey add(String name){
        int length = 0;
        String[] parameters = getParameterNames();
        if(parameters != null){
            length = parameters.length;
        }
        String[] results = new String[length + 1];
        int count = 0;
        if(parameters != null){
            for(int i = 0; i < length; i++){
                results[count] = parameters[i];
                count ++;
            }
        }
        results[count] = name;
        return new TypeListKey(results);
    }

    public String[] getParameterNames() {
        return parameters;
    }
    public int indexOf(String name){
        if(name == null){
            return -1;
        }
        String[] parameters = getParameterNames();
        if(parameters != null){
            int length = parameters.length;
            for(int i = 0; i < length; i++){
                if(name.equals(parameters[i])){
                    return i;
                }
            }
        }
        return -1;
    }
    public int indexOf(TypeKey typeKey){
        if(typeKey == null){
            return -1;
        }
        int size = size();
        for(int i = 0; i < size; i++){
            if(typeKey.equals(getType(i))){
                return i;
            }
        }
        return -1;
    }
    public int size() {
        String[] parameters = getParameterNames();
        if(parameters != null){
            return parameters.length;
        }
        return 0;
    }
    public String get(int i){
        return getParameterNames()[i];
    }
    public TypeKey getType(int i){
        return TypeKey.create(get(i));
    }
    public Iterator<TypeKey> getTypes(){
        return ComputeIterator.of(iterator(), TypeKey::create);
    }
    @Override
    public Iterator<String> iterator(){
        return ArrayIterator.of(getParameterNames());
    }
    @Override
    public Iterator<TypeKey> mentionedKeys() {
        return getTypes();
    }
    @Override
    public Key replaceKey(Key search, Key replace) {
        TypeListKey result = this;
        if(search.equals(result)){
            return replace;
        }
        String[] parameters = this.getParameterNames();
        if(parameters != null && search instanceof TypeKey){
            TypeKey searchType = (TypeKey) search;
            String replaceType = ((TypeKey) replace).getTypeName();
            int length = parameters.length;
            for(int i = 0; i < length; i++){
                if(searchType.equals(new TypeKey(parameters[i]))){
                    parameters[i] = replaceType;
                }
            }
        }
        return result;
    }

    @Override
    public int compareTo(Object obj) {
        if(obj == null){
            return -1;
        }
        TypeListKey key = (TypeListKey) obj;
        return CompareUtil.compare(getParameterNames(), key.getParameterNames());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TypeListKey)) {
            return false;
        }
        TypeListKey key = (TypeListKey) obj;
        return CompareUtil.compare(getParameterNames(), key.getParameterNames()) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        String[] parameters = getParameterNames();
        if(parameters != null){
            for(String param : parameters){
                hash = hash * 31 + param.hashCode();
            }
        }
        return hash;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        String[] parameters = getParameterNames();
        if(parameters != null){
            for (String parameter : parameters) {
                builder.append(parameter);
            }
        }
        builder.append(')');
        return builder.toString();
    }


    public static TypeListKey create(TypeList typeList){
        return create(typeList.getNames());
    }
    public static TypeListKey create(String[] parameters){
        if(parameters == null || parameters.length == 0){
            return null;
        }
        return new TypeListKey(parameters);
    }
}
