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

import com.reandroid.arsc.item.*;
import com.reandroid.dex.base.DexBlockAlign;
import com.reandroid.dex.base.NumberArray;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliValidateException;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.dex.smali.model.SmaliPayloadArray;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.InstanceIterator;

import java.io.IOException;
import java.util.Iterator;

public class InsArrayData extends PayloadData implements SmaliRegion {

    private final NumberArray numberArray;
    private final DexBlockAlign blockAlign;

    public InsArrayData() {
        super(4, Opcode.ARRAY_PAYLOAD);
        ShortItem elementWidth = new ShortItem();
        IntegerItem elementCount = new IntegerItem();
        this.numberArray = new NumberArray(elementWidth, elementCount);

        this.blockAlign = new DexBlockAlign(this.numberArray);
        this.blockAlign.setAlignment(2);

        addChild(1, elementWidth);
        addChild(2, elementCount);
        addChild(3, this.numberArray);
        addChild(4, this.blockAlign);
    }

    public Iterator<InsFillArrayData> getInsFillArrayData() {
        InsBlockList insBlockList = getInsBlockList();
        if(insBlockList == null) {
            return EmptyIterator.of();
        }
        insBlockList.link();
        return InstanceIterator.of(getExtraLines(), InsFillArrayData.class);
    }
    public int size(){
        return getNumberArray().size();
    }
    public void setSize(int size){
        getNumberArray().setSize(size);
        refreshAlignment();
    }
    public int getWidth(){
        return getNumberArray().getWidth();
    }
    public void setWidth(int width){
        getNumberArray().setWidth(width);
        refreshAlignment();
    }

    public void set(byte[] values){
        NumberArray numberArray = getNumberArray();
        numberArray.setSize(0);
        numberArray.setWidth(1);
        numberArray.put(values);
        refreshAlignment();
    }
    public void set(short[] values){
        NumberArray numberArray = getNumberArray();
        numberArray.setSize(0);
        numberArray.setWidth(2);
        numberArray.put(values);
        refreshAlignment();
    }
    public void set(int[] values){
        NumberArray numberArray = getNumberArray();
        numberArray.setSize(0);
        numberArray.put(values);
        refreshAlignment();
    }
    public void set(long[] values){
        NumberArray numberArray = getNumberArray();
        numberArray.setSize(0);
        numberArray.setWidth(8);
        numberArray.putLong(values);
        refreshAlignment();
    }
    @Override
    public Iterator<IntegerReference> getReferences(){
        return getNumberArray().getReferences();
    }
    public IntegerReference getReference(int i){
        return getNumberArray().getReference(i);
    }
    public int getAsInteger(int index){
        return getNumberArray().getAsInteger(index);
    }
    public long getLong(int index){
        return getNumberArray().getLong(index);
    }
    public void put(int index, int value){
        getNumberArray().put(index, value);
    }
    public void putLong(int index, long value){
        getNumberArray().putLong(index, value);
    }
    public NumberArray getNumberArray() {
        return numberArray;
    }

    public void refreshAlignment(){
        this.blockAlign.align(this);
    }
    @Override
    public void appendCode(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        writer.appendInteger(getWidth());
        writer.indentPlus();
        appendData(writer);
        writer.indentMinus();
        getSmaliDirective().appendEnd(writer);
    }
    private void appendData(SmaliWriter writer) throws IOException {
        NumberArray numberArray = getNumberArray();
        int width = numberArray.getWidth();
        int size = numberArray.size();
        if(width < 2){
            for(int i = 0; i < size; i++){
                writer.newLine();
                writer.appendHex(numberArray.getByte(i));
            }
        }else if(width < 4){
            for(int i = 0; i < size; i++){
                writer.newLine();
                writer.appendHex(numberArray.getShort(i));
            }
        }else if(width == 4){
            for(int i = 0; i < size; i++){
                writer.newLine();
                writer.appendHex(numberArray.getInteger(i));
            }
        }else {
            for(int i = 0; i < size; i++){
                writer.newLine();
                writer.appendHex(numberArray.getLong(i));
            }
        }
    }

    @Override
    public void merge(Ins ins){
        InsArrayData coming = (InsArrayData) ins;
        getNumberArray().merge(coming.getNumberArray());
        refreshAlignment();
    }

    @Override
    public void fromSmali(SmaliInstruction smaliInstruction) throws IOException{
        validateOpcode(smaliInstruction);
        SmaliPayloadArray smaliPayloadArray = (SmaliPayloadArray) smaliInstruction;
        int width = smaliPayloadArray.getWidth();
        if(width < 1 || width > 8){
            throw new SmaliValidateException("Array values width out of range '" + width + "'", smaliInstruction);
        }
        setWidth(width);
        if(width > 4){
            set(smaliPayloadArray.unsignedLong());
        }else {
            set(smaliPayloadArray.unsignedInt());
        }
        refreshAlignment();
    }
    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.ARRAY_DATA;
    }
}