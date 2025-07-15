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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.Sle128Item;
import com.reandroid.dex.data.FixedDexContainerWithTool;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.model.SmaliCodeCatch;
import com.reandroid.dex.smali.model.SmaliCodeCatchAll;
import com.reandroid.dex.smali.model.SmaliCodeTryItem;
import com.reandroid.dex.smali.model.SmaliSet;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Objects;

public class TryItem extends FixedDexContainerWithTool implements Iterable<Label> {

    private final HandlerOffsetArray handlerOffsetArray;

    final Sle128Item handlersCount;
    private final BlockList<CatchTypedHandler> catchTypedHandlerList;
    private CatchAllHandler catchAllHandler;

    private HandlerOffset mHandlerOffset;

    public TryItem(HandlerOffsetArray handlerOffsetArray) {
        super(3);

        this.handlerOffsetArray = handlerOffsetArray;
        this.handlersCount = new Sle128Item();
        this.catchTypedHandlerList = new BlockList<>();

        addChild(0, handlersCount);
        addChild(1, catchTypedHandlerList);
    }
    private TryItem() {
        super(0);
        this.handlerOffsetArray = null;

        this.handlersCount = null;
        this.catchTypedHandlerList = null;
    }

    public boolean isCopy(){
        return false;
    }
    InstructionList getInstructionList(){
        return getTryBlock().getInstructionList();
    }
    TryBlock getTryBlock(){
        return getParent(TryBlock.class);
    }

    TryItem newCopy(){
        return new Copy(this);
    }
    HandlerOffset getHandlerOffset() {
        HandlerOffset handlerOffset = this.mHandlerOffset;
        if(handlerOffset == null){
            handlerOffset = getHandlerOffsetArray().getOrCreate(getIndex());
            this.mHandlerOffset = handlerOffset;
            handlerOffset.setTryItem(this);
        }
        return handlerOffset;
    }
    HandlerOffsetArray getHandlerOffsetArray(){
        return handlerOffsetArray;
    }
    BlockList<CatchTypedHandler> getCatchTypedHandlerBlockList(){
        return catchTypedHandlerList;
    }
    Iterator<CatchTypedHandler> getCatchTypedHandlers(){
        return catchTypedHandlerList.iterator();
    }
    TryItem getTryItem(){
        return this;
    }
    void updateCount(){
        Sle128Item handlersCount = this.handlersCount;
        if(handlersCount == null){
            return;
        }
        int count = catchTypedHandlerList.size();
        if(hasCatchAllHandler()){
            count = -count;
        }
        handlersCount.set(count);
    }

    @Override
    public Iterator<Label> iterator(){
        return new ExpandIterator<>(getExceptionHandlers());
    }
    public int getCatchTypedHandlersCount(){
        return getCatchTypedHandlerBlockList().size();
    }
    public Iterator<ExceptionHandler> getExceptionHandlers(){
        Iterator<ExceptionHandler> iterator1 = EmptyIterator.of();
        ExceptionHandler handler = getCatchAllHandler();
        if(handler != null){
            iterator1 = SingleIterator.of(handler);
        }
        return new CombiningIterator<>(getCatchTypedHandlers(), iterator1);
    }
    public ExceptionHandler getExceptionHandler(TypeKey typeKey){
        Iterator<CatchTypedHandler> iterator = getCatchTypedHandlers();
        while (iterator.hasNext()){
            CatchTypedHandler handler = iterator.next();
            if(typeKey.equals(handler.getKey())){
                return handler;
            }
        }
        return null;
    }
    public int getStartAddress(){
        return getHandlerOffset().getStartAddress();
    }
    public void setStartAddress(int address){
        getHandlerOffset().setStartAddress(address);
    }
    public int getCatchCodeUnit(){
        return getHandlerOffset().getCatchCodeUnit();
    }
    public void setCatchCodeUnit(int codeUnit){
        getHandlerOffset().setCatchCodeUnit(codeUnit);
    }

