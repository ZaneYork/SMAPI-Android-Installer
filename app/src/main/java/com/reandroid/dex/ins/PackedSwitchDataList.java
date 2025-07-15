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
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.CountedList;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class PackedSwitchDataList extends CountedList<PackedSwitchDataList.PackedSwitchEntry>
        implements SmaliFormat, LabelsSet {

    private final InsPackedSwitchData switchData;

    public PackedSwitchDataList(InsPackedSwitchData switchData, IntegerReference itemCount){
        super(itemCount, CREATOR);
        this.switchData = switchData;
    }

    public int getFirstKey(){
        return switchData.getFirstKey();
    }
    public int getBaseAddress(){
        InsPackedSwitch packedSwitch = switchData.getSwitch();
        if(packedSwitch == null){
            return 0;
        }
        return packedSwitch.getAddress();
    }
    public InsPackedSwitchData getSwitchData() {
        return switchData;
    }
    void onDataChange(int index, int value) {
        this.switchData.onDataChange(index, value);
    }

    public void merge(PackedSwitchDataList dataList){
        int size = dataList.size();
        setSize(size);
        for(int i = 0; i < size; i++){
            get(i).merge(dataList.get(i));
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        int size = size();
        for(int i = 0; i < size; i++){
            get(i).append(writer);
        }
    }

    @Override
    public Iterator<PackedSwitchEntry> getLabels() {
        return iterator();
    }

    public static class PackedSwitchEntry extends IntegerItem implements InsSwitchPayload.SwitchEntry{

        private Ins targetIns;

        public PackedSwitchEntry(){
            super();
        }

        @Override
        public Ins getTargetIns() {
            Ins targetIns = this.targetIns;
            if(targetIns == null) {
                setTargetIns(findTargetIns());
                targetIns = this.targetIns;
            }
            return targetIns;
        }
        @Override
        public void setTargetIns(Ins targetIns) {
            if(targetIns != this.targetIns) {
                this.targetIns = targetIns;
                if(targetIns != null) {
                    targetIns.addExtraLine(this);
                }
            }
        }
        @Override
        public InsPackedSwitchData getPayload() {
            return getParentDataList().getSwitchData();
        }
        @Override
        public int get() {
            return getParentDataList().getFirstKey() + getIndex();
        }

        @Override
        public void set(int value) {
            if(value != this.get()) {
                getParentDataList().onDataChange(getIndex(), value);
            }
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.newLine();
            writer.appendLabelName(getLabelName());
            int value = this.get();
            writer.appendComment(HexUtil.toHex(value, 1));
            writer.appendResourceIdComment(value);
        }
        @Override
        public void appendExtra(SmaliWriter writer) throws IOException {
            writer.appendLabelName(getLabelName());
            writer.appendComment(HexUtil.toHex(get(), 1));
        }

        @Override
        public int getAddress() {
            return super.get();
        }
        public void setAddress(int address) {
            super.set(address);
        }
        @Override
        public int getTargetAddress() {
            return getParentDataList().getBaseAddress() + getAddress();
        }
        @Override
        public void setTargetAddress(int targetAddress){
            setAddress(targetAddress - getParentDataList().getBaseAddress());
        }
        @Override
        public String getLabelName() {
            return HexUtil.toHex(":pswitch_", getTargetAddress(), 1);
        }
        private PackedSwitchDataList getParentDataList() {
            return (PackedSwitchDataList) getParent();
        }
        @Override
        public int getSortOrder() {
            return ExtraLine.ORDER_INSTRUCTION_LABEL;
        }
        public void merge(PackedSwitchEntry data) {
            setAddress(data.getAddress());
        }
        @Override
        public int hashCode() {
            return Objects.hash(getIndex());
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            PackedSwitchEntry data = (PackedSwitchEntry) obj;
            return getIndex() == data.getIndex() && getParent() == data.getParent();
        }

        @Override
        public String toString() {
            return getLabelName();
        }
    }

    private static final Creator<PackedSwitchEntry> CREATOR = new Creator<PackedSwitchEntry>() {
        @Override
        public PackedSwitchEntry[] newArrayInstance(int length) {
            return new PackedSwitchEntry[length];
        }
        @Override
        public PackedSwitchEntry newInstance() {
            return new PackedSwitchEntry();
        }
    };
}
