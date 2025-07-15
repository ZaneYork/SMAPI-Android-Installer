package com.reandroid.dex.data;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.DataKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.reference.StringUle128Reference;
import com.reandroid.dex.smali.model.SmaliAnnotationElement;
import com.reandroid.dex.smali.model.SmaliValue;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.dex.value.NullValue;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.CombiningIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class AnnotationElement extends DataItem implements Comparable<AnnotationElement>, SmaliFormat {

    private final StringUle128Reference elementName;

    private final DataKey<AnnotationElement> mKey;

    public AnnotationElement() {
        super(2);
        this.elementName = new StringUle128Reference(StringId.USAGE_METHOD_NAME);
        addChild(0, elementName);
        this.mKey = new DataKey<>(this);
    }

    @Override
    public DataKey<AnnotationElement> getKey(){
        return mKey;
    }
    public DexValueBlock<?> getValue(){
        return (DexValueBlock<?>) getChildes()[1];
    }

    @SuppressWarnings("unchecked")
    public<T1 extends DexValueBlock<?>> T1 getValue(DexValueType<T1> valueType){
        DexValueBlock<?> value = getValue();
        if(value != null && value.is(valueType)){
            return (T1) value;
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public<T1 extends DexValueBlock<?>> T1 getOrCreateValue(DexValueType<T1> valueType){
        DexValueBlock<?> value = getValue();
        if(value == null || value == NullValue.PLACE_HOLDER || value.getValueType() != valueType){
            value = valueType.newInstance();
            setValue(value);
        }
        return (T1) value;
    }
    public void setValue(DexValueBlock<?> dexValue){
        addChild(1, dexValue);
    }
    public boolean is(DexValueType<?> valueType){
        return getValueType() == valueType;
    }
    public boolean is(MethodKey methodKey) {
        return methodKey != null &&
                methodKey.equalsIgnoreReturnType(getMethodKey());
    }
    public DexValueType<?> getValueType(){
        DexValueBlock<?> value = getValue();
        if(value != null){
            return value.getValueType();
        }
        return null;
    }
    public String getName(){
        return elementName.getString();
    }
    public void setName(String name){
        elementName.setString(name);
    }
    public StringId getNameId(){
        return elementName.getItem();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        this.elementName.onReadBytes(reader);
        DexValueBlock<?> value = DexValueType.create(reader);
        setValue(value);
        value.onReadBytes(reader);
    }

    public void replaceKeys(Key search, Key replace){
        getValue().replaceKeys(search, replace);
    }
    @Override
    public Iterator<IdItem> usedIds(){
        return CombiningIterator.singleOne(getNameId(), getValue().usedIds());
    }
    public void merge(AnnotationElement element){
        if(element == this){
            return;
        }
        setName(element.getName());
        DexValueBlock<?> coming = element.getValue();
        DexValueBlock<?> value = getOrCreateValue(coming.getValueType());
        value.merge(coming);
    }
    public void fromSmali(SmaliAnnotationElement element){
        setName(element.getName());
        SmaliValue smaliValue = element.getValue();
        DexValueBlock<?> value = getOrCreateValue(smaliValue.getValueType());
        value.fromSmali(smaliValue);
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getName());
        writer.append(" = ");
        getValue().append(writer);
    }


    @Override
    public int compareTo(AnnotationElement other) {
        if(other == null){
            return -1;
        }
        if(other == this){
            return 0;
        }
        return SectionTool.compareIdx(getNameId(), other.getNameId());
    }
    public TypeKey getDataTypeKey(){
        DexValueBlock<?> valueBlock = getValue();
        if(valueBlock != null){
            return valueBlock.getDataTypeKey();
        }
        return null;
    }
    public TypeKey getParentType(){
        AnnotationItem parent = getParentInstance(AnnotationItem.class);
        if(parent != null){
            return parent.getTypeKey();
        }
        return null;
    }
    public MethodKey getMethodKey(){
        TypeKey parentType = getParentType();
        if(parentType == null){
            return null;
        }
        TypeKey dataType = getDataTypeKey();
        if(dataType == null){
            return null;
        }
        return new MethodKey(parentType, getName(), null, dataType);
    }

    @Override
    public Iterator<Key> usedKeys() {
        return CombiningIterator.singleOne(getMethodKey(), super.usedKeys());
    }
    @Override
    public int hashCode() {
        int hash = 1;
        Object obj = getName();
        hash = hash * 31;
        if(obj != null){
            hash += obj.hashCode();
        }
        obj = getValue();
        hash = hash * 31;
        if(obj != null){
            hash = hash + obj.hashCode();
        }
        return hash;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AnnotationElement element = (AnnotationElement) obj;
        if(!Objects.equals(getName(), element.getName())){
            return false;
        }
        return Objects.equals(getValue(), element.getValue());
    }

    @Override
    public String toString() {
        return getName() + " = " + getValue();
    }

    public static final Creator<AnnotationElement> CREATOR = new Creator<AnnotationElement>() {
        @Override
        public AnnotationElement[] newArrayInstance(int length) {
            if(length == 0){
                return EMPTY;
            }
            return new AnnotationElement[length];
        }
        @Override
        public AnnotationElement newInstance() {
            return new AnnotationElement();
        }
    };
    private static final AnnotationElement[] EMPTY = new AnnotationElement[0];
}
