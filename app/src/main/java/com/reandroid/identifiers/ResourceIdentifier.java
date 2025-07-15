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
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.utils.HexUtil;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class ResourceIdentifier extends Identifier{
    private Boolean mHasGoodName;
    public ResourceIdentifier(int id, String name){
        super(id, name);
    }
    public ResourceIdentifier(){
        this(0, null);
    }


    public void write(XmlSerializer serializer) throws IOException {
        serializer.text("\n  ");
        serializer.startTag(null, XML_TAG_PUBLIC);
        serializer.attribute(null, XML_ATTRIBUTE_ID, getHexId());
        serializer.attribute(null, TypeIdentifier.XML_ATTRIBUTE_TYPE, getTypeName());
        serializer.attribute(null, XML_ATTRIBUTE_NAME, getName());
        serializer.endTag(null, XML_TAG_PUBLIC);
    }
    public TypeIdentifier getTypeIdentifier() {
        return (TypeIdentifier) getParent();
    }
    public void setTypeIdentifier(TypeIdentifier typeIdentifier) {
        setParent(typeIdentifier);
    }
    public PackageIdentifier getPackageIdentifier(){
        TypeIdentifier typeIdentifier = getTypeIdentifier();
        if(typeIdentifier != null){
            return typeIdentifier.getPackageIdentifier();
        }
        return null;
    }
    public String getTypeName(){
        TypeIdentifier typeIdentifier = getTypeIdentifier();
        if(typeIdentifier != null){
            return typeIdentifier.getName();
        }
        return null;
    }
    public String getPackageName(){
        TypeIdentifier typeIdentifier = getTypeIdentifier();
        if(typeIdentifier != null){
            return typeIdentifier.getPackageName();
        }
        return null;
    }
    public int getTypeId(){
        TypeIdentifier typeIdentifier = getTypeIdentifier();
        if(typeIdentifier != null){
            return typeIdentifier.getId();
        }
        return 0;
    }
    public int getPackageId(){
        TypeIdentifier typeIdentifier = getTypeIdentifier();
        if(typeIdentifier != null){
            return typeIdentifier.getPackageId();
        }
        return 0;
    }
    public int getResourceId(){
        int resourceId = getPackageId() << 24;
        resourceId |= getTypeId() << 16;
        resourceId |= getId();
        return resourceId;
    }
    @Override
    public void setId(int id) {
        super.setId(id & 0xffff);
    }
    @Override
    public String getHexId(){
        return HexUtil.toHex8(getResourceId());
    }
    public String getResourceName(){
        return getResourceName(null);
    }
    public String getResourceName(PackageIdentifier context){
        boolean appendPackage = context != getPackageIdentifier();
        return getResourceName('@', appendPackage, true);
    }
    public String getResourceName(char prefix, boolean appendPackage, boolean appendType){
        String packageName = appendPackage ? getPackageName() : null;
        String typeName = appendType ? getTypeName() : null;
        return buildResourceName(prefix, packageName, typeName, getName());
    }
    @Override
    public void setName(String name) {
        super.setName(name);
        mHasGoodName = null;
    }
    @Override
    long getUniqueId(){
        return 0x00000000ffffffffL & this.getResourceId();
    }
    @Override
    public void setTag(Object tag){
        TypeIdentifier ti = getTypeIdentifier();
        if(ti == null){
            super.setTag(tag);
            return;
        }
        Object exist = getTag();
        if(exist != null){
            ti.removeTag(exist);
        }
        ti.addTag(tag, this);
        super.setTag(tag);
    }
    public String generateUniqueName(){
        String type = getTypeName();
        if(type == null){
            type = "res";
        }
        return type + "_" + getHexId();
    }
    public boolean isGeneratedName(){
        String name = getName();
        if(name == null){
            return false;
        }
        if(!name.contains("_0x")){
            return false;
        }
        return generateUniqueName().equals(name);
    }
    public boolean renameSpecGenerated(){
        setName(generateUniqueName());
        return renameSpec();
    }
    public boolean renameBadSpec(){
        if(hasGoodName()){
            return renameDollarPrefix();
        }
        setName(generateUniqueName());
        return renameSpec();
    }
    public boolean renameDollarPrefix(){
        if(!Identifier.isAapt()) {
            return false;
        }
        String name = getName();
        if(name.charAt(0) != '$'){
            return false;
        }
        name = name.substring(1);

        setName(name);
        return renameSpec();
    }
    public boolean renameSpec(){
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock == null){
            return false;
        }
        ResourceEntry resourceEntry = packageBlock.getResource(getResourceId());
        if(resourceEntry == null){
            return false;
        }
        String name = getName();
        if(name == null){
            return false;
        }
        if(name.equals(resourceEntry.getName())){
            return false;
        }
        resourceEntry.setName(name);
        return true;
    }
    private PackageBlock getPackageBlock(){
        PackageIdentifier pi = getPackageIdentifier();
        if(pi != null){
            return pi.getPackageBlock();
        }
        return null;
    }
    public boolean hasGoodName(){
        if(mHasGoodName == null){
            mHasGoodName = isGoodName(getName());
        }
        return mHasGoodName;
    }
    @Override
    public String toString(){
        return getHexId() + " " + getResourceName();
    }

    public static String buildResourceName(char prefix, String packageName, String type, String entry){
        StringBuilder builder = new StringBuilder();
        if(prefix != 0){
            builder.append(prefix);
        }
        if(packageName != null){
            builder.append(packageName);
            builder.append(':');
        }
        if(type != null){
            builder.append(type);
            builder.append('/');
        }
        builder.append(entry);
        return builder.toString();
    }
    public static boolean isGoodName(String name){
        if(name == null){
            return false;
        }
        int length = name.length();
        if(length < NAME_LENGTH_MIN || length > NAME_LENGTH_MAX){
            return false;
        }
        char[] chars = name.toCharArray();
        if(!isGoodFirstChar(chars[0])){
            return false;
        }
        length = chars.length;
        for(int i = 1; i < length; i++){
            if(!isGoodNameChar(chars[i])){
                return false;
            }
        }
        return true;
    }
    private static boolean isGoodNameChar(char ch){
        return isAtoZ(ch)
                || isDigits(ch)
                || ch == '_'
                || ch == '.'
                || ch == '$'
                || ch == '-';
    }
    private static boolean isGoodFirstChar(char ch){
        return isAtoZ(ch) ||
                ch == '_'
                || ch == '$';
    }
    private static boolean isAtoZ(char ch){
        if(ch >= 'A' && ch <= 'Z'){
            return true;
        }
        return ch >= 'a' && ch <= 'z';
    }
    private static boolean isDigits(char ch){
        return ch >= '0' && ch <= '9';
    }


    public static final int NAME_LENGTH_MIN = 1;
    public static final int NAME_LENGTH_MAX = 100;

}