    public boolean hasCatchAllHandler(){
        return getCatchAllHandler() != null;
    }
    public CatchAllHandler getCatchAllHandler(){
        return catchAllHandler;
    }
    public CatchAllHandler getOrCreateCatchAll(){
        CatchAllHandler handler = getCatchAllHandler();
        if(handler == null){
            initCatchAllHandler();
            handler = getCatchAllHandler();
        }
        return handler;
    }
    private CatchAllHandler initCatchAllHandler(){
        CatchAllHandler catchAllHandler = this.getCatchAllHandler();
        if(catchAllHandler == null){
            catchAllHandler = new CatchAllHandler();
            addChild(2, catchAllHandler);
            this.catchAllHandler = catchAllHandler;
        }
        return catchAllHandler;
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        updateCount();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int maxPosition = reader.getPosition();

        int position = getHandlerOffsetArray().getItemsStart()
                + getHandlerOffset().getOffset();
        reader.seek(position);
        this.handlersCount.readBytes(reader);
        int count = this.handlersCount.get();
        boolean hasCatchAll = false;
        if(count <= 0){
            count = -count;
            hasCatchAll = true;
        }
        BlockList<CatchTypedHandler> handlerList = this.getCatchTypedHandlerBlockList();
        handlerList.ensureCapacity(count);
        for(int i = 0; i < count; i++){
            CatchTypedHandler handler = new CatchTypedHandler();
            handlerList.add(handler);
            handler.readBytes(reader);
        }
        if(hasCatchAll){
            initCatchAllHandler().readBytes(reader);
        }
        if(maxPosition > reader.getPosition()){
            // Should never reach here
            reader.seek(maxPosition);
        }
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        Block end = counter.END;
        if(end instanceof TryItem.Copy){
            TryItem tryItem = ((TryItem.Copy) end).getTryItem();
            if(tryItem == this){
                counter.FOUND = true;
                return;
            }
        }
        super.onCountUpTo(counter);
    }
    public void removeSelf(){
        TryBlock tryBlock = getTryBlock();
        if(tryBlock != null){
            tryBlock.remove(this);
        }
    }
    public void remove(ExceptionHandler handler){
        if(handler == null){
            return;
        }
        if(handler == this.catchAllHandler){
            handler.onRemove();
            this.catchAllHandler = null;
        }else if(handler instanceof CatchTypedHandler && this.catchTypedHandlerList != null){
            if(catchTypedHandlerList.contains(handler)){
                catchTypedHandlerList.remove((CatchTypedHandler) handler);
                handler.onRemove();
            }
        }
    }
    public void onRemove(){
        HandlerOffset handlerOffset = this.mHandlerOffset;
        BlockList<CatchTypedHandler> list = this.catchTypedHandlerList;
        if(list != null){
            int size = list.size();
            for(int i = 0; i < size; i++){
                CatchTypedHandler handler = list.get(i);
                handler.onRemove();
                handler.setParent(null);
            }
            list.destroy();
        }
        remove(this.catchAllHandler);
        if(handlerOffset != null){
            this.mHandlerOffset = null;
            handlerOffset.removeSelf();
        }
        setParent(null);
    }
    public void merge(TryItem tryItem){
        mergeOffset(tryItem);
        mergeHandlers(tryItem);
    }
    void mergeHandlers(TryItem tryItem){
        BlockList<CatchTypedHandler> comingList = tryItem.getCatchTypedHandlerBlockList();
        int size = comingList.size();
        BlockList<CatchTypedHandler> handlerList = this.getCatchTypedHandlerBlockList();
        handlerList.ensureCapacity(size);
        for (int i = 0; i < size; i++){
            CatchTypedHandler coming = comingList.get(i);
            CatchTypedHandler handler = new CatchTypedHandler();
            handlerList.add(handler);
            handler.merge(coming);
        }
        if(tryItem.hasCatchAllHandler()){
            initCatchAllHandler().merge(tryItem.getCatchAllHandler());
        }
        updateCount();
    }
    void mergeOffset(TryItem tryItem){

        HandlerOffset coming = tryItem.getHandlerOffset();
        HandlerOffset handlerOffset = getHandlerOffset();

        handlerOffset.setCatchCodeUnit(coming.getCatchCodeUnit());
        handlerOffset.setStartAddress(coming.getStartAddress());
    }
    public void fromSmali(SmaliCodeTryItem smaliCodeTryItem){
        setStartAddress(smaliCodeTryItem.getStartAddress());
        SmaliSet<SmaliCodeCatch> smaliCodeCatchSet = smaliCodeTryItem.getCatchSet();

        BlockList<CatchTypedHandler> handlerList = this.getCatchTypedHandlerBlockList();
        int size = smaliCodeCatchSet.size();
        for(int i = 0; i < size; i++){
            SmaliCodeCatch smaliCodeCatch = smaliCodeCatchSet.get(i);
            CatchTypedHandler handler = new CatchTypedHandler();
            handlerList.add(handler);
            handler.fromSmali(smaliCodeCatch);
        }
        SmaliCodeCatchAll smaliCodeCatchAll = smaliCodeTryItem.getCatchAll();
        if(smaliCodeCatchAll != null){
            CatchAllHandler catchAllHandler = initCatchAllHandler();
            catchAllHandler.fromSmali(smaliCodeCatchAll);
        }
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
        TryItem tryItem = (TryItem) obj;
        return Objects.equals(catchTypedHandlerList, tryItem.catchTypedHandlerList) &&
                Objects.equals(catchAllHandler, tryItem.catchAllHandler);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31;
        Object obj = catchTypedHandlerList;
        if(obj != null){
            hash = hash * 31 + obj.hashCode();
        }
        hash = hash * 31;
        obj = catchAllHandler;
        if(obj != null){
            hash = hash * 31 + obj.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Iterator<ExceptionHandler> handlers = getExceptionHandlers();
        while (handlers.hasNext()) {
            if(builder.length() != 0){
                builder.append('\n');
            }
            builder.append(handlers.next());
        }
        return builder.toString();
    }
    static class Copy extends TryItem {

        private final TryItem tryItem;
        private ArrayCollection<CatchTypedHandler> mTypeHandlerList;
        private CatchAllHandler mOriginalCatchAllHandler;
        private CatchAllHandler mCatchAllHandler;

        public Copy(TryItem tryItem) {
            super();
            this.tryItem = tryItem;
        }

        @Override
        public boolean isCopy(){
            return true;
        }
        @Override
        TryBlock getTryBlock() {
            return tryItem.getTryBlock();
        }

        @Override
        TryItem newCopy() {
            return tryItem.newCopy();
        }
        @Override
        HandlerOffsetArray getHandlerOffsetArray(){
            return tryItem.getHandlerOffsetArray();
        }
        @Override
        Iterator<CatchTypedHandler> getCatchTypedHandlers(){
            return getTypeHandlerList().iterator();
        }
        private ArrayCollection<CatchTypedHandler> getTypeHandlerList() {
            ArrayCollection<CatchTypedHandler> typedHandlerList = this.mTypeHandlerList;
            BlockList<CatchTypedHandler> blockList = getCatchTypedHandlerBlockList();
            if(typedHandlerList == null || typedHandlerList.size() != blockList.size()) {
                typedHandlerList = new ArrayCollection<>();
                mTypeHandlerList = typedHandlerList;
                Iterator<CatchTypedHandler> iterator = blockList.iterator();
                while (iterator.hasNext()) {
                    typedHandlerList.add(iterator.next().newCopy(this));
                }
            }
            return typedHandlerList;
        }
        @Override
        BlockList<CatchTypedHandler> getCatchTypedHandlerBlockList() {
            return tryItem.getCatchTypedHandlerBlockList();
        }
        @Override
        TryItem getTryItem(){
            return tryItem.getTryItem();
        }
        @Override
        public CatchAllHandler getCatchAllHandler() {
            CatchAllHandler catchAllHandler = tryItem.getCatchAllHandler();
            if(catchAllHandler != mOriginalCatchAllHandler) {
                mOriginalCatchAllHandler = catchAllHandler;
                mCatchAllHandler = null;
            }
            if(catchAllHandler == null) {
                this.mCatchAllHandler = null;
            } else if(mCatchAllHandler == null) {
                catchAllHandler = catchAllHandler.newCopy(this);
                this.mCatchAllHandler = catchAllHandler;
            } else {
                catchAllHandler = this.mCatchAllHandler;
            }
            return catchAllHandler;
        }

        @Override
        public CatchAllHandler getOrCreateCatchAll() {
            tryItem.getOrCreateCatchAll();
            return getCatchAllHandler();
        }

        @Override
        public int countBytes() {
            return 0;
        }
        @Override
        public int onWriteBytes(OutputStream stream) throws IOException {
            return 0;
        }
        @Override
        public byte[] getBytes() {
            return null;
        }
        @Override
        protected void onPreRefresh() {
        }
        @Override
        protected void onRefreshed() {
            clearCache();
        }
        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
            clearCache();
        }
        @Override
        void updateCount(){
        }
        @Override
        void mergeHandlers(TryItem tryItem){
            clearCache();
        }

        private void clearCache() {
            this.mTypeHandlerList = null;
            this.mCatchAllHandler = null;
        }
    }
}
