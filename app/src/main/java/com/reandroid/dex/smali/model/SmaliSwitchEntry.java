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
package com.reandroid.dex.smali.model;

import com.reandroid.utils.ObjectsUtil;

public abstract class SmaliSwitchEntry extends Smali {

    private final SmaliLabel label;

    public SmaliSwitchEntry() {
        super();
        this.label = new SmaliLabel();
        this.label.setParent(this);
    }

    public SmaliLabel getLabel() {
        return label;
    }


    public Integer getRelativeOffset() {
        SmaliInstruction switchInstruction = getSwitch();
        if(switchInstruction != null) {
            SmaliInstruction target = getTargetInstruction();
            if(target != null) {
                return target.getAddress() - switchInstruction.getAddress();
            }
        }
        return null;
    }
    public SmaliInstruction getTargetInstruction() {
        return getLabel().getTargetInstruction();
    }
    public SmaliInstruction getSwitch() {
        SmaliSwitchPayload<?> payload = getPayload();
        if(payload != null) {
            return payload.getSwitch();
        }
        return null;
    }
    public SmaliSwitchPayload<?> getPayload() {
        return ObjectsUtil.cast(getParentInstance(SmaliSwitchPayload.class));
    }
}
