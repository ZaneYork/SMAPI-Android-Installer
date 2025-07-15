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
package com.reandroid.dex.resource;

import android.text.TextUtils;

import com.reandroid.apk.XmlHelper;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.NumberIntegerReference;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.value.AttributeType;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.dex.ins.InsArrayData;
import com.reandroid.dex.ins.InsFillArrayData;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexField;
import com.reandroid.dex.model.DexInstruction;
import com.reandroid.dex.model.DexMethod;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.EmptyIterator;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;

public class RDeclareStyleable extends RStyleableItem implements Iterable<IntegerReference>,
        Comparable<RDeclareStyleable> {

    public RDeclareStyleable(DexField dexField) {
        super(dexField);
    }

    @SuppressWarnings("all")
    public DeclareStyleable toDeclareStyleable(TableBlock tableBlock) {
        DeclareStyleable declareStyleable = new DeclareStyleable(getName());
        ArrayCollection<DeclareStyleable.Attr> attrList = new ArrayCollection<>();
        for(IntegerReference reference : this) {
            declareStyleable.add(toAttr(declareStyleable, tableBlock, reference));
        }
        return declareStyleable;
    }
    private DeclareStyleable.Attr toAttr(DeclareStyleable parent, TableBlock tableBlock, IntegerReference reference) {
        int id = reference.get();
        ResourceEntry resourceEntry = tableBlock.getResource(id);
        if(resourceEntry == null || resourceEntry.isEmpty()) {
            return null;
        }
        String name = resourceEntry.getName();
        if(!resourceEntry.isContext(tableBlock)) {
            name = resourceEntry.getPackageName() + ":" + name;
        }
        String format = decodeFormatValue(resourceEntry);
        return new DeclareStyleable.Attr(parent, id, name, format);
    }
    @Override
    public Iterator<IntegerReference> iterator() {
        DexInstruction sputObject = getSputObjectInstruction();
        if(sputObject == null) {
            return EmptyIterator.of();
        }
        InsArrayData arrayData = findArrayData(sputObject);
        if(arrayData != null) {
            return arrayData.getReferences();
        }
        PrimitiveArrayFillSequence sequence = new PrimitiveArrayFillSequence(sputObject);
        if(sequence.isValid()) {
            return sequence.iterator();
        }
        return EmptyIterator.of();
    }
    private InsArrayData findArrayData(DexInstruction sputObject) {
        int reg = sputObject.getRegister();
        DexInstruction previous = sputObject.getPrevious();
        while (previous != null) {
            if(previous.getRegister() == reg) {
                if(previous.is(Opcode.FILL_ARRAY_DATA)) {
                    return ((InsFillArrayData) previous.getIns()).getInsArrayData();
                }
                if (previous.is(Opcode.MOVE_OBJECT) ||
                        previous.is(Opcode.MOVE_OBJECT_16) ||
                        previous.is(Opcode.MOVE_OBJECT_FROM16)) {
                    reg = previous.getRegister(1);
                } else {
                    return null;
                }
            }
            previous = previous.getPrevious();
        }
        return null;
    }
    private DexInstruction getSputObjectInstruction() {
        DexClass dexClass = getDexField().getDexClass();
        DexMethod staticConstructor = dexClass.getStaticConstructor();
        if(staticConstructor == null) {
            return null;
        }
        FieldKey fieldKey = getKey();
        Iterator<DexInstruction> iterator = staticConstructor.getInstructions();
        while (iterator.hasNext()) {
            DexInstruction instruction = iterator.next();
            if(instruction.is(Opcode.SPUT_OBJECT) && fieldKey.equals(instruction.getFieldKey())) {
                return instruction;
            }
        }
        return null;
    }

    @Override
    public void serialize(TableBlock tableBlock, XmlSerializer serializer) throws IOException {
        String tag = "declare-styleable";
        serializer.startTag(null, tag);
        serializer.attribute(null, "name", getName());
        for(IntegerReference reference : this) {
            ResourceEntry resourceEntry = tableBlock.getResource(reference.get());
            if(resourceEntry == null || resourceEntry.isEmpty()) {
                continue;
            }
            serializer.startTag(null, "attr");
            XmlHelper.setIndent(serializer, false);

            decodeAttributeName(tableBlock, resourceEntry, serializer);
            decodeFormat(resourceEntry, serializer);

            XmlHelper.setIndent(serializer, true);
            serializer.endTag(null, "attr");
        }
        serializer.endTag(null, tag);
    }
    private void decodeFormat(ResourceEntry resourceEntry, XmlSerializer serializer) throws IOException {
        String value = decodeFormatValue(resourceEntry);
        if(!TextUtils.isEmpty(value)) {
            serializer.attribute(null, "format", value);
        }
    }
    private String decodeFormatValue(ResourceEntry resourceEntry) {
        Entry entry = resourceEntry.get();
        ResTableMapEntry mapEntry = entry.getResTableMapEntry();
        if(mapEntry != null) {
            ResValueMap format = mapEntry.getByType(AttributeType.FORMATS);
            if(format != null) {
                return format.decodeValue();
            }
        }
        return null;
    }
    private void decodeAttributeName(TableBlock context, ResourceEntry resourceEntry, XmlSerializer serializer) throws IOException {
        String name = resourceEntry.getName();
        if(!resourceEntry.isContext(context)) {
            name = resourceEntry.getPackageName() + ":" + name;
        }
        serializer.attribute(null, "name", name);
    }

    @Override
    public void appendJavaValue(SmaliWriter writer) throws IOException {
        boolean appendOnce = false;
        Iterator<IntegerReference> iterator = iterator();
        writer.append("new int[]{");
        while (iterator.hasNext()) {
            IntegerReference reference = iterator.next();
            if(appendOnce) {
                writer.append(", ");
            }
            writer.appendHex(reference.get());
            appendOnce = true;
        }
        writer.append('}');
    }

    @Override
    public boolean isValid() {
        if(!super.isValid()) {
            return false;
        }
        int count = 0;
        for(IntegerReference reference : this) {
            if(!PackageBlock.isResourceId(reference.get())) {
                return false;
            }
            count ++;
        }
        return count != 0;
    }

    @Override
    public int compareTo(RDeclareStyleable styleable) {
        return CompareUtil.compare(getName(), styleable.getName());
    }

    static class PrimitiveArrayFillSequence implements Iterable<IntegerReference> {
        private final DexInstruction sputObject;
        private DexInstruction sizeInstruction;
        private DexInstruction newArrayInstruction;

        PrimitiveArrayFillSequence(DexInstruction sputObject) {
            this.sputObject = sputObject;
        }

        @Override
        public Iterator<IntegerReference> iterator() {
            if(!isValid()) {
                return null;
            }
            return new Iterator<IntegerReference>() {
                private int index;
                @Override
                public boolean hasNext() {
                    return index < size();
                }
                @Override
                public IntegerReference next() {
                    IntegerReference reference = getValueReference(index);
                    index ++;
                    return reference;
                }
            };
        }
        public int size() {
            DexInstruction instruction = getSizeInstruction();
            if(instruction != null) {
                return instruction.getAsInteger();
            }
            return -1;
        }

        private IntegerReference getValueReference(int index) {
            if(!isValid()) {
                return null;
            }
            DexInstruction instruction = getValueInstruction(index);
            if(instruction == null) {
                return new NumberIntegerReference();
            }
            return new IntegerReference() {
                @Override
                public int get() {
                    return instruction.getAsInteger();
                }
                @Override
                public void set(int value) {
                    instruction.setAsInteger(value);
                }
            };
        }
        public boolean isValid() {
            return getSizeInstruction() != null;
        }
        private DexInstruction getValueInstruction(int index) {
            DexInstruction aput = sputObject.getPreviousReader(sputObject.getRegister(),
                    Opcode.APUT);
            while (aput != null) {
                DexInstruction indexInstruction = aput.getPreviousSetter(aput.getRegister(2));
                if(indexInstruction == null) {
                    return null;
                }
                if(indexInstruction.getAsInteger() == index) {
                    return aput.getPreviousSetter(aput.getRegister(0),
                            DexInstruction::isNumber);
                }
                aput = aput.getPreviousReader(aput.getRegister(1),
                        Opcode.APUT);
            }
            return null;
        }
        private DexInstruction getSizeInstruction() {
            if(this.sizeInstruction != null) {
                return this.sizeInstruction;
            }
            DexInstruction newArray = getNewArrayInstruction();
            if(newArray == null) {
                return null;
            }
            DexInstruction previous = newArray.getPreviousSetter(newArray.getRegister(1),
                    DexInstruction::isNumber);
            if(previous != null) {
                this.sizeInstruction = previous;
                return previous;
            }
            return null;
        }
        private DexInstruction getNewArrayInstruction() {
            if(newArrayInstruction != null) {
                return newArrayInstruction;
            }
            DexInstruction previous = sputObject.getPreviousSetter(sputObject.getRegister(), Opcode.NEW_ARRAY);
            if(previous != null) {
                this.newArrayInstruction = previous;
                return previous;
            }
            return null;
        }
    }

    public static final TypeKey INT_ARRAY = TypeKey.TYPE_I.setArrayDimension(1);
}
