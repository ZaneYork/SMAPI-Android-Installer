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

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.AlignItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.data.MethodDef;
import com.reandroid.dex.debug.DebugElement;
import com.reandroid.utils.NumbersUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.IterableIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class InsBlockList extends BlockList<Ins> {

    private final AlignItem blockAlign;
    private final IntegerReference codeUnitsReference;
    private final IntegerReference outSizReference;
    private final Iterable<? extends ExtraLine> extraLines;

    private NullInstruction mNullInstruction;

    private boolean mLinked;
    private boolean mLocked;
    private boolean mSecondUpdateRequired;

    private Object mLockedBy;

    public InsBlockList(AlignItem blockAlign,
                        IntegerReference codeUnitsReference,
                        IntegerReference outSizReference,
                        Iterable<? extends ExtraLine> extraLines) {

        this.blockAlign = blockAlign;
        this.codeUnitsReference = codeUnitsReference;
        this.outSizReference = outSizReference;
        this.extraLines = extraLines;
    }

    public Ins getPrevious(Ins ins) {
        return get(indexOf(ins) - 1);
    }
    public Ins getNext(Ins ins) {
        int index = indexOf(ins);
        if(index >= 0) {
            return get(index + 1);
        }
        return null;
    }
    public Ins getAtAddress(int address){
        int size = size();
        int codeUnits = 0;
        for (int i = 0; i < size; i++) {
            Ins ins = get(i);
            if(codeUnits == address) {
                return ins;
            }
            codeUnits += ins.getCodeUnits();
        }
        if(address == codeUnits) {
            return getOrCreateNullInstruction();
        }
        return null;
    }
    public int addressOf(Ins instruction) {
        int count = size();
        int address = 0;
        for(int i = 0; i < count; i++) {
            Ins ins = get(i);
            if(ins == instruction) {
                return address;
            }
            address += ins.getCodeUnits();
        }
        if(instruction == getNullInstruction()) {
            return address;
        }
        return -1;
    }
    private Ins[] buildAddressMap() {
        int size = size();
        int address = 0;
        updateCodeUnits();
        Ins[] map = new Ins[getCodeUnits()];
        for(int i = 0; i < size; i++) {
            Ins ins = get(i);
            map[address] = ins;
            address += ins.getCodeUnits();
        }
        return map;
    }
    public Iterator<Label> getLabels() {
        return  new IterableIterator<Ins, Label>(iterator()) {
            @Override
            public Iterator<Label> iterator(Ins element) {
                Iterator<Label> iterator = null;
                if(element instanceof LabelsSet) {
                    iterator = ObjectsUtil.cast(((LabelsSet) element).getLabels());
                }
                if(element instanceof Label) {
                    Label label = (Label) element;
                    if(iterator == null) {
                        iterator = SingleIterator.of(label);
                    } else {
                        iterator = CombiningIterator.singleOne(label, iterator);
                    }
                }
                if(iterator == null) {
                    iterator = EmptyIterator.of();
                }
                return iterator;
            }
        };
    }

    public void onEditingInternal(InsBlockList insBlockList) {
        if(this.isLinked() && !insBlockList.isLinked()) {
            insBlockList.linkLocked();
            insBlockList.mLocked = false;
            insBlockList.mLockedBy = null;
        }
    }
    public boolean isLinked() {
        return mLinked;
    }
    public boolean isLocked() {
        return mLocked || mLockedBy != null;
    }
    public Object linkLocked() {
        return link(new Object());
    }
    public Object link(Object obj) {
        if(mLinked || isLocked()) {
            return null;
        }
        mLockedBy = obj;
        mLocked = true;
        linkTargetIns();
        linkExtraLines();
        mLinked = size() != 0;
        mLocked = false;
        return obj;
    }
    public void link() {
        if(mLinked || isLocked()) {
            return;
        }
        mLocked = true;
        linkTargetIns();
        linkExtraLines();
        mLinked = true;
        mLocked = false;
    }
    private void linkTargetIns() {
        mSecondUpdateRequired = false;
        int count = size();
        for(int i = 0; i < count; i++) {
            get(i).linkTargetIns();
        }
    }
    private void linkExtraLines() {
        Iterator<? extends ExtraLine> iterator = extraLines.iterator();
        if(!iterator.hasNext()) {
            return;
        }
        Ins[] map = buildAddressMap();
        int length = map.length;
        while (iterator.hasNext()) {
            ExtraLine extraLine = iterator.next();
            if(extraLine.isRemoved() ||
                    extraLine.getTargetIns() != null) {
                continue;
            }
            int address = extraLine.getTargetAddress();
            if(extraLine instanceof DebugElement) {
                if(address < 0 || address >= length || map[address] == null) {
                    continue;
                }
            }
            Ins target;
            if(address == length) {
                target = getOrCreateNullInstruction();
            } else {
                target = map[address];
            }
            if(target == null) {
                throw new NullPointerException("Invalid address " + address + ": " + extraLine);
            }
            extraLine.setTargetIns(target);
        }
    }

    public void unlinkLocked(Object obj) {
        unlink(obj, true);
    }
    public void unlink(Object obj) {
        unlink(obj, false);
    }
    public void unlink(Object obj, boolean update) {
        if(update) {
            mSecondUpdateRequired = true;
        }
        if(mLocked) {
            return;
        }
        if(obj == null || obj != mLockedBy) {
            return;
        }
        mLocked = true;
        if(!mLinked) {
            if(!update) {
                mLocked = false;
                mLockedBy = null;
                return;
            }
            linkTargetIns();
            linkExtraLines();
        }
        if(update || mSecondUpdateRequired) {
            update();
        }
        unlinkTargets();
        mLocked = false;
        mLockedBy = null;
    }
    public void unlink() {
        if(!mLinked || isLocked()) {
            mSecondUpdateRequired = true;
            return;
        }
        mLocked = true;
        update();
        unlinkTargets();
        updateCodeUnits();
        mLocked = false;
    }
    private void unlinkTargets() {
        int size = size();
        for(int i = 0; i < size; i++) {
            get(i).unLinkTargetIns();
        }
        removeNullInstruction();
        for (ExtraLine extraLine : this.extraLines) {
            extraLine.setTargetIns(null);
        }
        mLinked = false;
    }
    private void update() {
        mSecondUpdateRequired = false;
        updateInsTarget();
        updateExtraLines();
        if(mSecondUpdateRequired) {
            updateInsTarget();
            updateExtraLines();
            mSecondUpdateRequired = false;
        }
    }
    private void updateInsTarget() {
        int size = size();
        for(int i = 0; i < size; i++) {
            get(i).updateTargetAddress();
        }
    }
    private void updateExtraLines() {
        for (ExtraLine extraLine : extraLines) {
            extraLine.updateTarget();
        }
    }
    private String getCurrentMethodForDebug() {
        InstructionList instructionList = getParentInstance(InstructionList.class);
        if(instructionList != null) {
            MethodDef methodDef = instructionList.getCodeItem().getMethodDef();
            if(methodDef != null) {
                return methodDef.getKey().toString();
            }
        }
        return null;
    }
    public NullInstruction getNullInstruction() {
        NullInstruction nullInstruction = this.mNullInstruction;
        if(nullInstruction != null) {
            nullInstruction.setParent(this);
            nullInstruction.setIndex(getCount());
        }
        return nullInstruction;
    }
    public NullInstruction getOrCreateNullInstruction() {
        NullInstruction nullInstruction = this.mNullInstruction;
        if(nullInstruction == null) {
            nullInstruction = new NullInstruction();
            this.mNullInstruction = nullInstruction;
        }
        nullInstruction.setParent(this);
        nullInstruction.setIndex(getCount());
        return nullInstruction;
    }
    public void removeNullInstruction() {
        NullInstruction nullInstruction = this.mNullInstruction;
        if(nullInstruction != null) {
            this.mNullInstruction = null;
            nullInstruction.setParent(null);
            nullInstruction.setIndex(-1);
        }
    }
    public int getCodeUnits() {
        return codeUnitsReference.get();
    }
    public int getOutSize() {
        return outSizReference.get();
    }
    public void updateCodeUnits() {
        int address = 0;
        int outSize = 0;
        int count = size();
        for(int i = 0; i < count; i++) {
            Ins ins = get(i);
            address += ins.getCodeUnits();
            outSize = NumbersUtil.max(ins.getOutSize(), outSize);
        }
        codeUnitsReference.set(address);
        outSizReference.set(outSize);
        blockAlign.align(address * 2);
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        updateCodeUnits();
        mLocked = false;
        mSecondUpdateRequired = false;
        unlink();
        mLockedBy = null;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        mLockedBy = new Object();
        int insCodeUnits = codeUnitsReference.get();
        int position = reader.getPosition() + insCodeUnits * 2;
        int zeroPosition = reader.getPosition();

        int count = (insCodeUnits + 1) / 2;
        ensureCapacity(count);

        while (reader.getPosition() < position){
            Opcode<?> opcode = Opcode.read(reader);
            Ins ins = opcode.newInstance();
            add(ins);
            ins.readBytes(reader);
        }
        trimToSize();
        if(position != reader.getPosition()){
            // should not reach here
            reader.seek(position);
        }
        int totalRead = reader.getPosition() - zeroPosition;
        blockAlign.align(totalRead);
        reader.offset(blockAlign.size());
        mLocked = false;
        mLinked = false;
        mLockedBy = null;
    }
    public void merge(InsBlockList insBlockList){
        if(insBlockList == this) {
            return;
        }
        mLockedBy = new Object();
        mLocked = true;
        clearChildes();
        int size = insBlockList.size();
        ensureCapacity(size);
        for(int i = 0; i < size; i++) {
            Ins coming = insBlockList.get(i);
            Ins ins = coming.getOpcode().newInstance();
            add(ins);
            ins.merge(coming);
        }
        mLocked = false;
        mLinked = false;
        mLockedBy = null;
        updateCodeUnits();
    }
}
