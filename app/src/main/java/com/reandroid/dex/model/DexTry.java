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

import com.reandroid.dex.base.DexException;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.ins.ExceptionHandler;
import com.reandroid.dex.ins.ExceptionLabel;
import com.reandroid.dex.ins.Ins;
import com.reandroid.dex.ins.TryItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.FilterIterator;

import java.io.IOException;
import java.util.Iterator;

public class DexTry extends DexCode {

    private final DexMethod dexMethod;
    private final TryItem tryItem;

    public DexTry(DexMethod dexMethod, TryItem tryItem) {
        super();
        this.dexMethod = dexMethod;
        this.tryItem = tryItem;
    }

    public DexInstruction getFirst(){
        return CollectionUtil.getFirst(getInstructions());
    }
    public void setFirst(DexInstruction instruction){
        setStartAddress(instruction.getAddress());
    }
    public DexInstruction getLast(){
        return CollectionUtil.getLast(getInstructions());
    }
    public void setLast(DexInstruction instruction){
        setEndAddress(instruction.getAddress() + instruction.getCodeUnits());
    }

    public Iterator<DexInstruction> getInstructions(){
        InstructionList instructionList = getDexMethod()
                .getDefinition()
                .getInstructionList();
        if(instructionList == null){
            return EmptyIterator.of();
        }
        Iterator<Ins> iterator = FilterIterator.of(instructionList.iterator(),
                ins -> {
                    int address = ins.getAddress();
                    return address >= getStartAddress() && address < getEndAddress();
                });
        return DexInstruction.create(getDexMethod(), iterator);
    }
    public int getStartAddress(){
        return getTryItem().getStartAddress();
    }
    public void setStartAddress(int address){
        getTryItem().setStartAddress(address);
    }
    public int getEndAddress(){
        TryItem tryItem = getTryItem();
        return tryItem.getStartAddress() + tryItem.getCatchCodeUnit();
    }
    public void setEndAddress(int address){
        TryItem tryItem = getTryItem();
        int start = tryItem.getStartAddress();
        if(address < start){
            throw new DexException("Invalid try end address "
                    + address + "<" + start);
        }
        getTryItem().setCatchCodeUnit(address - start);
    }
    public DexCatch getCatchAll(){
        return create(getTryItem().getCatchAllHandler());
    }
    public DexCatch getOrCreateCatchAll() {
        TryItem tryItem = getTryItem();
        boolean hasCatchAll = tryItem.hasCatchAllHandler();
        DexCatch dexCatch = create(getTryItem().getOrCreateCatchAll());
        if(!hasCatchAll){
            dexCatch.setCatchAddress(getEndAddress());
        }
        return dexCatch;
    }
    public DexCatch getCatch(TypeKey typeKey) {
        return create(getTryItem().getExceptionHandler(typeKey));
    }
    public int getCatchCount() {
        TryItem tryItem = getTryItem();
        int count = 0;
        if(tryItem.hasCatchAllHandler()){
            count = 1;
        }
        count += tryItem.getCatchTypedHandlersCount();
        return count;
    }
    public Iterator<DexCatch> getCatches(){
        return ComputeIterator.of(getTryItem().getExceptionHandlers(),
                this::create);
    }
    DexCatch create(ExceptionHandler handler){
        if(handler != null){
            return new DexCatch(this, handler);
        }
        return null;
    }
    @Override
    public void removeSelf(){
        getTryItem().removeSelf();
    }
    public TryItem getTryItem() {
        return tryItem;
    }

    @Override
    public boolean uses(Key key) {
        Iterator<DexCatch> iterator = getCatches();
        while (iterator.hasNext()){
            DexCatch dexCatch = iterator.next();
            if(dexCatch.uses(key)){
                return true;
            }
        }
        return false;
    }

    @Override
    public DexMethod getDexMethod() {
        return dexMethod;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        TryItem tryItem = getTryItem();
        Iterator<ExceptionHandler> handlers = tryItem.getExceptionHandlers();
        if(!handlers.hasNext()){
            writer.appendComment("Empty try-catch");
            writer.newLine();
            return;
        }
        Object previous = null;
        while (handlers.hasNext()){
            ExceptionLabel label = handlers.next().getStartLabel();
            if(label.isEqualExtraLine(previous)){
                continue;
            }
            previous = label;
            writer.newLine();
            label.appendExtra(writer);
        }
        writer.indentPlus();
        Iterator<DexInstruction> instructions = getInstructions();
        writer.appendAll(instructions, true);
        writer.indentMinus();
        handlers = tryItem.getExceptionHandlers();
        previous = null;
        while (handlers.hasNext()){
            ExceptionLabel label = handlers.next().getEndLabel();
            if(label.isEqualExtraLine(previous)){
                continue;
            }
            previous = label;
            writer.newLine();
            label.appendExtra(writer);
        }
        handlers = tryItem.getExceptionHandlers();
        while (handlers.hasNext()){
            ExceptionLabel label = handlers.next().getHandlerLabel();
            writer.newLine();
            label.appendExtra(writer);
        }
    }

    public static Iterator<DexTry> create(DexMethod dexMethod, Iterator<TryItem> iterator){
        if(dexMethod == null){
            return EmptyIterator.of();
        }
        return ComputeIterator.of(iterator, tryItem -> create(dexMethod, tryItem));
    }
    public static DexTry create(DexMethod dexMethod, TryItem tryItem) {
        if(dexMethod == null || tryItem == null){
            return null;
        }
        return new DexTry(dexMethod, tryItem);
    }
}
