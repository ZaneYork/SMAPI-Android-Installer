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

import com.reandroid.dex.common.RegistersTable;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.IndexIterator;
import com.reandroid.utils.collection.SizedSupplier;

import java.io.IOException;
import java.util.Iterator;

public class RegistersIterator implements SizedSupplier<RegisterReference>, Iterable<RegisterReference>, SmaliFormat {

    private final RegistersTable registersTable;
    private final RegistersSet registersSet;

    public RegistersIterator(RegistersTable registersTable, RegistersSet registersSet){
        this.registersTable = registersTable;
        this.registersSet = registersSet;
    }

    @Override
    public RegisterReference get(int index) {
        return new RegisterReference(getRegistersTable(), getRegistersSet(), index);
    }
    @Override
    public int size() {
        return getRegistersSet().getRegistersCount();
    }
    public void setSize(int size){
        getRegistersSet().setRegistersCount(size);
    }
    public boolean isRange(){
        return getRegistersSet().getRegisterFormat().isRange();
    }
    @Override
    public Iterator<RegisterReference> iterator() {
        return new IndexIterator<>(this);
    }
    public RegistersSet getRegistersSet() {
        return registersSet;
    }
    public RegistersTable getRegistersTable() {
        return registersTable;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        int size = size();
        if(size == 0){
            return;
        }
        if(isRange()){
            get(0).append(writer);
            writer.append(" .. ");
            get(size - 1).append(writer);
            return;
        }
        String separator = ", ";
        for(int i = 0; i < size; i++){
            if(i != 0){
                writer.append(separator);
            }
            get(i).append(writer);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RegistersIterator)) {
            return false;
        }
        RegistersIterator iterator = (RegistersIterator) obj;
        int size = size();
        if(size != iterator.size()){
            return false;
        }
        for(int i = 0; i < size; i++){
            if(!get(i).equals(iterator.get(i))){
                return false;
            }
        }
        return true;
    }
    @Override
    public int hashCode() {
        int hash = 1;
        int size = size();
        for(int i = 0; i < size; i++){
            hash = hash * 31 + get(i).hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String separator = ", ";
        int size = size();
        for(int i = 0; i < size; i++){
            if(i != 0){
                builder.append(separator);
            }
            builder.append(get(i));
        }
        return builder.toString();
    }
}
