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
package com.reandroid.arsc.item;


import com.reandroid.arsc.pool.TypeStringPool;
import com.reandroid.utils.HexUtil;

public class TypeString extends StringItem {
    public TypeString(boolean utf8) {
        super(utf8);
    }

    public boolean isTypeAttr(){
        return isTypeAttr(get());
    }
    public boolean isTypeId(){
        return isTypeId(get());
    }
    public int getId(){
        TypeStringPool stringPool = getParent(TypeStringPool.class);
        if(stringPool!=null){
            return stringPool.idOf(this);
        }
        // Should not reach here , this means it not added to string pool
        return getIndex()+1;
    }
    @Override
    public StyleItem getOrCreateStyle(){
        // Type don't have style unless to obfuscate/confuse other decompilers
        return null;
    }
    @Override
    void ensureStringLinkUnlocked(){
    }
    @Override
    public int compareTo(StringItem stringItem){
        if(stringItem == null){
            return -1;
        }
        return Integer.compare(getIndex(), stringItem.getIndex());
    }
    @Override
    public String toString(){
        return HexUtil.toHex2((byte) getId())+':'+get();
    }


    public static boolean isTypeAttr(String type){
        // TODO: find better way
        if(type == null){
            return false;
        }
        return type.contains("attr");
    }
    public static boolean isTypeArray(String type){
        // TODO: find better way
        if(type == null){
            return false;
        }
        return type.contains("array");
    }
    public static boolean isTypeStyle(String type){
        // TODO: find better way
        if(type == null){
            return false;
        }
        return type.contains("style");
    }
    public static boolean isTypePlurals(String type){
        // TODO: find better way
        if(type == null){
            return false;
        }
        return type.contains("plurals");
    }
    public static boolean isTypeId(String type){
        // TODO: find better way
        return "id".equals(type);
    }
    public static boolean isTypeString(String type){
        // TODO: find better way
        if(type == null){
            return false;
        }
        return type.equals("string");
    }
    public static String toXmlTagName(String typeName){
        // e.g ^attr-private
        if(typeName != null && typeName.length()>0 && typeName.charAt(0) == '^'){
            typeName = typeName.substring(1);
        }
        return typeName;
    }
}
