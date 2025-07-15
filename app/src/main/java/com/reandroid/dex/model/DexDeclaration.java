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
package com.reandroid.dex.model;

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.IdDefinition;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;

import java.util.Iterator;

public abstract class DexDeclaration extends Dex implements AnnotatedDex {

    public boolean uses(Key key) {
        if(getKey().equals(key)){
            return false;
        }
        return getId().uses(key);
    }
    public boolean isAccessibleTo(TypeKey typeKey) {
        if(this.getDefining().equals(typeKey)){
            return true;
        }
        if(isInternal()) {
            return this.getPackageName().equals(typeKey.getPackageName());
        }
        return !isPrivate();
    }
    public boolean isAccessibleTo(DexClass dexClass) {
        DexClass myClass = getDexClass();
        TypeKey defining = dexClass.getDefining();
        if(!myClass.isAccessibleTo(defining)){
            return false;
        }
        if(myClass.getDefining().equals(defining)){
            return true;
        }
        return myClass == this || isAccessibleTo(defining);
    }
    public boolean isInternal() {
        return (getAccessFlagsValue() & 0x7) == 0;
    }
    public boolean isFinal() {
        return AccessFlag.FINAL.isSet(getAccessFlagsValue());
    }
    public boolean isPublic() {
        return AccessFlag.PUBLIC.isSet(getAccessFlagsValue());
    }
    public boolean isProtected() {
        return AccessFlag.PROTECTED.isSet(getAccessFlagsValue());
    }
    public boolean isPrivate() {
        return AccessFlag.PRIVATE.isSet(getAccessFlagsValue());
    }
    public boolean isNative() {
        return AccessFlag.NATIVE.isSet(getAccessFlagsValue());
    }
    public boolean isStatic() {
        return AccessFlag.STATIC.isSet(getAccessFlagsValue());
    }
    public boolean isSynthetic() {
        return AccessFlag.SYNTHETIC.isSet(getAccessFlagsValue());
    }
    public boolean isAbstract() {
        return AccessFlag.ABSTRACT.isSet(getAccessFlagsValue());
    }
    public boolean hasAccessFlag(AccessFlag accessFlag) {
        return accessFlag.isSet(getAccessFlagsValue());
    }
    public boolean hasAccessFlag(AccessFlag flag1, AccessFlag flag2) {
        return hasAccessFlag(flag1) && hasAccessFlag(flag2);
    }
    public boolean hasAccessFlag(AccessFlag flag1, AccessFlag flag2, AccessFlag flag3) {
        return hasAccessFlag(flag1) &&
                hasAccessFlag(flag2) &&
                hasAccessFlag(flag3);
    }

    public abstract IdDefinition<?> getDefinition();
    public abstract Key getKey();
    public abstract IdItem getId();
    public abstract DexClass getDexClass();

    public Iterator<? extends Modifier> getAccessFlags(){
        return getDefinition().getAccessFlags();
    }
    public void addAccessFlag(AccessFlag accessFlag){
        getDefinition().addAccessFlag(accessFlag);
    }
    public void removeAccessFlag(AccessFlag accessFlag){
        getDefinition().removeAccessFlag(accessFlag);
    }

    int getAccessFlagsValue(){
        return getDefinition().getAccessFlagsValue();
    }
    public TypeKey getDefining(){
        return getKey().getDeclaring();
    }
    public DexFile getDexFile(){
        if(getClass() == DexClass.class){
            throw new RuntimeException(
                    "getDexFile() must be override for: " + getClass());
        }
        return getDexClass().getDexFile();
    }
    public DexDirectory getDexDirectory(){
        DexFile dexFile = getDexFile();
        if(dexFile != null){
            return dexFile.getDexDirectory();
        }
        return null;
    }
    @Override
    public DexClassRepository getClassRepository(){
        DexFile dexFile = getDexFile();
        if(dexFile != null){
            return dexFile.getClassRepository();
        }
        return null;
    }
    public String getPackageName() {
        return getDefining().getPackageName();
    }
    public boolean isInSameFile(DexDeclaration dexDeclaration){
        if(dexDeclaration == null){
            return false;
        }
        if(dexDeclaration == this){
            return true;
        }
        DexFile dexFile = getDexFile();
        if(dexFile == null){
            return false;
        }
        return dexFile == dexDeclaration.getDexFile();
    }
    public boolean isInSameFile(DexFile dexFile){
        return dexFile != null &&
                dexFile.getDexLayout() == getDexFile().getDexLayout();
    }
    public boolean isInSameDirectory(DexDeclaration dexDeclaration){
        if(dexDeclaration == null){
            return false;
        }
        if(isInSameFile(dexDeclaration)){
            return true;
        }
        DexDirectory directory = getDexDirectory();
        if(directory == null){
            return false;
        }
        return directory == dexDeclaration.getDexDirectory();
    }
    public boolean isInSameDirectory(DexDirectory directory){
        return getDexDirectory() == directory;
    }


    @Override
    public int hashCode() {
        Key key = getKey();
        if(key != null){
            return key.hashCode();
        }
        return 0;
    }
    @Override
    public String toString() {
        return Modifier.toString(getAccessFlags()) + getKey();
    }
}
