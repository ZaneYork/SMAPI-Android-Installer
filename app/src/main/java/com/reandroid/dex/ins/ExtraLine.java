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

import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;
import java.util.Comparator;

public interface ExtraLine {
    int getTargetAddress();
    void setTargetAddress(int targetAddress);
    Ins getTargetIns();
    void setTargetIns(Ins ins);
    void appendExtra(SmaliWriter writer) throws IOException;
    boolean isEqualExtraLine(Object obj);
    int getSortOrder();
    boolean isRemoved();
    default void updateTarget() {
        Ins ins = getTargetIns();
        if(ins != null && !ins.isRemoved()) {
            setTargetAddress(ins.getAddress());
        }
    }
    default int getSortOrderFine(){
        return 0;
    }
    default int compareExtraLine(ExtraLine other){
        int order1 = this.getSortOrder();
        int order2 = other.getSortOrder();
        if(order1 < order2){
            return -1;
        }
        if(order2 < order1){
            return 1;
        }
        order1 = this.getSortOrderFine();
        order2 = other.getSortOrderFine();
        if(order1 < order2){
            return -1;
        }
        if(order2 < order1){
            return 1;
        }
        return 0;
    }

    Comparator<ExtraLine> COMPARATOR = ExtraLine::compareExtraLine;

    int ORDER_DEBUG_LINE_NUMBER = 0;
    int ORDER_DEBUG_LINE = 1;
    int ORDER_TRY_END = 2;
    int ORDER_EXCEPTION_HANDLER = 3;
    int ORDER_CATCH = 4;
    int ORDER_INSTRUCTION_LABEL = 5;
    int ORDER_TRY_START = 6;
}
