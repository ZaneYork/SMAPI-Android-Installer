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

import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class TypeKey implements Key{

    private final String typeName;
    private String simpleName;

    public TypeKey(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getSourceName(){
        int array = getArrayDimension();
        if(array == 0) {
            String type = getTypeName();
            if(type.length() == 1) {
                TypeKey typeKey = PrimitiveTypeKey.primitiveType(type.charAt(0));
                if(typeKey != null) {
                    return typeKey.getSourceName();
                }
            }
            return DexUtils.toSourceName(type);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(getDeclaring().getSourceName());
        for(int i = 0; i < array; i++){
            builder.append("[]");
        }
        return builder.toString();
    }
    @Override
    public TypeKey getDeclaring(){
        String main = getDeclaringName();
        if(main.equals(getTypeName())){
            return this;
        }
        return TypeKey.create(main);
    }
    @Override
    public Iterator<TypeKey> mentionedKeys() {
        Iterator<TypeKey> iterator = SingleIterator.of(this);
        if(isTypeArray()) {
            iterator = CombiningIterator.singleOne(getDeclaring(), iterator);
        }
        return iterator;
    }

    @Override
    public Key replaceKey(Key search, Key replace) {
        if(search.equals(this)){
            return replace;
        }
        return this;
    }

    public String getDeclaringName() {
        return DexUtils.toDeclaringType(getTypeName());
    }
    public String getSignatureTypeName() {
        return DexUtils.toSignatureType(getTypeName());
    }
    public TypeKey setArrayDimension(int dimension){
        if(dimension == getArrayDimension()){
            return this;
        }
        return new TypeKey(getArrayType(dimension));
    }
    public String getArrayType(int dimension){
        return DexUtils.makeArrayType(getTypeName(), dimension);
    }
    public int getArrayDimension(){
        return DexUtils.countArrayPrefix(getTypeName());
    }
    public boolean isTypeSignature(){
        return DexUtils.isTypeSignature(getTypeName());
    }
    public boolean isTypeArray(){
        String name = getTypeName();
        return name.length() > 1 && name.charAt(0) == '[';
    }
    public boolean isTypeObject(){
        return DexUtils.isTypeObject(getTypeName());
    }
    public boolean isPrimitive(){
        return DexUtils.isPrimitive(getTypeName());
    }
    public boolean isWide(){
        String name = getTypeName();
        if(name.length() != 1){
            return false;
        }
        return name.equals(TYPE_D.getTypeName()) || name.equals(TYPE_J.getTypeName());
    }

    public String getSimpleName() {
        if(simpleName == null){
            simpleName = DexUtils.getSimpleName(getTypeName());
        }
        return simpleName;
    }
    public String getSimpleInnerName(){
        return DexUtils.getSimpleInnerName(getTypeName());
    }
    public boolean isInnerName(){
        return !getSimpleName().equals(getSimpleInnerName());
    }
    public String getPackageName() {
        return DexUtils.getPackageName(getTypeName());
    }
    public String getPackageSourceName(){
        String packageName = getPackageName();
        int i = packageName.length() - 1;
        if(i < 1){
            return StringsUtil.EMPTY;
        }
        return packageName.substring(1, i).replace('/', '.');
    }
    public TypeKey changeTypeName(String typeName){
        return changeTypeName(create(typeName));
    }
    public TypeKey changeTypeName(TypeKey typeKey){
        if(this.equals(typeKey)){
            return this;
        }
        return typeKey.setArrayDimension(getArrayDimension());
    }
    public TypeKey renamePackage(String from, String to){
        String packageName = getPackageName();
        if(packageName.equals(from)){
            return setPackage(packageName, to);
        }
        int i = from.length();
        if(i == 1 || packageName.length() < i || !packageName.startsWith(from)){
            return this;
        }
        return setPackage(packageName, packageName.replace(from, to));
    }
    public TypeKey setPackage(String packageName){
        return setPackage(getPackageName(), packageName);
    }
    private TypeKey setPackage(String myPackage, String packageName){
        if(myPackage.equals(packageName)){
            return this;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(packageName);
        int i = packageName.length() - 1;
        if(i > 0 && packageName.charAt(i) != '/'){
            builder.append('/');
        }
        builder.append(getSimpleName());
        String name = getTypeName();
        char postFix = name.charAt(name.length() - 1);
        if(postFix == ';' || postFix == '<'){
            builder.append(postFix);
        }
        TypeKey typeKey = new TypeKey(builder.toString());
        return typeKey.setArrayDimension(getArrayDimension());
    }
    public boolean isPackage(String packageName){
        return isPackage(packageName, !"L".equals(packageName));
    }
    public boolean isPackage(String packageName, boolean checkSubPackage) {
        if(isPrimitive()){
            return false;
        }
        String name = getPackageName();
        if(checkSubPackage){
            return name.startsWith(packageName);
        }
        return name.equals(packageName);
    }
    public TypeKey getEnclosingClass(){
        String type = getTypeName();
        String parent = DexUtils.getParentClassName(type);
        if(type.equals(parent)){
            return this;
        }
        return new TypeKey(parent);
    }
    public TypeKey createInnerClass(String simpleName){
        String type = getTypeName();
        String child = DexUtils.createChildClass(type, simpleName);
        if(type.equals(child)){
            return this;
        }
        return new TypeKey(child);
    }
    public Iterator<String> iteratePackageNames(){
        if(getTypeName().indexOf('/') < 0){
            return EmptyIterator.of();
        }
        String packageName = getPackageName();

        return new Iterator<String>() {
            String name = packageName;
            @Override
            public boolean hasNext() {
                return name.charAt(name.length() - 1) == '/';
            }
            @Override
            public String next() {
                String result = this.name;
                String name = result;
                while (name.charAt(name.length() - 1) == '/'){
                    name = name.substring(0, name.length() - 1);
                }
                int i = name.lastIndexOf('/');
                if(i > 0){
                    name = name.substring(0, i + 1);
                }
                this.name = name;
                return result;
            }
        };
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getTypeName());
    }

    public int compareInnerFirst(TypeKey other) {
        if(this.equals(other)){
            return 0;
        }
        String name1 = this.getSimpleName();
        String name2 = other.getSimpleName();
        int diff = StringsUtil.diffStart(name1, name2);
        if(diff > 0 && name1.charAt(diff) == '$' && diff > name1.lastIndexOf('/') + 1){
            return CompareUtil.compare(name2, name1);
        }
        name1 = this.getTypeName();
        name2 = other.getTypeName();
        return CompareUtil.compare(name1, name2);
    }
    public boolean equalsPackage(TypeKey typeKey) {
        if(typeKey == null) {
            return false;
        }
        String name1 = StringsUtil.trimStart(getTypeName(), '[');
        String name2 = StringsUtil.trimStart(typeKey.getTypeName(), '[');
        if(name1.charAt(0) != 'L' || name2.charAt(0) != 'L') {
            return false;
        }
        if(typeKey == this || name1.equals(name2)) {
            return true;
        }
        int start = StringsUtil.diffStart(name1, name2);
        if(start < 0) {
            return false;
        }
        return StringsUtil.indexOfFrom(name1, start, '/') < 0 &&
                StringsUtil.indexOfFrom(name2, start, '/') < 0;
    }
    @Override
    public int compareTo(Object obj) {
        if(obj == null){
            return -1;
        }
        if(obj == this){
            return 0;
        }
        TypeKey key = (TypeKey) obj;
        return CompareUtil.compare(getTypeName(), key.getTypeName());
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if(!(obj instanceof TypeKey)) {
            return false;
        }
        TypeKey key = (TypeKey) obj;
        return getTypeName().equals(key.getTypeName());
    }
    @Override
    public int hashCode() {
        return getTypeName().hashCode();
    }
    @Override
    public String toString() {
        return getTypeName();
    }

    public static TypeKey parse(String name){
        if(name == null || name.length() == 0){
            return null;
        }
        if(name.indexOf('>') > 0 ||
                name.indexOf('(') > 0 ||
                name.indexOf('@') > 0){
            return null;
        }
        if(name.indexOf('/') > 0 ||
                name.indexOf(';') > 0 ||
                name.charAt(0) == '['){
            return create(name);
        }
        if(name.indexOf('.') >= 0){
            return create(DexUtils.toBinaryName(name));
        }
        int i = 0;
        while (name.charAt(i) == '[' && i < name.length() - 1){
            i++;
        }
        if(primitiveType(name.charAt(i)) != null){
            return create(name);
        }
        if(name.equals(TYPE_B.getSourceName())){
            return TYPE_B;
        }
        if(name.equals(TYPE_D.getSourceName())){
            return TYPE_D;
        }
        if(name.equals(TYPE_F.getSourceName())){
            return TYPE_F;
        }
        if(name.equals(TYPE_I.getSourceName())){
            return TYPE_I;
        }
        if(name.equals(TYPE_J.getSourceName())){
            return TYPE_J;
        }
        if(name.equals(TYPE_S.getSourceName())){
            return TYPE_S;
        }
        if(name.equals(TYPE_V.getSourceName())){
            return TYPE_V;
        }
        if(name.equals(TYPE_Z.getSourceName())){
            return TYPE_Z;
        }
        return create(DexUtils.toBinaryName(name));
    }

    public static TypeKey create(String typeName){
        if(typeName == null){
            return null;
        }
        int length = typeName.length();
        if(length == 0){
            return null;
        }
        if(length != 1){
            return new TypeKey(typeName);
        }
        return primitiveType(typeName.charAt(0));
    }
    public static TypeKey read(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        int position = reader.position();
        StringBuilder builder = new StringBuilder();
        while (reader.get() == '['){
            builder.append(reader.readASCII());
        }
        byte b = reader.get();
        if(b != 'L'){
            builder.append(reader.readASCII());
        }else {
            int i = reader.indexOfBeforeLineEnd(';');
            if(i < 0){
                reader.position(position);
                throw new SmaliParseException("Invalid type, missing ';'", reader);
            }
            i = i + 1;
            builder.append(reader.readString(i - reader.position()));
        }
        TypeKey typeKey = TypeKey.create(builder.toString());
        if(typeKey == null){
            reader.position(position);
            throw new SmaliParseException("Invalid type", reader);
        }
        return typeKey;
    }

    public static TypeKey primitiveType(char ch){
        switch (ch){
            case 'B':
                return TYPE_B;
            case 'C':
                return TYPE_C;
            case 'D':
                return TYPE_D;
            case 'F':
                return TYPE_F;
            case 'I':
                return TYPE_I;
            case 'J':
                return TYPE_J;
            case 'S':
                return TYPE_S;
            case 'V':
                return TYPE_V;
            case 'Z':
                return TYPE_Z;
            default:
                return null;
        }
    }


    public static TypeKey parseSignature(String type){
        if(DexUtils.isTypeOrSignature(type)){
            return new TypeKey(type);
        }
        return null;
    }
    public static final TypeKey NULL = new TypeKey("00"){
        @Override
        public boolean isPlatform() {
            return false;
        }
        @Override
        public boolean isPrimitive() {
            return false;
        }
        @Override
        public boolean isTypeArray() {
            return false;
        }
        @Override
        public boolean isTypeObject() {
            return false;
        }
        @Override
        public TypeKey getDeclaring() {
            return this;
        }
        @Override
        public String getTypeName() {
            return StringsUtil.EMPTY;
        }
        @Override
        public Iterator<TypeKey> mentionedKeys() {
            return EmptyIterator.of();
        }
        @Override
        public Key replaceKey(Key search, Key replace) {
            return this;
        }

        @Override
        public boolean isPackage(String packageName) {
            return false;
        }
        @Override
        public boolean isPackage(String packageName, boolean checkSubPackage) {
            return false;
        }
        @Override
        public TypeKey setArrayDimension(int dimension) {
            return this;
        }
        @Override
        public TypeKey setPackage(String packageName) {
            return this;
        }

        @Override
        public void append(SmaliWriter writer) {
        }
    };
    static class PrimitiveTypeKey extends TypeKey {
        private final String sourceName;

        public PrimitiveTypeKey(String type, String sourceName) {
            super(type);
            this.sourceName = sourceName;
        }

        @Override
        public boolean uses(Key key) {
            return equals(key);
        }
        @Override
        public String getSourceName() {
            return sourceName;
        }
        @Override
        public TypeKey getDeclaring() {
            return this;
        }
        @Override
        public boolean isPrimitive() {
            return true;
        }
        @Override
        public boolean isWide() {
            return false;
        }
        @Override
        public boolean isTypeObject() {
            return false;
        }
        @Override
        public boolean isTypeArray() {
            return false;
        }
        @Override
        public boolean isInnerName() {
            return false;
        }
        @Override
        public boolean isTypeSignature() {
            return false;
        }
    }

    public static final TypeKey TYPE_B = new PrimitiveTypeKey("B", "byte");
    public static final TypeKey TYPE_C = new PrimitiveTypeKey("C", "char");
    public static final TypeKey TYPE_D = new PrimitiveTypeKey("D", "double"){
        @Override
        public boolean isWide() {
            return true;
        }
    };
    public static final TypeKey TYPE_F = new PrimitiveTypeKey("F", "float");
    public static final TypeKey TYPE_I = new PrimitiveTypeKey("I", "int");
    public static final TypeKey TYPE_J = new PrimitiveTypeKey("J", "long"){
        @Override
        public boolean isWide() {
            return true;
        }
    };
    public static final TypeKey TYPE_S = new PrimitiveTypeKey("S", "short");
    public static final TypeKey TYPE_V = new PrimitiveTypeKey("V", "void");
    public static final TypeKey TYPE_Z = new PrimitiveTypeKey("Z", "boolean");

    public static final TypeKey CLASS = new TypeKey("Ljava/lang/Class;");
    public static final TypeKey OBJECT = new TypeKey("Ljava/lang/Object;");
    public static final TypeKey STRING = new TypeKey("Ljava/lang/String;");

    public static final TypeKey DALVIK_EnclosingClass = new TypeKey("Ldalvik/annotation/EnclosingClass;");
    public static final TypeKey DALVIK_EnclosingMethod = new TypeKey("Ldalvik/annotation/EnclosingMethod;");
    public static final TypeKey DALVIK_InnerClass = new TypeKey("Ldalvik/annotation/InnerClass;");
    public static final TypeKey DALVIK_MemberClass = new TypeKey("Ldalvik/annotation/MemberClasses;");
    public static final TypeKey DALVIK_Signature = new TypeKey("Ldalvik/annotation/Signature;");
}
