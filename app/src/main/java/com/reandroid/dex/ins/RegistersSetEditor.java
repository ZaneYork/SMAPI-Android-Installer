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
package com.reandroid.dex.ins;

import com.reandroid.common.ArraySupplier;
import com.reandroid.dex.common.RegistersTable;
import com.reandroid.utils.collection.ArraySupplierIterator;
import com.reandroid.utils.collection.EmptyIterator;

import java.util.Iterator;

public class RegistersSetEditor implements ArraySupplier<RegisterReference.Editor>, Iterable<RegisterReference.Editor> {

    private RegisterReference.Editor[] elements;
    private int size;

    public RegistersSetEditor(){
        
    }
    public void apply(){
        for(RegisterReference.Editor editor : this){
            editor.apply();
        }
    }
    public boolean contains(Object obj) {
        if(obj == null){
            return false;
        }
        RegisterReference.Editor[] elements = this.elements;
        if(elements == null){
            return false;
        }
        for(RegisterReference.Editor reg : elements){
            if(obj.equals(reg)){
                return true;
            }
        }
        return false;
    }
    public boolean isEmpty(){
        return getCount() != 0;
    }
    @Override
    public Iterator<RegisterReference.Editor> iterator() {
        return ArraySupplierIterator.of(this);
    }
    @Override
    public RegisterReference.Editor get(int i){
        if(elements != null){
            return elements[i];
        }
        return null;
    }
    @Override
    public int getCount(){
        return size;
    }
    public void add(Iterator<RegisterReference.Editor> iterator){
        while (iterator.hasNext()){
            add(iterator.next());
        }
    }
    public void add(RegisterReference.Editor editor){
        if (editor == null){
            return;
        }
        ensureCapacity();
        this.elements[size] = editor;
        this.size ++;
    }
    public void trimToSize(){
        if(availableCapacity() == 0){
            return;
        }
        int size = this.size;
        if(size == 0){
            this.elements = null;
            return;
        }
        RegisterReference.Editor[] update = new RegisterReference.Editor[size];
        System.arraycopy(this.elements, 0, update, 0, size);
        this.elements = update;
    }
    private void ensureCapacity(){
        int capacity;
        if(this.size == 0){
            capacity = 1;
        }else {
            capacity = DEFAULT_CAPACITY;
        }
        ensureCapacity(capacity);
    }
    private void ensureCapacity(int capacity) {
        if(availableCapacity() >= capacity){
            return;
        }
        int size = this.size;
        int length = size + capacity;
        RegisterReference.Editor[] update = new RegisterReference.Editor[length];
        RegisterReference.Editor[] elements = this.elements;
        if(elements == null || size == 0){
            this.elements = update;
            return;
        }
        System.arraycopy(elements, 0, update, 0, size);
        this.elements = update;
    }
    private int availableCapacity(){
        RegisterReference.Editor[] elements = this.elements;
        if(elements != null){
            return elements.length - size;
        }
        return 0;
    }

    public static RegistersSetEditor of(RegistersTable registersTable, RegistersSet registersSet){
        if(registersSet == null){
            return EMPTY;
        }
        int count = registersSet.getRegistersCount();
        if(count == 0){
            return EMPTY;
        }
        RegistersSetEditor registersSetEditor = new RegistersSetEditor();
        registersSetEditor.ensureCapacity(count);
        for(int i = 0; i < count; i++){
            RegisterReference registerReference = new RegisterReference(registersTable, registersSet, i);
            registersSetEditor.add(registerReference.toEditor());
        }
        return registersSetEditor;
    }


    public static final RegistersSetEditor EMPTY = new RegistersSetEditor(){

        @Override
        public void add(Iterator<RegisterReference.Editor> iterator) {
            if(iterator == null || !iterator.hasNext()){
                return;
            }
            throw new IllegalArgumentException("Empty RegistersList");
        }
        @Override
        public void add(RegisterReference.Editor reg) {
            if(reg != null){
                throw new IllegalArgumentException("Empty RegistersList");
            }
        }
        @Override
        public Iterator<RegisterReference.Editor> iterator() {
            return EmptyIterator.of();
        }

        @Override
        public boolean contains(Object obj) {
            return false;
        }
        @Override
        public boolean isEmpty() {
            return true;
        }
        @Override
        public RegisterReference.Editor get(int i) {
            throw new IllegalArgumentException("Empty RegistersList");
        }
        @Override
        public int getCount() {
            return 0;
        }
        @Override
        public void trimToSize() {
        }
    };

    private static final int DEFAULT_CAPACITY = 2;
}
