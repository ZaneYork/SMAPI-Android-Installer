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

import android.text.TextUtils;

import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.id.MethodId;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArrayIterator;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Function;

public class MethodKey implements Key{

    private final String declaring;
    private final String name;
    private final String[] parameters;
    private final String returnType;

    private int mParamsHash;

    public MethodKey(String declaring, String name, String[] parameters, String returnType){
        this.declaring = declaring;
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
    }
    public MethodKey(TypeKey typeKey, String name, String[] parameters, String returnType){
        this(typeKey.getTypeName(), name, parameters, returnType);
    }
    public MethodKey(TypeKey typeKey, String name, String[] parameters, TypeKey returnType){
        this(typeKey.getTypeName(), name, parameters, returnType.getTypeName());
    }

    public int getRegister(int index) {
        return getProtoKey().getRegister(index);
    }
    public MethodKey changeDeclaring(TypeKey typeKey){
        return changeDeclaring(typeKey.getTypeName());
    }
    public MethodKey changeDeclaring(String declaring){
        if(declaring.equals(getDeclaringName())){
            return this;
        }
        return new MethodKey(declaring, getName(), getParameterNames(), getReturnTypeName());
    }
    public MethodKey changeName(String name){
        if(name.equals(getName())){
            return this;
        }
        return new MethodKey(getDeclaringName(), name, getParameterNames(), getReturnTypeName());
    }
    public MethodKey changeParameters(String[] parameters){
        if(parameters == getParameterNames()){
            return this;
        }
        return new MethodKey(getDeclaringName(), getName(), parameters, getReturnTypeName());
    }
    public MethodKey changeParameter(int index, TypeKey parameter){
        return changeParameter(index, parameter.getTypeName());
    }
    public MethodKey changeParameter(int index, String parameter){
        if(parameter.equals(getParameter(index))){
            return this;
        }
        String[] update = this.parameters.clone();
        update[index] = parameter;
        return new MethodKey(getDeclaringName(),
                getName(),
                update,
                getReturnTypeName());
    }
    public MethodKey changeReturnType(TypeKey typeKey){
        return changeReturnType(typeKey.getTypeName());
    }
    public MethodKey changeReturnType(String type){
        if(type.equals(getReturnTypeName())){
            return this;
        }
        return new MethodKey(getDeclaringName(), getName(), getParameterNames(), type);
    }
    public MethodKey removeParameter(int index){
        ProtoKey protoKey = getProtoKey();
        protoKey = protoKey.removeParameter(index);
        return new MethodKey(getDeclaringName(), getName(), protoKey.getParameterNames(), getReturnTypeName());
    }
    @Override
    public TypeKey getDeclaring() {
        return new TypeKey(getDeclaringName());
    }
    public StringKey getNameKey() {
        return new StringKey(getName());
    }
    public ProtoKey getProtoKey() {
        return new ProtoKey(getParameterNames(), getReturnTypeName());
    }
    public TypeKey getReturnType() {
        return new TypeKey(getReturnTypeName());
    }

    public String getDeclaringName() {
        return declaring;
    }
    public String getName() {
        return name;
    }
    public String[] getParameterNames() {
        return parameters;
    }

    public Iterator<TypeKey> getParameters(){
        return ComputeIterator.of(ArrayIterator.of(getParameterNames()), TypeKey::create);
    }
    public int getParametersCount() {
        String[] parameters = getParameterNames();
        if(parameters != null){
            return parameters.length;
        }
        return 0;
    }
    public String getParameter(int i){
        return getParameterNames()[i];
    }
    public TypeKey getParameterType(int i){
        return TypeKey.create(getParameter(i));
    }
    public String getReturnTypeName() {
        return returnType;
    }
    public int getParameterRegistersCount(){
        return getProtoKey().getParameterRegistersCount();
    }
    @Override
    public Iterator<Key> mentionedKeys() {
        return CombiningIterator.singleTwo(
                MethodKey.this,
                CombiningIterator.singleOne(getDeclaring(), SingleIterator.of(getNameKey())),
                getProtoKey().mentionedKeys());
    }

    public MethodKey replaceTypes(Function<TypeKey, TypeKey> function) {
        MethodKey result = this;
        TypeKey typeKey = getDeclaring();
        typeKey = typeKey.changeTypeName(function.apply(typeKey));

        result = result.changeDeclaring(typeKey);

        typeKey = getReturnType();
        typeKey = typeKey.changeTypeName(function.apply(typeKey));

        result = result.changeReturnType(typeKey);

        int count = getParametersCount();
        for(int i = 0; i < count; i++){
            typeKey = getParameterType(i);
            typeKey = typeKey.changeTypeName(function.apply(typeKey));
            result = result.changeParameter(i, typeKey);
        }
        return result;
    }

