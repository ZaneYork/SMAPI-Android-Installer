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
import com.reandroid.utils.collection.InstanceIterator;

import java.util.Iterator;

public class RegistersEditor implements ArraySupplier<RegistersSetEditor>, Iterable<RegistersSetEditor> {

    private final RegistersTable registersTable;

    private RegistersSetEditor[] elements;
    private int size;

    public RegistersEditor(RegistersTable registersTable){
        this.registersTable = registersTable;
    }
    public void apply(){
        for(RegistersSetEditor editor : this){
            editor.apply();
        }
    }
    public void addLocalRegistersCount(int amount){
        setLocalRegistersCount(getLocalRegistersCount() + amount);
    }
    public int getLocalRegistersCount(){
        RegistersTable table = getRegistersTable();
        return table.getRegistersCount() - table.getParameterRegistersCount();
    }
    public void setLocalRegistersCount(int count){
        RegistersTable table = getRegistersTable();
        int param = table.getParameterRegistersCount();
        table.setRegistersCount(count + param);
    }
    public int getParameterRegistersCount(){
        RegistersTable table = getRegistersTable();
        return table.getParameterRegistersCount();
    }

    public RegistersTable getRegistersTable() {
        return registersTable;
    }

    public boolean contains(Object obj) {
        if(obj == null){
            return false;
        }
        RegistersSetEditor[] elements = this.elements;
        if(elements == null){
            return false;
        }
        for(RegistersSetEditor reg : elements){
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
    public Iterator<RegistersSetEditor> iterator() {
        return ArraySupplierIterator.of(this);
    }
    @Override
    public RegistersSetEditor get(int i){
        if(elements != null){
            return elements[i];
        }
        return null;
    }
    @Override
    public int getCount(){
        return size;
    }
    public void add(Iterator<RegistersSetEditor> iterator){
        while (iterator.hasNext()){
            add(iterator.next());
        }
    }
    public void add(RegistersSetEditor editor){
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
        RegistersSetEditor[] update = new RegistersSetEditor[size];
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
        RegistersSetEditor[] update = new RegistersSetEditor[length];
        RegistersSetEditor[] elements = this.elements;
        if(elements == null || size == 0){
            this.elements = update;
            return;
        }
        System.arraycopy(elements, 0, update, 0, size);
        this.elements = update;
    }
    private int availableCapacity(){
        RegistersSetEditor[] elements = this.elements;
        if(elements != null){
            return elements.length - size;
        }
        return 0;
    }

    public static RegistersEditor fromIns(RegistersTable registersTable, Iterator<Ins> iterator){
        return of(registersTable, InstanceIterator.of(iterator, RegistersSet.class));
    }
    public static RegistersEditor of(RegistersTable registersTable, Iterator<RegistersSet> iterator){
        if(!iterator.hasNext()){
            return EMPTY;
        }
        RegistersEditor registersEditor = new RegistersEditor(registersTable);
        while (iterator.hasNext()){
            RegistersSet registersSet = iterator.next();
            RegistersSetEditor editor = RegistersSetEditor.of(registersTable, registersSet);
            registersEditor.add(editor);
        }
        return registersEditor;
    }


    public static final RegistersEditor EMPTY = new RegistersEditor(null){

        @Override
        public int getLocalRegistersCount() {
            throw new IllegalArgumentException("Empty RegistersEditor");
        }
        @Override
        public void setLocalRegistersCount(int count) {
            throw new IllegalArgumentException("Empty RegistersEditor");
        }

        @Override
        public void add(Iterator<RegistersSetEditor> iterator) {
            if(iterator == null || !iterator.hasNext()){
                return;
            }
            throw new IllegalArgumentException("Empty RegistersEditor");
        }
        @Override
        public void add(RegistersSetEditor reg) {
            if(reg != null){
                throw new IllegalArgumentException("Empty RegistersEditor");
            }
        }
        @Override
        public Iterator<RegistersSetEditor> iterator() {
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
        public RegistersSetEditor get(int i) {
            throw new IllegalArgumentException("Empty RegistersEditor");
        }
        @Override
        public int getCount() {
            return 0;
        }
        @Override
        public void trimToSize() {
        }
    };

    private static final int DEFAULT_CAPACITY = 100;
}
