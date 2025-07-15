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

import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.*;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;
import java.util.Iterator;

public class InsPackedSwitchData extends InsSwitchPayload {

    private final ShortItem elementCount;
    private final IntegerItem firstKey;
    private final PackedSwitchDataList elements;

    private InsSparseSwitchData mReplacement;

    public InsPackedSwitchData() {
        super(3, Opcode.PACKED_SWITCH_PAYLOAD);
        this.elementCount = new ShortItem();
        this.firstKey = new IntegerItem();
        this.elements = new PackedSwitchDataList(this, elementCount);

        addChild(1, elementCount);
        addChild(2, firstKey);
        addChild(3, elements);
    }

    public int getFirstKey() {
        return firstKey.get();
    }
    public void setFirstKey(int firstKey){
        this.firstKey.set(firstKey);
    }

    @Override
    public Iterator<SwitchEntry> iterator() {
        return ObjectsUtil.cast(elements.getLabels());
    }


    void onDataChange(int index, int value) {
        replaceBySparse().get(index).set(value);
    }
    public InsSparseSwitchData replaceBySparse() {
        InsSparseSwitchData sparseData = this.mReplacement;
        if(sparseData != null) {
            return sparseData;
        }
        InsBlockList insBlockList = getInsBlockList();
        Object lock = insBlockList.linkLocked();

        InsPackedSwitch packed = getSwitch();
        InsSparseSwitch sparse = packed.getSparseSwitchReplacement();

        sparseData = Opcode.SPARSE_SWITCH_PAYLOAD.newInstance();
        this.mReplacement = sparseData;

        sparseData.setSwitch(sparse);
        sparseData.fromPackedSwitchData(this.elements);

        this.replace(sparseData);

        insBlockList.unlinkLocked(lock);

        return sparseData;
    }

    @Override
    public Opcode<InsPackedSwitch> getSwitchOpcode() {
        return Opcode.PACKED_SWITCH;
    }

    @Override
    public InsPackedSwitch getSwitch() {
        return (InsPackedSwitch) super.getSwitch();
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.PACKED_SWITCH;
    }

    @Override
    public Iterator<PackedSwitchDataList.PackedSwitchEntry> getLabels() {
        return elements.getLabels();
    }

    @Override
    public void merge(Ins ins){
        InsPackedSwitchData switchData = (InsPackedSwitchData) ins;
        setFirstKey(switchData.getFirstKey());
        elements.merge(switchData.elements);
    }

    @Override
    public void fromSmali(SmaliInstruction smaliInstruction) throws IOException {
        validateOpcode(smaliInstruction);
        SmaliPayloadPackedSwitch smaliPayloadPackedSwitch = (SmaliPayloadPackedSwitch) smaliInstruction;
        setFirstKey(smaliPayloadPackedSwitch.getFirstKey());
        SmaliSet<SmaliPackedSwitchEntry> entries = smaliPayloadPackedSwitch.getEntries();
        int size = entries.size();
        PackedSwitchDataList dataList = this.elements;
        dataList.setSize(size);
        for(int i = 0; i < size; i++) {
            SmaliPackedSwitchEntry smaliSwitchEntry = entries.get(i);
            PackedSwitchDataList.PackedSwitchEntry data = dataList.get(i);
            data.setAddress(smaliSwitchEntry.getRelativeOffset());
        }
    }

    @Override
    public void appendCode(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        writer.append(HexUtil.toHex(firstKey.get(), 1));
        writer.indentPlus();
        elements.append(writer);
        writer.indentMinus();
        getSmaliDirective().appendEnd(writer);
    }

    @Override
    public String toString() {
        return "InsPackedSwitchData{" +
                "elementCount=" + elementCount +
                ", firstKey=" + firstKey +
                ", elements=" + elements +
                '}';
    }
}