    @Override
    public Key replaceKey(Key search, Key replace) {
        MethodKey result = this;
        if(search.equals(result)){
            return replace;
        }
        if(search.equals(result.getDeclaring())){
            result = result.changeDeclaring((TypeKey) replace);
        }
        if(search.equals(result.getReturnType())){
            result = result.changeReturnType((TypeKey) replace);
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
    public void append(SmaliWriter writer) throws IOException {
        getDeclaring().append(writer);
        writer.append("->");
        writer.append(getName());
        getProtoKey().append(writer);
    }

    @Override
    public int compareTo(Object obj) {
        return compareTo(obj, true);
    }
    public int compareTo(Object obj, boolean checkDefining) {
        if(obj == null){
            return -1;
        }
        MethodKey key = (MethodKey) obj;
        int i;
        if(checkDefining){
            i = CompareUtil.compare(getDeclaringName(), key.getDeclaringName());
            if(i != 0) {
                return i;
            }
        }
        i = CompareUtil.compare(getName(), key.getName());
        if(i != 0) {
            return i;
        }
        i = CompareUtil.compare(getParameterNames(), key.getParameterNames());
        if(i != 0) {
            return i;
        }
        return CompareUtil.compare(getReturnTypeName(), key.getReturnTypeName());
    }

    public boolean equalsIgnoreDeclaring(MethodKey other){
        if(other == null){
            return false;
        }
        if(other == this){
            return true;
        }
        if(!KeyUtil.matches(getName(), other.getName())){
            return false;
        }
        if(getNameParamsHashCode() != other.getNameParamsHashCode()){
            return false;
        }
        int i = CompareUtil.compare(getParameterNames(), other.getParameterNames());
        if(i != 0) {
            return false;
        }
        return KeyUtil.matches(getReturnTypeName(), other.getReturnTypeName());
    }
    public boolean equalsIgnoreReturnType(MethodKey other){
        if(other == null){
            return false;
        }
        if(other == this){
            return true;
        }
        if(!KeyUtil.matches(getDeclaringName(), other.getDeclaringName())){
            return false;
        }
        if(!KeyUtil.matches(getName(), other.getName())){
            return false;
        }
        if(getNameParamsHashCode() != other.getNameParamsHashCode()){
            return false;
        }
        int i = CompareUtil.compare(getParameterNames(), other.getParameterNames());
        return i == 0;
    }
    public boolean equalsNameAndParameters(MethodKey other){
        if(other == null){
            return false;
        }
        if(other == this){
            return true;
        }
        if(!KeyUtil.matches(getName(), other.getName())){
            return false;
        }
        if(getNameParamsHashCode() != other.getNameParamsHashCode()){
            return false;
        }
        int i = CompareUtil.compare(getParameterNames(), other.getParameterNames());
        return i == 0;
    }
    public boolean equalsIgnoreName(MethodKey other){
        if(other == null){
            return false;
        }
        if(other == this){
            return true;
        }
        if(!KeyUtil.matches(getDeclaringName(), other.getDeclaringName())){
            return false;
        }
        int i = CompareUtil.compare(getParameterNames(), other.getParameterNames());
        if(i != 0) {
            return false;
        }
        return KeyUtil.matches(getReturnTypeName(), other.getReturnTypeName());
    }
    public boolean equalsName(MethodKey other){
        if(other == null){
            return false;
        }
        if(other == this){
            return true;
        }
        return KeyUtil.matches(getName(), other.getName());
    }
    public boolean equalsName(String name){
        return KeyUtil.matches(getName(), name);
    }
    public boolean equalsProto(MethodKey other){
        if(other == null){
            return false;
        }
        if(other == this){
            return true;
        }
        int i = CompareUtil.compare(getParameterNames(), other.getParameterNames());
        if(i != 0) {
            return false;
        }
        return KeyUtil.matches(getReturnTypeName(), other.getReturnTypeName());
    }
    public boolean equalsDeclaring(String declaring){
        return KeyUtil.matches(getDeclaringName(), declaring);
    }
    public boolean equalsDeclaring(TypeKey declaring){
        if(declaring == null){
            return false;
        }
        return KeyUtil.matches(getDeclaringName(), declaring.getTypeName());
    }
    public boolean equalsDeclaring(MethodKey other){
        if(other == null){
            return false;
        }
        if(other == this){
            return true;
        }
        return KeyUtil.matches(getDeclaringName(), other.getDeclaringName());
    }
    public boolean equalsReturnType(String returnTypeName){
        return KeyUtil.matches(getReturnTypeName(), returnTypeName);
    }
    public boolean equalsReturnType(TypeKey returnType){
        if(returnType == null){
            return false;
        }
        return KeyUtil.matches(getReturnTypeName(), returnType.getTypeName());
    }
    public boolean equalsReturnType(MethodKey other){
        if(other == null){
            return false;
        }
        if(other == this){
            return true;
        }
        return KeyUtil.matches(getReturnTypeName(), other.getReturnTypeName());
    }
    @Override
    public boolean equals(Object obj) {
        return equals(obj, true, true);
    }
    public boolean equals(Object obj, boolean checkDefining, boolean checkType) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MethodKey)) {
            return false;
        }
        MethodKey methodKey = (MethodKey) obj;
        if(getNameParamsHashCode() != methodKey.getNameParamsHashCode()){
            return false;
        }
        if(!KeyUtil.matches(getName(), methodKey.getName())){
            return false;
        }
        int i = CompareUtil.compare(getParameterNames(), methodKey.getParameterNames());
        if(i != 0) {
            return false;
        }
        if(checkDefining){
            if(!KeyUtil.matches(getDeclaringName(), methodKey.getDeclaringName())){
                return false;
            }
        }
        if(checkType){
            return KeyUtil.matches(getReturnTypeName(), methodKey.getReturnTypeName());
        }
        return true;
    }


    @Override
    public int hashCode() {
        int hash = 1;
        String defining = getDeclaringName();
        if(defining != null){
            hash += defining.hashCode();
        }
        hash = hash * 31 + getNameParamsHashCode();
        String returnType = getReturnTypeName();
        hash = hash * 31;
        if(returnType != null){
            hash = hash + returnType.hashCode();
        }
        return hash;
    }
    private int getNameParamsHashCode() {
        int hash = mParamsHash;
        if(hash != 0){
            return hash;
        }
        hash = 31 + getName().hashCode();
        String[] parameters = getParameterNames();
        if(parameters != null){
            for(String param : parameters){
                hash = hash * 31 + param.hashCode();
            }
        }
        mParamsHash = hash;
        return hash;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String defining = getDeclaringName();
        if(defining != null){
            builder.append(defining);
            builder.append("->");
        }
        builder.append(getName());
        builder.append('(');
        String[] parameters = getParameterNames();
        if(parameters != null){
            for (String parameter : parameters) {
                builder.append(parameter);
            }
        }
        builder.append(')');
        String type = getReturnTypeName();
        if(type != null){
            builder.append(type);
        }
        return builder.toString();
    }

    public static MethodKey parse(String text) {
        if(text == null){
            return null;
        }
        text = text.trim();
        if(text.length() < 6 || (text.charAt(0) != 'L' && text.charAt(0) != '[')){
            return null;
        }
        int i = text.indexOf(";->");
        if(i < 0){
            return null;
        }
        String defining = text.substring(0, i + 1);
        text = text.substring(i + 3);
        i = text.indexOf('(');
        if(i < 0){
            return null;
        }
        String name = text.substring(0, i);
        text = text.substring(i + 1);
        i = text.indexOf(')');
        if(i < 0){
            return null;
        }
        String[] parameters = DexUtils.splitParameters(text.substring(0, i));
        text = text.substring(i + 1);
        String returnType = null;
        if(!TextUtils.isEmpty(text)){
            returnType = text;
        }
        return new MethodKey(defining, name, parameters, returnType);
    }

    public static MethodKey create(MethodId methodId){
        TypeKey defining = methodId.getDefining();
        if(defining == null){
            return null;
        }
        String name = methodId.getName();
        if(name == null){
            return null;
        }
        return new MethodKey(defining, name, methodId.getParameterNames(), methodId.getReturnTypeName());
    }

    public static MethodKey read(SmaliReader reader) throws IOException {
        TypeKey declaring = TypeKey.read(reader);
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, '-');
        SmaliParseException.expect(reader, '>');
        reader.skipWhitespacesOrComment();
        String name = reader.readEscapedString('(');
        ProtoKey protoKey = ProtoKey.read(reader);
        return new MethodKey(
                declaring.getTypeName(),
                name,
                protoKey.getParameterNames(),
                protoKey.getReturnTypeName());
    }

    public static final MethodKey STATIC_CONSTRUCTOR = new MethodKey(
            TypeKey.OBJECT.getTypeName(),
            "<clinit>",
            null,
            TypeKey.TYPE_V.getTypeName());

    public static final MethodKey EQUALS = new MethodKey(
            TypeKey.OBJECT.getTypeName(),
            "equals",
            new String[]{TypeKey.OBJECT.getTypeName()},
            TypeKey.TYPE_Z.getTypeName());

    public static final MethodKey HASHCODE = new MethodKey(
            TypeKey.OBJECT.getTypeName(),
            "hashCode",
            null,
            TypeKey.TYPE_I.getTypeName());

    public static final MethodKey TO_STRING = new MethodKey(
            TypeKey.OBJECT.getTypeName(),
            "toString",
            null,
            TypeKey.STRING.getTypeName());
    public static final MethodKey CONSTRUCTOR = new MethodKey(
            TypeKey.OBJECT.getTypeName(),
            "<init>",
            null,
            TypeKey.TYPE_V.getTypeName());
    public static final MethodKey CONSTRUCTOR_STATIC = new MethodKey(
            TypeKey.OBJECT.getTypeName(),
            "<clinit>",
            null,
            TypeKey.TYPE_V.getTypeName());
}
