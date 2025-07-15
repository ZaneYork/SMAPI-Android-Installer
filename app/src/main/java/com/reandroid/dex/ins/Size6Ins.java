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

public class Size6Ins extends SizeXIns {
    public Size6Ins(Opcode<?> opcode) {
        super(opcode);
    }
    @Override
    public int getData(){
        return getShortUnsigned(2);
    }
    public void setData(int data){
        setShort(2, data);
    }

    @Override
    public int getCodeUnits() {
        return 3;
    }
}