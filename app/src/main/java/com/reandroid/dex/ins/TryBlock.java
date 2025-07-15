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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.dex.base.DexPositionAlign;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.data.CodeItem;
import com.reandroid.dex.data.FixedDexContainerWithTool;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.smali.model.SmaliCodeTryItem;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.ExpandIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class TryBlock extends FixedDexContainerWithTool implements
        Creator<TryItem>, Iterable<TryItem>, LabelsSet {

    private final CodeItem codeItem;
    private HandlerOffsetArray handlerOffsetArray;
    private Ule128Item tryItemsCount;
    private ByteArray unknownBytes;
    private BlockList<TryItem> tryItemArray;
    private DexPositionAlign positionAlign;

    public TryBlock(CodeItem codeItem) {
        super(5);
        this.codeItem = codeItem;
    }

    InstructionList getInstructionList(){
        return getCodeItem().getInstructionList();
    }
    private CodeItem getCodeItem(){
        return codeItem;
    }
    public int getTryItemCount() {
        if(isNull()){
            return 0;
        }
        return tryItemArray.getCount();
    }
    public int getRealTryItemCount(){
        BlockList<TryItem> array = this.tryItemArray;
        if(array == null){
            return 0;
        }
        int count = 0;
        int size = array.size();
        for(int i = 0; i < size; i++){
            TryItem tryItem = array.get(i);
            if(!tryItem.isCopy()){
                count ++;
            }
        }
        return count;
    }
    @Override
    public Iterator<Label> getLabels() {
        return new ExpandIterator<>(iterator());
    }

    public TryItem createNext(){
        initialize();
        TryItem tryItem = newInstance();
        add(tryItem);
        return tryItem;
    }
    private void add(TryItem tryItem){
        if(tryItemArray != null){
            tryItemArray.add(tryItem);
            handlerOffsetArray.ensureSize(tryItemArray.size());
        }
    }
    public TryItem get(int i){
        if(tryItemArray != null){
            return tryItemArray.get(i);
        }
        return null;
    }
    @Override
    public Iterator<TryItem> iterator(){
        if(isNull()){
            return EmptyIterator.of();
        }
        return tryItemArray.iterator();
    }
    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        if(isNull()){
            return;
        }
        updateHandlerOffsets();
        updateCount();
        positionAlign.align(this);
    }
    private void updateCount(){
        if(isNull()){
            return;
        }
        this.tryItemsCount.set(getRealTryItemCount());
    }
    private void updateHandlerOffsets(){
        BlockList<TryItem> array = this.tryItemArray;
        if(array == null){
            return;
        }
        int baseOffset = this.tryItemsCount.countBytes();
        HandlerOffsetArray offsetArray = this.handlerOffsetArray;
        int size = array.getCount();
        offsetArray.setSize(size);
        for(int i = 0; i < size; i++){
            TryItem item = array.get(i);
            HandlerOffset offset = offsetArray.get(i);
            int count = array.countUpTo(item);
            count += baseOffset;
            offset.setOffset(count);
        }
    }
    private HandlerOffsetArray initHandlersOffset() {
        if(handlerOffsetArray == null){
            handlerOffsetArray = new HandlerOffsetArray(getCodeItem().getTryCountReference());
            addChild(INDEX_offsetArray, handlerOffsetArray);
        }
        return handlerOffsetArray;
    }
    private void initTryItemArray(){
        if(tryItemArray != null){
            return;
        }
        tryItemsCount = new Ule128Item();
        addChild(INDEX_itemsCount, tryItemsCount);
        tryItemArray = new BlockList<>(this);
        addChild(INDEX_itemArray, tryItemArray);
    }
    @Override
    public void setNull(boolean is_null){
        if(is_null == this.isNull()){
            return;
        }
        if(is_null){
            clear();
        }else {
            initialize();
        }
    }
    private void initialize(){
        initHandlersOffset();
        initTryItemArray();
        if(positionAlign == null){
            positionAlign = new DexPositionAlign();
            addChild(INDEX_positionAlign, positionAlign);
        }
    }
    private void clear(){
        if(handlerOffsetArray != null){
            handlerOffsetArray.setParent(null);
            handlerOffsetArray.setIndex(-1);
            handlerOffsetArray = null;
        }
        if(tryItemsCount != null){
            tryItemsCount.setParent(null);
            tryItemsCount.setIndex(-1);
            tryItemArray = null;
        }
        if(tryItemArray != null){
            tryItemArray.clearChildes();
            tryItemArray.setParent(null);
            tryItemArray.setIndex(-1);
            tryItemArray = null;
        }
        if(positionAlign != null){
            positionAlign.setParent(null);
            positionAlign.setIndex(-1);
            positionAlign = null;
        }
        addChild(INDEX_offsetArray, null);
        addChild(INDEX_itemsCount, null);
        addChild(INDEX_unknownBytes, null);
        addChild(INDEX_itemArray, null);
        addChild(INDEX_positionAlign, null);
    }
    @Override
    public boolean isNull(){
        return tryItemArray == null;
    }

    public void remove(TryItem tryItem){
        BlockList<TryItem> tryItemArray = this.tryItemArray;
        if(tryItemArray != null){
            if(tryItemArray.remove(tryItem)){
                tryItem.onRemove();
            }
        }
    }
    public void onRemove(){
        BlockList<TryItem> tryItemArray = this.tryItemArray;
        if(tryItemArray != null){
            this.tryItemArray = null;
            int count = tryItemArray.getCount();
            for(int i = 0; i < count; i++){
                TryItem tryItem = tryItemArray.getLast();
                tryItem.onRemove();
            }
            tryItemArray.clearChildes();
        }
        HandlerOffsetArray array = this.handlerOffsetArray;
        if(array != null){
            array.setSize(0);
            this.handlerOffsetArray = null;
        }
        DexPositionAlign positionAlign = this.positionAlign;
        if(positionAlign != null){
            this.positionAlign = null;
        }
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        boolean is_null = getCodeItem().getTryCountReference().get() == 0;
        setNull(is_null);
        if(is_null){
            return;
        }
        this.handlerOffsetArray.onReadBytes(reader);
        this.tryItemsCount.onReadBytes(reader);
        readUnknownBytes(reader);
        this.tryItemArray.setSize(handlerOffsetArray.size());
        this.tryItemArray.readChildes(reader);
        this.positionAlign.onReadBytes(reader);
    }
    private void setUnknownBytes(int count) {
        if(count <= 0 || isNull()) {
            ByteArray unknown = this.unknownBytes;
            if(unknown != null) {
                unknown.setParent(null);
                unknown.setIndex(-1);
                unknown.setSize(0);
                this.unknownBytes = null;
            }
            return;
        }
        ByteArray unknown = this.unknownBytes;
        if(unknown == null) {
            unknown = new ByteArray(count);
            this.unknownBytes = unknown;
            unknown.setParent(this);
            addChild(INDEX_unknownBytes, unknown);
        }else {
            unknown.setSize(count);
        }
    }
    private void readUnknownBytes(BlockReader reader) throws IOException {
        int minStart = this.handlerOffsetArray.getMinStart();
        minStart = minStart - this.tryItemsCount.countBytes();
        setUnknownBytes(minStart);
        ByteArray unknown = this.unknownBytes;
        if(unknown != null){
            unknown.readBytes(reader);
        }
    }

    public DexPositionAlign getPositionAlign(){
        return positionAlign;
    }

    @Override
    public TryItem newInstance() {
        return new TryItem(initHandlersOffset());
    }
    public TryItem[] newArrayInstance(int length) {
        return new TryItem[length];
    }
    @Override
    public TryItem newInstanceAt(int index) {
        BlockList<TryItem> tryItemArray = this.tryItemArray;
        HandlerOffsetArray offsetArray = initHandlersOffset();
        if(tryItemArray.size() < 2){
            return new TryItem(offsetArray);
        }
        int i = offsetArray.indexOf(offsetArray.getOffset(index));
        TryItem tryItem = null;
        if(i >= 0 && i < index){
            tryItem = tryItemArray.get(i);
            if(tryItem != null){
                tryItem = tryItem.newCopy();
            }
        }
        if(tryItem == null){
            tryItem = new TryItem(offsetArray);
        }
        return tryItem;
    }

    public void merge(TryBlock tryBlock){
        boolean is_null = tryBlock.isNull();
        setNull(is_null);
        if(is_null){
            return;
        }
        int count = tryBlock.getTryItemCount();
        for(int i = 0; i < count; i++){
            TryItem coming = tryBlock.get(i);
            TryItem comingSource = coming.getTryItem();
            TryItem tryItem;
            if(coming != comingSource){
                tryItem = get(comingSource.getIndex()).newCopy();
            }else {
                tryItem = newInstance();
            }
            add(tryItem);
            tryItem.merge(coming);
        }
        updateHandlerOffsets();
        updateCount();
    }
    public void fromSmali(SmaliCodeTryItem smaliCodeTryItem){
        initialize();
        TryItem tryItem = newInstance();
        add(tryItem);
        initHandlersOffset().setSize(tryItemArray.size());
        tryItem.fromSmali(smaliCodeTryItem);
        updateHandlerOffsets();
        updateCount();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TryBlock tryBlock = (TryBlock) obj;
        if(isNull()){
            return tryBlock.isNull();
        }
        return Objects.equals(handlerOffsetArray, tryBlock.handlerOffsetArray) &&
                Objects.equals(tryItemArray, tryBlock.tryItemArray);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        Object obj = handlerOffsetArray;
        hash = hash * 31;
        if(obj != null){
            hash = hash + obj.hashCode();
        }
        obj = tryItemArray;
        hash = hash * 31;
        if(obj != null){
            hash = hash + obj.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        if(isNull()){
            return "NULL";
        }
        return "tryItems = " + tryItemArray.toString() + ", bytes="+countBytes();
    }

    private static final int INDEX_offsetArray = 0;
    private static final int INDEX_itemsCount = 1;
    private static final int INDEX_unknownBytes = 2;
    private static final int INDEX_itemArray = 3;
    private static final int INDEX_positionAlign = 4;
}
