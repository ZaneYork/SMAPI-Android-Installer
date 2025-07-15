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
package com.reandroid.dex.common;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.NumberIntegerReference;
import com.reandroid.dex.base.OffsetReceiver;
import com.reandroid.dex.base.PositionedItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SectionItemContainer extends SectionItem implements BlockRefresh,
        PositionedItem, OffsetSupplier, OffsetReceiver {

    private final Block[] mChildes;

    private IntegerReference mReference;

    public SectionItemContainer(int childesCount) {
        super(0);
        Block[] childes;
        if(childesCount == 0){
            childes = EMPTY;
        }else {
            childes = new Block[childesCount];
        }
        this.mChildes = childes;

    }

    @Override
    public int getIdx(){
        return getOffset();
    }
    @Override
    public IntegerReference getOffsetReference() {
        return mReference;
    }
    @Override
    public void setOffsetReference(IntegerReference reference) {
        this.mReference = reference;
    }
    @Override
    public void setPosition(int position) {
        IntegerReference reference = getOffsetReference();
        if(reference == null){
            reference = new NumberIntegerReference(position);
            setOffsetReference(reference);
        }else {
            reference.set(position);
        }
    }
    @Override
    public void removeLastAlign(){

    }
    public int getOffset(){
        IntegerReference reference = getOffsetReference();
        if(reference != null){
            return reference.get();
        }
        return 0;
    }

    @Override
    public int countBytes(){
        if(isNull()){
            return 0;
        }
        Block[] childes = getChildes();
        if(childes == null){
            return 0;
        }
        int result = 0;
        int max = childes.length;
        for(int i = 0; i < max; i++){
            Block item = childes[i];
            if(item != null){
                result += item.countBytes();
            }
        }
        return result;
    }
    @Override
    public byte[] getBytes(){
        if(isNull()){
            return null;
        }
        Block[] childes = getChildes();
        if(childes == null){
            return null;
        }
        byte[] results = null;
        int length = childes.length;
        for(int i = 0; i < length; i++){
            Block item = childes[i];
            if(item != null){
                results = addBytes(results, item.getBytes());
            }
        }
        return results;
    }
    @Override
    public int onWriteBytes(OutputStream stream) throws IOException {
        if(isNull()){
            return 0;
        }
        Block[] childes = getChildes();
        if(childes == null){
            return 0;
        }
        int result = 0;
        int length = childes.length;
        for(int i = 0; i < length; i++){
            Block item = childes[i];
            if(item != null){
                result += item.writeBytes(stream);
            }
        }
        return result;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        Block[] childes = getChildes();
        if(childes == null){
            return;
        }
        int length = childes.length;
        if(skipReading(this, reader)){
            return;
        }
        for(int i = 0; i < length; i++){
            Block block = childes[i];
            if(block == null){
                continue;
            }
            if(skipReading(block, reader)){
                continue;
            }
            block.readBytes(reader);
        }
    }
    protected void nonCheckRead(BlockReader reader) throws IOException {
        Block[] childes = getChildes();
        if(childes == null){
            return;
        }
        int length = childes.length;
        for(int i = 0; i < length; i++){
            Block block = childes[i];
            if(block != null){
                block.readBytes(reader);
            }
        }
    }
    private boolean skipReading(Block block, BlockReader reader){
        if(!(block instanceof OffsetSupplier)){
            return false;
        }
        OffsetSupplier offsetSupplier = (OffsetSupplier) block;
        IntegerReference reference = offsetSupplier.getOffsetReference();
        if(reference != null){
            int offset = reference.get();
            if(!isValidOffset(offset)){
                return true;
            }
            reader.seek(offset);
        }
        return false;
    }
    protected boolean isValidOffset(int offset){
        return offset > 0;
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        counter.setCurrent(this);
        if(counter.END==this){
            counter.FOUND=true;
            return;
        }
        Block[] childes = getChildes();
        if(childes == null){
            return;
        }
        int max = childes.length;
        for(int i = 0; i < max; i++){
            if(counter.FOUND){
                return;
            }
            Block item = childes[i];
            if(item != null){
                item.onCountUpTo(counter);
            }
        }
    }
    public void addChild(int index, Block block){
        mChildes[index] = block;
        if(block != null){
            block.setIndex(index);
            block.setParent(this);
        }
    }
    @Override
    public void refresh() {
        if(isNull()){
            return;
        }
        onPreRefresh();
        refreshChildes();
        onRefreshed();
    }
    protected void refreshChildes(){
        Block[] childes = getChildes();
        if(childes == null){
            return;
        }
        int length = childes.length;
        for(int i = 0; i < length; i++){
            Block item = childes[i];
            if(item instanceof BlockRefresh){
                ((BlockRefresh)item).refresh();
            }
        }
    }
    protected void onPreRefresh(){
    }
    protected void onRefreshed(){
    }
    public int getChildesCount() {
        return mChildes.length;
    }
    public Block[] getChildes() {
        return mChildes;
    }

    @Override
    protected final byte[] getBytesInternal() {
        throw new RuntimeException("Not block item");
    }
    @Override
    protected void onBytesChanged() {
        throw new RuntimeException("Not block item");
    }
    @Override
    public final int readBytes(InputStream inputStream) throws IOException {
        throw new RuntimeException("Not block item");
    }

    private static final Block[] EMPTY = new Block[0];

}
