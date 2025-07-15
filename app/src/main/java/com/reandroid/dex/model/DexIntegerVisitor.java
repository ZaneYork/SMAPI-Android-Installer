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

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.data.*;
import com.reandroid.dex.ins.*;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.value.AnnotationValue;
import com.reandroid.dex.value.ArrayValue;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.IntValue;
import com.reandroid.utils.collection.*;

import java.util.Iterator;

public class DexIntegerVisitor extends CombiningIterator<IntegerReference, IntegerReference> {

    public DexIntegerVisitor(DexClassRepository classRepository) {
        super(getEncodedArrayReferences(classRepository), getCodeItemReferences(classRepository));
    }

    private static Iterator<IntegerReference> getEncodedArrayReferences(DexClassRepository repository) {
        return new IterableIterator<EncodedArray, IntegerReference>(
                repository.getItems(SectionType.ENCODED_ARRAY)
        ) {
            @Override
            public Iterator<IntegerReference> iterator(EncodedArray element) {
                return DexIntegerVisitor.iterator(element);
            }
        };
    }
    private static Iterator<IntegerReference> getCodeItemReferences(DexClassRepository repository) {
        return new IterableIterator<CodeItem, IntegerReference>(
                repository.getItems(SectionType.CODE)
        ) {
            @Override
            public Iterator<IntegerReference> iterator(CodeItem element) {
                return DexIntegerVisitor.iterator(element);
            }
        };
    }

    static Iterator<IntegerReference> iterator(CodeItem codeItem) {
        return new IterableIterator<Ins, IntegerReference>(codeItem.getInstructionList().iterator()) {
            @Override
            public Iterator<IntegerReference> iterator(Ins element) {
                return DexIntegerVisitor.iterator(element);
            }
        };
    }

    static Iterator<IntegerReference> iterator(Ins ins) {
        if (ins instanceof ConstNumber) {
            return SingleIterator.of((ConstNumber) ins);
        } else if (ins instanceof PayloadData) {
            return ((PayloadData) ins).getReferences();
        } else {
            return EmptyIterator.of();
        }
    }
    static Iterator<IntegerReference> iterator(EncodedArray encodedArray) {
        return new IterableIterator<DexValueBlock<?>, IntegerReference>(encodedArray.iterator()) {
            @Override
            public Iterator<IntegerReference> iterator(DexValueBlock<?> element) {
                return DexIntegerVisitor.iterator(element);
            }
        };
    }
    static Iterator<IntegerReference> iterator(DexValueBlock<?> valueBlock) {
        if(valueBlock instanceof IntValue){
            return SingleIterator.of((IntValue) valueBlock);
        }else if(valueBlock instanceof ArrayValue) {
            return iterator((ArrayValue) valueBlock);
        }else if(valueBlock instanceof AnnotationValue) {
            return iterator(((AnnotationValue) valueBlock).get());
        }
        return EmptyIterator.of();
    }
    private static Iterator<IntegerReference> iterator(ArrayValue arrayValue) {
        return new IterableIterator<DexValueBlock<?>, IntegerReference>(arrayValue.iterator()) {
            @Override
            public Iterator<IntegerReference> iterator(DexValueBlock<?> element) {
                return DexIntegerVisitor.iterator(element);
            }
        };
    }
    private static Iterator<IntegerReference> iterator(AnnotationItem arrayValue) {
        return new IterableIterator<AnnotationElement, IntegerReference>(arrayValue.iterator()) {
            @Override
            public Iterator<IntegerReference> iterator(AnnotationElement element) {
                return DexIntegerVisitor.iterator(element.getValue());
            }
        };
    }
}
