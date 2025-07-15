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

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;
import java.io.OutputStream;

public class NullInstruction extends InsNop {

    public NullInstruction() {
        super();
    }

    @Override
    public boolean isNull() {
        return true;
    }
    @Override
    public int countBytes() {
        return 0;
    }
    @Override
    public int getCodeUnits() {
        return 0;
    }
    @Override
    public int onWriteBytes(OutputStream stream) throws IOException {
        return super.onWriteBytes(stream);
    }
    @Override
    public void appendCode(SmaliWriter writer) throws IOException {
    }
}
