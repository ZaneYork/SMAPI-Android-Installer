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
package com.reandroid.dex.data;

import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.NumberIntegerReference;
import com.reandroid.dex.base.FixedDexContainer;
import com.reandroid.dex.base.OffsetReceiver;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;
import java.io.OutputStream;

public class HiddenApiData extends FixedDexContainer
        implements OffsetSupplier, OffsetReceiver {

    private IntegerReference offsetReference;
    private ClassId classId;

    private HiddenApiFlagValueList staticFields;
    private HiddenApiFlagValueList instanceFields;
    private HiddenApiFlagValueList directMethods;
    private HiddenApiFlagValueList virtualMethods;

    public HiddenApiData() {
        super(4);
    }

    public HiddenApiFlagValue get(Def<?> def){
        HiddenApiFlagValueList flagValueList = getFlagValueList(def);
        if(flagValueList != null){
            return flagValueList.get(def.getIndex());
        }
        return null;
    }
    private HiddenApiFlagValueList getFlagValueList(Def<?> def){
        Key key = def.getKey();
        if(key instanceof FieldKey){
            if(def.isStatic()){
                return getStaticFields();
            }
            return getInstanceFields();
        }
        if(key instanceof MethodKey){
            if(def.isDirect()){
                return getDirectMethods();
            }
            return getVirtualMethods();
        }
        return null;
    }


    HiddenApiData newCopy(){
        return new Copy(this);
    }
    public boolean isEmptyValueList(){
        if(staticFields != null && !staticFields.isEmptyValueList()){
            return false;
        }
        if(instanceFields != null && !instanceFields.isEmptyValueList()){
            return false;
        }
        if(directMethods != null && !directMethods.isEmptyValueList()){
            return false;
        }
        if(virtualMethods != null && !virtualMethods.isEmptyValueList()){
            return false;
        }
        return true;
    }
    public ClassId getClassId() {
        return classId;
    }
    public void setClassId(ClassId classId) {

        if(this.classId == classId){
            return;
        }
        this.classId = classId;

        initializeValueList();

        addChild(0, staticFields);
        addChild(1, instanceFields);
        addChild(2, directMethods);
        addChild(3, virtualMethods);

        linkDefArray(classId);

    }
    private void initializeValueList() {
        HiddenApiFlagValueList[] valueLists = createHiddenApiFlagValueList();
        this.staticFields = valueLists[0];
        this.instanceFields = valueLists[1];
        this.directMethods = valueLists[2];
        this.virtualMethods = valueLists[3];
    }
    HiddenApiFlagValueList[] createHiddenApiFlagValueList(){
        return new HiddenApiFlagValueList[]{
                new HiddenApiFlagValueList(),
                new HiddenApiFlagValueList(),
                new HiddenApiFlagValueList(),
                new HiddenApiFlagValueList()
        };
    }
    void linkDefArray(ClassId classId) {
        ClassData classData = classId.getClassData();

        this.staticFields.linkDefArray(classData.getStaticFieldsArray());
        this.instanceFields.linkDefArray(classData.getInstanceFieldsArray());
        this.directMethods.linkDefArray(classData.getDirectMethodsArray());
        this.virtualMethods.linkDefArray(classData.getVirtualMethodArray());
    }

    public HiddenApiFlagValueList getStaticFields() {
        return staticFields;
    }
    public HiddenApiFlagValueList getInstanceFields() {
        return instanceFields;
    }
    public HiddenApiFlagValueList getDirectMethods() {
        return directMethods;
    }
    public HiddenApiFlagValueList getVirtualMethods() {
        return virtualMethods;
    }

    public int getOffset(){
        IntegerReference reference = getOffsetReference();
        if(reference != null){
            return reference.get();
        }
        return 0;
    }
    public void setOffset(int offset){
        IntegerReference reference = getOffsetReference();
        if(reference == null){
            reference = new NumberIntegerReference();
            setOffsetReference(reference);
        }
        reference.set(offset);
    }
    @Override
    public IntegerReference getOffsetReference() {
        return offsetReference;
    }
    @Override
    public void setOffsetReference(IntegerReference reference) {
        this.offsetReference = reference;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int offset = getOffset();
        if(offset == 0){
            throw new IOException("Can not read at zero");
        }
        reader.seek(offset);
        super.onReadBytes(reader);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        HiddenApiData apiData = (HiddenApiData) obj;
        return ObjectsUtil.equals(staticFields, apiData.staticFields) &&
                ObjectsUtil.equals(instanceFields, apiData.instanceFields) &&
                ObjectsUtil.equals(directMethods, apiData.directMethods) &&
                ObjectsUtil.equals(virtualMethods, apiData.virtualMethods);
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(staticFields, instanceFields, directMethods, virtualMethods);
    }


    static class Copy extends HiddenApiData{

        private final HiddenApiData source;

        Copy(HiddenApiData source){
            super();
            this.source = source;
        }

        @Override
        HiddenApiData newCopy() {
            return source.newCopy();
        }

        @Override
        HiddenApiFlagValueList[] createHiddenApiFlagValueList() {
            return new HiddenApiFlagValueList[]{
                    source.getStaticFields().newCopy(),
                    source.getInstanceFields().newCopy(),
                    source.getDirectMethods().newCopy(),
                    source.getVirtualMethods().newCopy(),
            };
        }
        @Override
        public IntegerReference getOffsetReference() {
            return source.getOffsetReference();
        }

        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
        }
        @Override
        public int onWriteBytes(OutputStream stream) throws IOException {
            return 0;
        }
        @Override
        public int countBytes() {
            return 0;
        }
        @Override
        public byte[] getBytes() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == this){
                return true;
            }
            if(!(obj instanceof Copy)){
                return false;
            }
            return source.equals(obj);
        }
        @Override
        public int hashCode() {
            return 31 * source.hashCode();
        }
    }
}
