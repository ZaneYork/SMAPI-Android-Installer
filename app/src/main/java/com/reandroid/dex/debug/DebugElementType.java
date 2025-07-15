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
package com.reandroid.dex.debug;

import com.reandroid.arsc.base.BlockCreator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.smali.SmaliDirective;

import java.io.IOException;

public class DebugElementType<T extends DebugElement> implements BlockCreator<T>{

    public static final DebugElementType<?>[] VALUES;

    public static final DebugElementType<DebugStartLocal> START_LOCAL;
    public static final DebugElementType<DebugEndLocal> END_LOCAL;
    public static final DebugElementType<DebugRestartLocal> RESTART_LOCAL;
    public static final DebugElementType<DebugPrologue> PROLOGUE;
    public static final DebugElementType<DebugEpilogue> EPILOGUE;
    public static final DebugElementType<DebugSetSourceFile> SET_SOURCE_FILE;
    public static final DebugElementType<DebugEndSequence> END_SEQUENCE;
    public static final DebugElementType<DebugAdvancePc> ADVANCE_PC;
    public static final DebugElementType<DebugAdvanceLine> ADVANCE_LINE;
    public static final DebugElementType<DebugStartLocalExtended> START_LOCAL_EXTENDED;
    public static final DebugElementType<DebugLineNumber> LINE_NUMBER;


    static {

        VALUES = new DebugElementType[0x0A + 1];

        END_SEQUENCE = new DebugElementType<>("END_SEQUENCE",
                0x00, () -> DebugEndSequence.INSTANCE);
        VALUES[0x00] = END_SEQUENCE;

        ADVANCE_PC = new DebugElementType<>("ADVANCE_PC",
                0x01, DebugAdvancePc::new);
        VALUES[0x01] = ADVANCE_PC;

        ADVANCE_LINE = new DebugElementType<>("ADVANCE_LINE",
                0x02, DebugAdvanceLine::new);
        VALUES[0x02] = ADVANCE_LINE;

        START_LOCAL = new DebugElementType<>("START_LOCAL", SmaliDirective.LOCAL,
                0x03, DebugStartLocal::new);
        VALUES[0x03] = START_LOCAL;

        START_LOCAL_EXTENDED = new DebugElementType<>("START_LOCAL_EXTENDED", SmaliDirective.LOCAL,
                0x04, DebugStartLocalExtended::new);
        VALUES[0x04] = START_LOCAL_EXTENDED;

        END_LOCAL = new DebugElementType<>("END_LOCAL", SmaliDirective.END_LOCAL,
                0x05, DebugEndLocal::new);
        VALUES[0x05] = END_LOCAL;

        RESTART_LOCAL = new DebugElementType<>("RESTART_LOCAL", SmaliDirective.RESTART_LOCAL,
                0x06, DebugRestartLocal::new);
        VALUES[0x06] = RESTART_LOCAL;

        PROLOGUE = new DebugElementType<>("PROLOGUE", SmaliDirective.PROLOGUE,
                0x07, DebugPrologue::new);
        VALUES[0x07] = PROLOGUE;

        EPILOGUE = new DebugElementType<>("EPILOGUE", SmaliDirective.EPILOGUE,
                0x08, DebugEpilogue::new);
        VALUES[0x08] = EPILOGUE;

        SET_SOURCE_FILE = new DebugElementType<>("SET_SOURCE_FILE", SmaliDirective.SET_SOURCE_FILE,
                0x09, DebugSetSourceFile::new);
        VALUES[0x09] = SET_SOURCE_FILE;

        LINE_NUMBER = new DebugElementType<>("LINE_NUMBER", SmaliDirective.LINE,
                0x0A, DebugLineNumber::new);
        VALUES[0x0A] = LINE_NUMBER;
    }

    private final String name;
    private final SmaliDirective directive;
    private final int flag;
    private final BlockCreator<T> creator;

    private DebugElementType(String name, SmaliDirective directive, int flag, BlockCreator<T> creator){
        this.name = name;
        this.directive = directive;
        this.flag = flag;
        this.creator = creator;
    }
    private DebugElementType(String name, int flag, BlockCreator<T> creator){
        this(name, null, flag, creator);
    }

    public String getName() {
        return name;
    }
    public SmaliDirective getSmaliDirective() {
        return directive;
    }
    public int getFlag() {
        return flag;
    }
    @Override
    public T newInstance() {
        return creator.newInstance();
    }

    public boolean is(DebugElementType<?> type){
        return type == this;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DebugElementType<?> debugElementType = (DebugElementType<?>) obj;
        return flag == debugElementType.flag;
    }
    @Override
    public int hashCode() {
        return flag;
    }
    @Override
    public String toString() {
        if(directive != null){
            return directive.toString();
        }
        return name;
    }

    public static DebugElementType<?> readFlag(BlockReader reader) throws IOException {
        int flag = reader.read();
        reader.offset(-1);
        return fromFlag(flag);
    }

    public static DebugElementType<?> fromFlag(int flag){
        flag = flag & 0xff;
        if(flag > 0x0A){
            flag = 0x0A;
        }
        return VALUES[flag];
    }
}
