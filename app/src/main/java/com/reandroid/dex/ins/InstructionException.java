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

import com.reandroid.dex.base.DexException;
import com.reandroid.dex.id.MethodId;
import com.reandroid.dex.data.CodeItem;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.data.MethodDef;
import com.reandroid.dex.sections.DexLayout;

public class InstructionException extends DexException {

    private final Ins ins;

    public InstructionException(String message, Ins ins){
        super(message);
        this.ins = ins;
    }

    public Ins getIns() {
        return ins;
    }
    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.getMessage());
        builder.append(", ");
        appendDex(builder);
        if(!appendMethod(builder)){
            builder.append(getIns());
        }
        return builder.toString();
    }

    private boolean appendMethod(StringBuilder builder){
        InstructionList instructionList = getIns().getInstructionList();
        if(instructionList == null){
            return false;
        }
        CodeItem codeItem = instructionList.getCodeItem();
        MethodDef methodDef = codeItem.getMethodDef();
        if(methodDef == null){
            return false;
        }
        MethodId methodId = methodDef.getId();
        if(methodId == null){
            return false;
        }
        builder.append("class = ");
        builder.append(methodId.getDefining());
        builder.append(", method = ");
        builder.append(methodId.getName());
        builder.append(methodId.getProto());
        builder.append(" { ... ");
        builder.append(getIns());
        builder.append(" ... }");
        return true;
    }
    private void appendDex(StringBuilder builder){
        DexLayout dexLayout = getIns().getParentInstance(DexLayout.class);
        if(dexLayout == null){
            return;
        }
        String simpleName = dexLayout.getSimpleName();
        if(simpleName == null){
            return;
        }
        builder.append("dex = ");
        builder.append(simpleName);
        builder.append(", ");
    }

}
