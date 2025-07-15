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

import com.reandroid.dex.data.InstructionList;

import java.util.Iterator;

public abstract class InsSwitch extends Ins31t {

    private InsSwitchPayload switchPayload;

    public InsSwitch(Opcode<?> opcode) {
        super(opcode);
    }

    public InsSwitchPayload getPayload() {
        InsSwitchPayload switchPayload = this.switchPayload;
        if(switchPayload == null) {
            switchPayload = findByAddress();
            if(switchPayload != null) {
                this.switchPayload = switchPayload;
                switchPayload.setSwitch(this);
            }
        }
        return switchPayload;
    }
    public void setPayload(InsSwitchPayload switchPayload) {
        this.switchPayload = switchPayload;
        setTargetAddress(switchPayload.getAddress());
    }

    private InsSwitchPayload findByAddress() {
        InstructionList instructionList = getInstructionList();
        if(instructionList != null){
            Iterator<? extends InsSwitchPayload> iterator = instructionList
                    .iterator(getPayloadOpcode());
            int address = getTargetAddress();
            while (iterator.hasNext()){
                InsSwitchPayload switchPayload = iterator.next();
                if(switchPayload.getAddress() == address){
                    return switchPayload;
                }
            }
        }
        return null;
    }
    public abstract Opcode<? extends InsSwitchPayload> getPayloadOpcode();
}
