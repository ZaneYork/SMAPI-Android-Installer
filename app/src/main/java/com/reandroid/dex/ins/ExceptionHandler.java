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

import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.data.FixedDexContainerWithTool;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliCodeExceptionHandler;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.collection.ArrayIterator;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public abstract class ExceptionHandler extends FixedDexContainerWithTool
        implements SmaliRegion, Iterable<Label>, LabelsSet {

    private final Ule128Item catchAddress;

    private final ExceptionLabel startLabel;
    private final ExceptionLabel endLabel;
    private final ExceptionLabel handlerLabel;
    private final ExceptionLabel catchLabel;

    private Label[] mLabels;


    private ExceptionHandler(int childesCount, Ule128Item catchAddress, int index) {
        super(childesCount);
        this.catchAddress = catchAddress;
        if(catchAddress != null){
            addChild(index, catchAddress);
        }

        this.startLabel = new TryStartLabel(this);
        this.endLabel = new TryEndLabel(this);
        this.handlerLabel = new HandlerLabel(this);
        this.catchLabel = new CatchLabel(this);

        this.mLabels = new Label[]{this.startLabel, this.endLabel, this.handlerLabel, this.catchLabel};
    }
    ExceptionHandler(int childesCount) {
        this(childesCount + 1, new Ule128Item(), childesCount);
    }

    ExceptionHandler() {
        this(0, null, 0);
    }


    public TypeKey getKey(){
        return null;
    }
    public void setKey(TypeKey typeKey){
    }
    public Iterator<Ins> getTryInstructions(){
        InstructionList instructionList = getInstructionList();
        if(instructionList == null){
            return EmptyIterator.of();
        }
        return instructionList.iteratorByAddress(
                getStartLabel().getTargetAddress(), getCodeUnit());
    }
    private InstructionList getInstructionList(){
        TryItem tryItem = getTryItem();
        if(tryItem != null){
            return tryItem.getInstructionList();
        }
        return null;
    }

    abstract TypeId getTypeId();
    public abstract SmaliDirective getSmaliDirective();
    Ule128Item getCatchAddressUle128(){
        return catchAddress;
    }

    @Override
    public Iterator<Label> getLabels(){
        return iterator();
    }
    @Override
    public Iterator<Label> iterator(){
        return ArrayIterator.of(mLabels);
    }
    public ExceptionLabel getHandlerLabel(){
        return handlerLabel;
    }
    public ExceptionLabel getStartLabel(){
        return startLabel;
    }
    public ExceptionLabel getEndLabel(){
        return endLabel;
    }
    public ExceptionLabel getCatchLabel(){
        return catchLabel;
    }

    public void refreshAddresses() {
        Ins handlerIns = getHandlerLabel().getTargetIns();
        Ins startIns = getStartLabel().getTargetIns();
        Ins endIns = getEndLabel().getTargetIns();
        Ins catchIns = getCatchLabel().getTargetIns();

        if(handlerIns != null && startIns != null && endIns != null && catchIns != null) {

            int handlerAddress = handlerIns.getAddress();
            int startAddress = startIns.getAddress();
            int endAddress = endIns.getAddress();
            int catchAddress = catchIns.getAddress();

            setStartAddress(startIns.getAddress());
            setCatchAddress(catchAddress);
            setCodeUnit(handlerAddress - startAddress);
        }
    }

    public int getCatchAddress(){
        return getCatchAddressUle128().get();
    }
    public void setCatchAddress(int address){
        getCatchAddressUle128().set(address);
    }
    public int getAddress(){
        return getStartAddress() + getCodeUnit();
    }
    public void setAddress(int address){
        setCodeUnit(address - getStartAddress());
    }

    public int getStartAddress(){
        TryItem tryItem = getTryItem();
        if(tryItem != null){
            return tryItem.getStartAddress();
        }
        return 0;
    }
    public void setStartAddress(int address){
        TryItem tryItem = getTryItem();
        if(tryItem != null){
            tryItem.setStartAddress(address);
        }
    }
    public int getCodeUnit(){
        TryItem tryItem = getTryItem();
        if(tryItem != null){
            return tryItem.getCatchCodeUnit();
        }
        return 0;
    }
    public void setCodeUnit(int value){
        TryItem tryItem = getTryItem();
        if(tryItem != null){
            tryItem.getHandlerOffset().setCatchCodeUnit(value);
        }
    }
    TryItem getTryItem(){
        return getParentInstance(TryItem.class);
    }

    public void onRemove(){
        mLabels = null;
        setParent(null);
    }
    public void removeSelf(){
        TryItem tryItem = getTryItem();
        if(tryItem != null){
            tryItem.remove(this);
        }
    }
    public boolean isRemoved() {
        return getParent() == null;
    }
    public void merge(ExceptionHandler handler){
        catchAddress.set(handler.catchAddress.get());
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {

    }
    public void fromSmali(SmaliCodeExceptionHandler smaliCodeExceptionHandler){
        getHandlerLabel().setTargetAddress(smaliCodeExceptionHandler.getAddress());
        getStartLabel().setTargetAddress(smaliCodeExceptionHandler.getStart().getAddress());
        getEndLabel().setTargetAddress(smaliCodeExceptionHandler.getEnd().getAddress());
        getCatchLabel().setTargetAddress(smaliCodeExceptionHandler.getCatchLabel().getAddress());
    }

    boolean isTypeEqual(ExceptionHandler handler){
        return true;
    }
    int getTypeHashCode(){
        return 0;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ExceptionHandler handler = (ExceptionHandler) obj;
        return getStartAddress() == handler.getStartAddress() &&
                getAddress() == handler.getAddress() &&
                getCatchAddress() == handler.getCatchAddress() &&
                isTypeEqual(handler);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + getStartAddress();
        hash = hash * 31 + getAddress();
        hash = hash * 31 + getCatchAddress();
        hash = hash * 31 + getTypeHashCode();
        return hash;
    }

    @Override
    public String toString() {
        return getHandlerLabel().toString();
    }

    static abstract class AbstractExceptionLabel implements ExceptionLabel {

        private Ins targetIns;
        private final ExceptionHandler handler;

        AbstractExceptionLabel(ExceptionHandler handler) {
            this.handler = handler;
        }

        @Override
        public ExceptionHandler getHandler() {
            return handler;
        }
        @Override
        public Ins getTargetIns() {
            Ins ins = this.targetIns;
            if(ins != null && ins.isRemoved()) {
                ins = null;
                this.targetIns = null;
            }
            return ins;
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
        public void updateTarget() {
            // update on handler is enough
        }
    }
    public static class HandlerLabel extends AbstractExceptionLabel {

        HandlerLabel(ExceptionHandler handler){
            super(handler);
        }

        @Override
        public int getAddress(){
            return getHandler().getAddress();
        }
        @Override
        public int getTargetAddress() {
            return getHandler().getAddress();
        }
        @Override
        public void setTargetAddress(int targetAddress){
            getHandler().setAddress(targetAddress);
        }
        @Override
        public void updateTarget() {
            getHandler().refreshAddresses();
        }

        @Override
        public String getLabelName() {
            ExceptionHandler handler = this.getHandler();
            StringBuilder builder = new StringBuilder();
            builder.append('.');
            builder.append(handler.getSmaliDirective().getName());
            builder.append(' ');
            TypeId typeId = handler.getTypeId();
            if(typeId != null){
                builder.append(typeId.getName());
                builder.append(' ');
            }
            builder.append("{");
            builder.append(handler.getStartLabel().getLabelName());
            builder.append(" .. ");
            builder.append(handler.getEndLabel().getLabelName());
            builder.append("} ");
            builder.append(handler.getCatchLabel().getLabelName());
            return builder.toString();
        }

        @Override
        public int getSortOrder() {
            return ExtraLine.ORDER_EXCEPTION_HANDLER;
        }
        @Override
        public boolean isEqualExtraLine(Object obj) {
            if(obj == this){
                return true;
            }
            if(obj == null || this.getClass() != obj.getClass()){
                return false;
            }
            HandlerLabel label = (HandlerLabel) obj;
            return this.getHandler() == label.getHandler();
        }
        @Override
        public void appendExtra(SmaliWriter writer) throws IOException {
            ExceptionHandler handler = this.getHandler();
            handler.getSmaliDirective().append(writer);
            TypeId typeId = handler.getTypeId();
            if(typeId != null){
                typeId.append(writer);
                writer.append(' ');
            }
            writer.append("{");
            writer.appendLabelName(handler.getStartLabel().getLabelName());
            writer.append(" .. ");
            writer.appendLabelName(handler.getEndLabel().getLabelName());
            writer.append("} ");
            writer.appendLabelName(handler.getCatchLabel().getLabelName());
        }
        @Override
        public String toString() {
            return getLabelName();
        }
    }

    public static class TryStartLabel extends AbstractExceptionLabel {

        TryStartLabel(ExceptionHandler handler){
            super(handler);
        }

        @Override
        public int getAddress(){
            return getHandler().getAddress();
        }
        @Override
        public int getTargetAddress() {
            return getHandler().getStartAddress();
        }
        @Override
        public void setTargetAddress(int targetAddress){
            getHandler().setStartAddress(targetAddress);
        }
        @Override
        public String getLabelName() {
            return HexUtil.toHex(":try_start_", getTargetAddress(), 1);
        }

        @Override
        public int getSortOrder() {
            return ExtraLine.ORDER_TRY_START;
        }

        @Override
        public boolean isEqualExtraLine(Object obj) {
            if(obj == this){
                return true;
            }
            if(obj == null || this.getClass() != obj.getClass()){
                return false;
            }
            TryStartLabel label = (TryStartLabel) obj;
            if(this.getHandler() == label.getHandler()){
                return true;
            }
            return getTargetAddress() == label.getTargetAddress();
        }
        @Override
        public String toString() {
            return getLabelName();
        }
    }

    public static class TryEndLabel extends AbstractExceptionLabel {

        TryEndLabel(ExceptionHandler handler){
            super(handler);
        }

        @Override
        public int getAddress() {
            return getHandler().getAddress();
        }
        @Override
        public int getTargetAddress() {
            return getHandler().getAddress();
        }
        @Override
        public void setTargetAddress(int targetAddress){
            getHandler().setAddress(targetAddress);
        }
        @Override
        public String getLabelName() {
            int startAddress = getHandler().getStartLabel().getTargetAddress();
            return HexUtil.toHex(":try_end_", startAddress, 1);
        }

        @Override
        public int getSortOrder() {
            return ExtraLine.ORDER_TRY_END;
        }
        @Override
        public boolean isEqualExtraLine(Object obj) {
            if(obj == this){
                return true;
            }
            if(obj == null || this.getClass() != obj.getClass()){
                return false;
            }
            TryEndLabel label = (TryEndLabel) obj;
            if(this.getHandler() == label.getHandler()){
                return true;
            }
            return getTargetAddress() == label.getTargetAddress();
        }
        @Override
        public String toString() {
            return getLabelName();
        }
    }

    public static class CatchLabel extends AbstractExceptionLabel {

        CatchLabel(ExceptionHandler handler) {
            super(handler);
        }

        @Override
        public int getAddress() {
            return getHandler().getStartLabel().getAddress();
        }
        @Override
        public int getTargetAddress() {
            return getHandler().getCatchAddress();
        }
        @Override
        public void setTargetAddress(int targetAddress){
            getHandler().setCatchAddress(targetAddress);
        }
        @Override
        public String getLabelName() {
            return HexUtil.toHex(":" + getHandler().getSmaliDirective().getName() + "_", getTargetAddress(), 1);
        }
        @Override
        public int getSortOrder() {
            return ExtraLine.ORDER_CATCH;
        }
        @Override
        public boolean isEqualExtraLine(Object obj) {
            if(obj == this){
                return true;
            }
            if(obj == null || this.getClass() != obj.getClass()){
                return false;
            }
            CatchLabel label = (CatchLabel) obj;
            if(this.getHandler() == label.getHandler()){
                return true;
            }
            return getTargetAddress() == label.getTargetAddress() &&
                    getHandler().getSmaliDirective().equals(label.getHandler().getSmaliDirective());
        }
        @Override
        public String toString() {
            return getLabelName();
        }
    }
}
