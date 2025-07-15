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

import com.reandroid.arsc.base.BlockCreator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.common.OperandType;
import com.reandroid.dex.common.RegisterFormat;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.ArrayIterator;
import com.reandroid.utils.collection.CombiningIterator;

import java.io.IOException;
import java.util.*;

public class Opcode<T extends Ins> implements BlockCreator<T>, SmaliFormat {

    private static final Opcode<?>[] VALUES;
    private static final Opcode<?>[] PAYLOADS;
    private static final Opcode<?>[] VALUES_2;
    private static final Opcode<?>[] VALUES_3;
    private static final Map<String, Opcode<?>> NAME_MAP;


    public static final Opcode<InsNop> NOP;
    public static final Opcode<Ins12x> MOVE;
    public static final Opcode<Ins22x> MOVE_FROM16;
    public static final Opcode<Ins32x> MOVE_16;
    public static final Opcode<Ins12x> MOVE_WIDE;
    public static final Opcode<Ins22x> MOVE_WIDE_FROM16;
    public static final Opcode<Ins32x> MOVE_WIDE_16;
    public static final Opcode<Ins12x> MOVE_OBJECT;
    public static final Opcode<Ins22x> MOVE_OBJECT_FROM16;
    public static final Opcode<Ins32x> MOVE_OBJECT_16;
    public static final Opcode<Ins11x> MOVE_RESULT;
    public static final Opcode<Ins11x> MOVE_RESULT_WIDE;
    public static final Opcode<Ins11x> MOVE_RESULT_OBJECT;
    public static final Opcode<Ins11x> MOVE_EXCEPTION;
    public static final Opcode<Ins10x> RETURN_VOID;
    public static final Opcode<Ins11x> RETURN;
    public static final Opcode<Ins11x> RETURN_WIDE;
    public static final Opcode<Ins11x> RETURN_OBJECT;
    public static final Opcode<InsConst4> CONST_4;
    public static final Opcode<InsConst16> CONST_16;
    public static final Opcode<InsConst> CONST;
    public static final Opcode<InsConst16High> CONST_HIGH16;
    public static final Opcode<InsConstWide16> CONST_WIDE_16;
    public static final Opcode<InsConstWide32> CONST_WIDE_32;
    public static final Opcode<InsConstWide> CONST_WIDE;
    public static final Opcode<InsConstWideHigh16> CONST_WIDE_HIGH16;
    public static final Opcode<InsConstString> CONST_STRING;
    public static final Opcode<InsConstStringJumbo> CONST_STRING_JUMBO;
    public static final Opcode<Ins21c> CONST_CLASS;
    public static final Opcode<Ins11x> MONITOR_ENTER;
    public static final Opcode<Ins11x> MONITOR_EXIT;
    public static final Opcode<Ins21c> CHECK_CAST;
    public static final Opcode<Ins22c> INSTANCE_OF;
    public static final Opcode<Ins12x> ARRAY_LENGTH;
    public static final Opcode<Ins21c> NEW_INSTANCE;
    public static final Opcode<Ins22c> NEW_ARRAY;
    public static final Opcode<Ins35c> FILLED_NEW_ARRAY;
    public static final Opcode<Ins3rc> FILLED_NEW_ARRAY_RANGE;
    public static final Opcode<InsFillArrayData> FILL_ARRAY_DATA;
    public static final Opcode<Ins11x> THROW;
    public static final Opcode<InsGoto> GOTO;
    public static final Opcode<InsGoto> GOTO_16;
    public static final Opcode<InsGoto> GOTO_32;
    public static final Opcode<InsPackedSwitch> PACKED_SWITCH;
    public static final Opcode<InsSparseSwitch> SPARSE_SWITCH;
    public static final Opcode<Ins23x> CMPL_FLOAT;
    public static final Opcode<Ins23x> CMPG_FLOAT;
    public static final Opcode<Ins23x> CMPL_DOUBLE;
    public static final Opcode<Ins23x> CMPG_DOUBLE;
    public static final Opcode<Ins23x> CMP_LONG;
    public static final Opcode<Ins22t> IF_EQ;
    public static final Opcode<Ins22t> IF_NE;
    public static final Opcode<Ins22t> IF_LT;
    public static final Opcode<Ins22t> IF_GE;
    public static final Opcode<Ins22t> IF_GT;
    public static final Opcode<Ins22t> IF_LE;
    public static final Opcode<Ins21t> IF_EQZ;
    public static final Opcode<Ins21t> IF_NEZ;
    public static final Opcode<Ins21t> IF_LTZ;
    public static final Opcode<Ins21t> IF_GEZ;
    public static final Opcode<Ins21t> IF_GTZ;
    public static final Opcode<Ins21t> IF_LEZ;
    public static final Opcode<Ins23x> AGET;
    public static final Opcode<Ins23x> AGET_WIDE;
    public static final Opcode<Ins23x> AGET_OBJECT;
    public static final Opcode<Ins23x> AGET_BOOLEAN;
    public static final Opcode<Ins23x> AGET_BYTE;
    public static final Opcode<Ins23x> AGET_CHAR;
    public static final Opcode<Ins23x> AGET_SHORT;
    public static final Opcode<Ins23x> APUT;
    public static final Opcode<Ins23x> APUT_WIDE;
    public static final Opcode<Ins23x> APUT_OBJECT;
    public static final Opcode<Ins23x> APUT_BOOLEAN;
    public static final Opcode<Ins23x> APUT_BYTE;
    public static final Opcode<Ins23x> APUT_CHAR;
    public static final Opcode<Ins23x> APUT_SHORT;
    public static final Opcode<Ins22c> IGET;
    public static final Opcode<Ins22c> IGET_WIDE;
    public static final Opcode<Ins22c> IGET_OBJECT;
    public static final Opcode<Ins22c> IGET_BOOLEAN;
    public static final Opcode<Ins22c> IGET_BYTE;
    public static final Opcode<Ins22c> IGET_CHAR;
    public static final Opcode<Ins22c> IGET_SHORT;
    public static final Opcode<Ins22c> IPUT;
    public static final Opcode<Ins22c> IPUT_WIDE;
    public static final Opcode<Ins22c> IPUT_OBJECT;
    public static final Opcode<Ins22c> IPUT_BOOLEAN;
    public static final Opcode<Ins22c> IPUT_BYTE;
    public static final Opcode<Ins22c> IPUT_CHAR;
    public static final Opcode<Ins22c> IPUT_SHORT;
    public static final Opcode<Ins21c> SGET;
    public static final Opcode<Ins21c> SGET_WIDE;
    public static final Opcode<Ins21c> SGET_OBJECT;
    public static final Opcode<Ins21c> SGET_BOOLEAN;
    public static final Opcode<Ins21c> SGET_BYTE;
    public static final Opcode<Ins21c> SGET_CHAR;
    public static final Opcode<Ins21c> SGET_SHORT;
    public static final Opcode<Ins21c> SPUT;
    public static final Opcode<Ins21c> SPUT_WIDE;
    public static final Opcode<Ins21c> SPUT_OBJECT;
    public static final Opcode<Ins21c> SPUT_BOOLEAN;
    public static final Opcode<Ins21c> SPUT_BYTE;
    public static final Opcode<Ins21c> SPUT_CHAR;
    public static final Opcode<Ins21c> SPUT_SHORT;
    public static final Opcode<Ins35c> INVOKE_VIRTUAL;
    public static final Opcode<Ins35c> INVOKE_SUPER;
    public static final Opcode<Ins35c> INVOKE_DIRECT;
    public static final Opcode<Ins35c> INVOKE_STATIC;
    public static final Opcode<Ins35c> INVOKE_INTERFACE;
    public static final Opcode<Ins10x> RETURN_VOID_NO_BARRIER;
    public static final Opcode<Ins3rc> INVOKE_VIRTUAL_RANGE;
    public static final Opcode<Ins3rc> INVOKE_SUPER_RANGE;
    public static final Opcode<Ins3rc> INVOKE_DIRECT_RANGE;
    public static final Opcode<Ins3rc> INVOKE_STATIC_RANGE;
    public static final Opcode<Ins3rc> INVOKE_INTERFACE_RANGE;
    public static final Opcode<Ins12x> NEG_INT;
    public static final Opcode<Ins12x> NOT_INT;
    public static final Opcode<Ins12x> NEG_LONG;
    public static final Opcode<Ins12x> NOT_LONG;
    public static final Opcode<Ins12x> NEG_FLOAT;
    public static final Opcode<Ins12x> NEG_DOUBLE;
    public static final Opcode<Ins12x> INT_TO_LONG;
    public static final Opcode<Ins12x> INT_TO_FLOAT;
    public static final Opcode<Ins12x> INT_TO_DOUBLE;
    public static final Opcode<Ins12x> LONG_TO_INT;
    public static final Opcode<Ins12x> LONG_TO_FLOAT;
    public static final Opcode<Ins12x> LONG_TO_DOUBLE;
    public static final Opcode<Ins12x> FLOAT_TO_INT;
    public static final Opcode<Ins12x> FLOAT_TO_LONG;
    public static final Opcode<Ins12x> FLOAT_TO_DOUBLE;
    public static final Opcode<Ins12x> DOUBLE_TO_INT;
    public static final Opcode<Ins12x> DOUBLE_TO_LONG;
    public static final Opcode<Ins12x> DOUBLE_TO_FLOAT;
    public static final Opcode<Ins12x> INT_TO_BYTE;
    public static final Opcode<Ins12x> INT_TO_CHAR;
    public static final Opcode<Ins12x> INT_TO_SHORT;
    public static final Opcode<Ins23x> ADD_INT;
    public static final Opcode<Ins23x> SUB_INT;
    public static final Opcode<Ins23x> MUL_INT;
    public static final Opcode<Ins23x> DIV_INT;
    public static final Opcode<Ins23x> REM_INT;
    public static final Opcode<Ins23x> AND_INT;
    public static final Opcode<Ins23x> OR_INT;
    public static final Opcode<Ins23x> XOR_INT;
    public static final Opcode<Ins23x> SHL_INT;
    public static final Opcode<Ins23x> SHR_INT;
    public static final Opcode<Ins23x> USHR_INT;
    public static final Opcode<Ins23x> ADD_LONG;
    public static final Opcode<Ins23x> SUB_LONG;
    public static final Opcode<Ins23x> MUL_LONG;
    public static final Opcode<Ins23x> DIV_LONG;
    public static final Opcode<Ins23x> REM_LONG;
    public static final Opcode<Ins23x> AND_LONG;
    public static final Opcode<Ins23x> OR_LONG;
    public static final Opcode<Ins23x> XOR_LONG;
    public static final Opcode<Ins23x> SHL_LONG;
    public static final Opcode<Ins23x> SHR_LONG;
    public static final Opcode<Ins23x> USHR_LONG;
    public static final Opcode<Ins23x> ADD_FLOAT;
    public static final Opcode<Ins23x> SUB_FLOAT;
    public static final Opcode<Ins23x> MUL_FLOAT;
    public static final Opcode<Ins23x> DIV_FLOAT;
    public static final Opcode<Ins23x> REM_FLOAT;
    public static final Opcode<Ins23x> ADD_DOUBLE;
    public static final Opcode<Ins23x> SUB_DOUBLE;
    public static final Opcode<Ins23x> MUL_DOUBLE;
    public static final Opcode<Ins23x> DIV_DOUBLE;
    public static final Opcode<Ins23x> REM_DOUBLE;
    public static final Opcode<Ins12x> ADD_INT_2ADDR;
    public static final Opcode<Ins12x> SUB_INT_2ADDR;
    public static final Opcode<Ins12x> MUL_INT_2ADDR;
    public static final Opcode<Ins12x> DIV_INT_2ADDR;
    public static final Opcode<Ins12x> REM_INT_2ADDR;
    public static final Opcode<Ins12x> AND_INT_2ADDR;
    public static final Opcode<Ins12x> OR_INT_2ADDR;
    public static final Opcode<Ins12x> XOR_INT_2ADDR;
    public static final Opcode<Ins12x> SHL_INT_2ADDR;
    public static final Opcode<Ins12x> SHR_INT_2ADDR;
    public static final Opcode<Ins12x> USHR_INT_2ADDR;
    public static final Opcode<Ins12x> ADD_LONG_2ADDR;
    public static final Opcode<Ins12x> SUB_LONG_2ADDR;
    public static final Opcode<Ins12x> MUL_LONG_2ADDR;
    public static final Opcode<Ins12x> DIV_LONG_2ADDR;
    public static final Opcode<Ins12x> REM_LONG_2ADDR;
    public static final Opcode<Ins12x> AND_LONG_2ADDR;
    public static final Opcode<Ins12x> OR_LONG_2ADDR;
    public static final Opcode<Ins12x> XOR_LONG_2ADDR;
    public static final Opcode<Ins12x> SHL_LONG_2ADDR;
    public static final Opcode<Ins12x> SHR_LONG_2ADDR;
    public static final Opcode<Ins12x> USHR_LONG_2ADDR;
    public static final Opcode<Ins12x> ADD_FLOAT_2ADDR;
    public static final Opcode<Ins12x> SUB_FLOAT_2ADDR;
    public static final Opcode<Ins12x> MUL_FLOAT_2ADDR;
    public static final Opcode<Ins12x> DIV_FLOAT_2ADDR;
    public static final Opcode<Ins12x> REM_FLOAT_2ADDR;
    public static final Opcode<Ins12x> ADD_DOUBLE_2ADDR;
    public static final Opcode<Ins12x> SUB_DOUBLE_2ADDR;
    public static final Opcode<Ins12x> MUL_DOUBLE_2ADDR;
    public static final Opcode<Ins12x> DIV_DOUBLE_2ADDR;
    public static final Opcode<Ins12x> REM_DOUBLE_2ADDR;
    public static final Opcode<Ins22s> ADD_INT_LIT16;
    public static final Opcode<Ins22s> RSUB_INT;
    public static final Opcode<Ins22s> MUL_INT_LIT16;
    public static final Opcode<Ins22s> DIV_INT_LIT16;
    public static final Opcode<Ins22s> REM_INT_LIT16;
    public static final Opcode<Ins22s> AND_INT_LIT16;
    public static final Opcode<Ins22s> OR_INT_LIT16;
    public static final Opcode<Ins22s> XOR_INT_LIT16;
    public static final Opcode<Ins22b> ADD_INT_LIT8;
    public static final Opcode<Ins22b> RSUB_INT_LIT8;
    public static final Opcode<Ins22b> MUL_INT_LIT8;
    public static final Opcode<Ins22b> DIV_INT_LIT8;
    public static final Opcode<Ins22b> REM_INT_LIT8;
    public static final Opcode<Ins22b> AND_INT_LIT8;
    public static final Opcode<Ins22b> OR_INT_LIT8;
    public static final Opcode<Ins22b> XOR_INT_LIT8;
    public static final Opcode<Ins22b> SHL_INT_LIT8;
    public static final Opcode<Ins22b> SHR_INT_LIT8;
    public static final Opcode<Ins22b> USHR_INT_LIT8;
    public static final Opcode<Ins22c> IGET_VOLATILE;
    public static final Opcode<Ins22c> IPUT_VOLATILE;
    public static final Opcode<Ins21c> SGET_VOLATILE;
    public static final Opcode<Ins21c> SPUT_VOLATILE;
    public static final Opcode<Ins22c> IGET_OBJECT_VOLATILE;
    public static final Opcode<Ins22c> IGET_WIDE_VOLATILE;
    public static final Opcode<Ins22c> IPUT_WIDE_VOLATILE;
    public static final Opcode<Ins21c> SGET_WIDE_VOLATILE;
    public static final Opcode<Ins21c> SPUT_WIDE_VOLATILE;
    public static final Opcode<Ins22cs> IPUT_BYTE_QUICK;
    public static final Opcode<Ins20bc> THROW_VERIFICATION_ERROR;
    public static final Opcode<Ins35mi> EXECUTE_INLINE;
    public static final Opcode<Ins3rmi> EXECUTE_INLINE_RANGE;
    public static final Opcode<Ins35c> INVOKE_DIRECT_EMPTY;
    public static final Opcode<Ins10x> RETURN_VOID_BARRIER;
    public static final Opcode<Ins22cs> IGET_QUICK;
    public static final Opcode<Ins22cs> IGET_WIDE_QUICK;
    public static final Opcode<Ins22cs> IGET_OBJECT_QUICK;
    public static final Opcode<Ins22cs> IPUT_QUICK;
    public static final Opcode<Ins22cs> IPUT_WIDE_QUICK;
    public static final Opcode<Ins22cs> IPUT_OBJECT_QUICK;
    public static final Opcode<Ins35ms> INVOKE_VIRTUAL_QUICK;
    public static final Opcode<Ins3rms> INVOKE_VIRTUAL_QUICK_RANGE;
    public static final Opcode<Ins35ms> INVOKE_SUPER_QUICK;
    public static final Opcode<Ins3rms> INVOKE_SUPER_QUICK_RANGE;
    public static final Opcode<Ins22c> IPUT_OBJECT_VOLATILE;
    public static final Opcode<Ins21c> SGET_OBJECT_VOLATILE;
    public static final Opcode<Ins21c> SPUT_OBJECT_VOLATILE;
    public static final Opcode<Ins21c> CONST_METHOD_TYPE;

    public static final Opcode<InsPackedSwitchData> PACKED_SWITCH_PAYLOAD;
    public static final Opcode<InsSparseSwitchData> SPARSE_SWITCH_PAYLOAD;
    public static final Opcode<InsArrayData> ARRAY_PAYLOAD;

    public static final Opcode<Ins22cs> IPUT_BOOLEAN_QUICK;
    public static final Opcode<Ins22cs> IPUT_CHAR_QUICK;
    public static final Opcode<Ins22cs> IPUT_SHORT_QUICK;
    public static final Opcode<Ins22cs> IGET_BOOLEAN_QUICK;
    public static final Opcode<Ins3rc> INVOKE_OBJECT_INIT_RANGE;
    public static final Opcode<Ins22cs> IGET_CHAR_QUICK;
    public static final Opcode<Ins22cs> IGET_SHORT_QUICK;
    public static final Opcode<Ins45cc> INVOKE_POLYMORPHIC;
    public static final Opcode<Ins4rcc> INVOKE_POLYMORPHIC_RANGE;
    public static final Opcode<Ins35c> INVOKE_CUSTOM;
    public static final Opcode<Ins3rc> INVOKE_CUSTOM_RANGE;
    public static final Opcode<Ins21c> CONST_METHOD_HANDLE;

    public static final Opcode<Ins22cs> IGET_BYTE_QUICK;

    static {
        Opcode<?>[] values = new Opcode[0xff + 1];
        VALUES = values;
        PAYLOADS = new Opcode[3];
        VALUES_2 = new Opcode[12];
        VALUES_3 = new Opcode[1];
        Map<String, Opcode<?>> map = new HashMap<>();
        NAME_MAP = map;

        NOP = new Opcode<>(0x00, 2, "nop", new InsNopCreator(0x0));
        values[0x00] = NOP;
        MOVE = new Opcode<>(0x01, 2, "move", new Ins12xCreator(0x1));
        values[0x01] = MOVE;
        MOVE_FROM16 = new Opcode<>(0x02, 4, "move/from16", new Ins22xCreator(0x2));
        values[0x02] = MOVE_FROM16;
        MOVE_16 = new Opcode<>(0x03, 6, "move/16", new Ins32xCreator(0x3));
        values[0x03] = MOVE_16;
        MOVE_WIDE = new Opcode<>(0x04, 2, "move-wide", new Ins12xCreator(0x4));
        values[0x04] = MOVE_WIDE;
        MOVE_WIDE_FROM16 = new Opcode<>(0x05, 4, "move-wide/from16", new Ins22xCreator(0x5));
        values[0x05] = MOVE_WIDE_FROM16;
        MOVE_WIDE_16 = new Opcode<>(0x06, 6, "move-wide/16", new Ins32xCreator(0x6));
        values[0x06] = MOVE_WIDE_16;
        MOVE_OBJECT = new Opcode<>(0x07, 2, "move-object", new Ins12xCreator(0x7));
        values[0x07] = MOVE_OBJECT;
        MOVE_OBJECT_FROM16 = new Opcode<>(0x08, 4, "move-object/from16", new Ins22xCreator(0x8));
        values[0x08] = MOVE_OBJECT_FROM16;
        MOVE_OBJECT_16 = new Opcode<>(0x09, 6, "move-object/16", new Ins32xCreator(0x9));
        values[0x09] = MOVE_OBJECT_16;
        MOVE_RESULT = new Opcode<>(0x0a, 2, "move-result", new Ins11xWriteCreator(0xa));
        values[0x0a] = MOVE_RESULT;
        MOVE_RESULT_WIDE = new Opcode<>(0x0b, 2, "move-result-wide", new Ins11xWriteCreator(0xb));
        values[0x0b] = MOVE_RESULT_WIDE;
        MOVE_RESULT_OBJECT = new Opcode<>(0x0c, 2, "move-result-object", new Ins11xWriteCreator(0xc));
        values[0x0c] = MOVE_RESULT_OBJECT;
        MOVE_EXCEPTION = new Opcode<>(0x0d, 2, "move-exception", new Ins11xWriteCreator(0xd));
        values[0x0d] = MOVE_EXCEPTION;
        RETURN_VOID = new Opcode<>(0x0e, 2, "return-void", new Ins10xCreator(0xe));
        values[0x0e] = RETURN_VOID;
        RETURN = new Opcode<>(0x0f, 2, "return", new Ins11xCreator(0xf));
        values[0x0f] = RETURN;
        RETURN_WIDE = new Opcode<>(0x10, 2, "return-wide", new Ins11xCreator(0x10));
        values[0x10] = RETURN_WIDE;
        RETURN_OBJECT = new Opcode<>(0x11, 2, "return-object", new Ins11xCreator(0x11));
        values[0x11] = RETURN_OBJECT;
        CONST_4 = new Opcode<>(0x12, 2, "const/4", new InsConst4Creator(0x12));
        values[0x12] = CONST_4;
        CONST_16 = new Opcode<>(0x13, 4, "const/16", new InsConst16Creator(0x13));
        values[0x13] = CONST_16;
        CONST = new Opcode<>(0x14, 6, "const", new InsConstCreator(0x14));
        values[0x14] = CONST;
        CONST_HIGH16 = new Opcode<>(0x15, 4, "const/high16", new InsConst16HighCreator(0x15));
        values[0x15] = CONST_HIGH16;
        CONST_WIDE_16 = new Opcode<>(0x16, 4, "const-wide/16", new InsConstWide16Creator(0x16));
        values[0x16] = CONST_WIDE_16;
        CONST_WIDE_32 = new Opcode<>(0x17, 6, "const-wide/32", new InsConstWide32Creator(0x17));
        values[0x17] = CONST_WIDE_32;
        CONST_WIDE = new Opcode<>(0x18, 10, "const-wide", new InsConstWideCreator(0x18));
        values[0x18] = CONST_WIDE;
        CONST_WIDE_HIGH16 = new Opcode<>(0x19, 4, "const-wide/high16", new InsConstWideHigh16Creator(0x19));
        values[0x19] = CONST_WIDE_HIGH16;
        CONST_STRING = new Opcode<>(0x1a, 4, "const-string", SectionType.STRING_ID, new InsConstStringCreator(0x1a));
        values[0x1a] = CONST_STRING;
        CONST_STRING_JUMBO = new Opcode<>(0x1b, 6, "const-string/jumbo", SectionType.STRING_ID, new InsConstStringJumboCreator(0x1b));
        values[0x1b] = CONST_STRING_JUMBO;
        CONST_CLASS = new Opcode<>(0x1c, 4, "const-class", SectionType.TYPE_ID, new Ins21cCreator(0x1c));
        values[0x1c] = CONST_CLASS;
        MONITOR_ENTER = new Opcode<>(0x1d, 2, "monitor-enter", new Ins11xCreator(0x1d));
        values[0x1d] = MONITOR_ENTER;
        MONITOR_EXIT = new Opcode<>(0x1e, 2, "monitor-exit", new Ins11xCreator(0x1e));
        values[0x1e] = MONITOR_EXIT;
        CHECK_CAST = new Opcode<>(0x1f, 4, "check-cast", SectionType.TYPE_ID, new Ins21cCreator(0x1f));
        values[0x1f] = CHECK_CAST;
        INSTANCE_OF = new Opcode<>(0x20, 4, "instance-of", SectionType.TYPE_ID, new Ins22cCreator(0x20));
        values[0x20] = INSTANCE_OF;
        ARRAY_LENGTH = new Opcode<>(0x21, 2, "array-length", new Ins12xCreator(0x21));
        values[0x21] = ARRAY_LENGTH;
        NEW_INSTANCE = new Opcode<>(0x22, 4, "new-instance", SectionType.TYPE_ID, new Ins21cCreator(0x22));
        values[0x22] = NEW_INSTANCE;
        NEW_ARRAY = new Opcode<>(0x23, 4, "new-array", SectionType.TYPE_ID, new Ins22cCreator(0x23));
        values[0x23] = NEW_ARRAY;
        FILLED_NEW_ARRAY = new Opcode<>(0x24, 6, "filled-new-array", SectionType.TYPE_ID, new Ins35cCreator(0x24));
        values[0x24] = FILLED_NEW_ARRAY;
        FILLED_NEW_ARRAY_RANGE = new Opcode<>(0x25, 6, "filled-new-array/range", SectionType.TYPE_ID, new Ins3rcCreator(0x25));
        values[0x25] = FILLED_NEW_ARRAY_RANGE;
        FILL_ARRAY_DATA = new Opcode<>(0x26, 6, "fill-array-data", new InsFillArrayDataCreator(0x26));
        values[0x26] = FILL_ARRAY_DATA;
        THROW = new Opcode<>(0x27, 2, "throw", new Ins11xCreator(0x27));
        values[0x27] = THROW;
        GOTO = new Opcode<>(0x28, 2, "goto", new InsGotoCreator(0x28));
        values[0x28] = GOTO;
        GOTO_16 = new Opcode<>(0x29, 4, "goto/16", new InsGotoCreator(0x29));
        values[0x29] = GOTO_16;
        GOTO_32 = new Opcode<>(0x2a, 6, "goto/32", new InsGotoCreator(0x2a));
        values[0x2a] = GOTO_32;
        PACKED_SWITCH = new Opcode<>(0x2b, 6, "packed-switch", new InsPackedSwitchCreator(0x2b));
        values[0x2b] = PACKED_SWITCH;
        SPARSE_SWITCH = new Opcode<>(0x2c, 6, "sparse-switch", new InsSparseSwitchCreator(0x2c));
        values[0x2c] = SPARSE_SWITCH;
        CMPL_FLOAT = new Opcode<>(0x2d, 4, "cmpl-float", new Ins23xCreator(0x2d));
        values[0x2d] = CMPL_FLOAT;
        CMPG_FLOAT = new Opcode<>(0x2e, 4, "cmpg-float", new Ins23xCreator(0x2e));
        values[0x2e] = CMPG_FLOAT;
        CMPL_DOUBLE = new Opcode<>(0x2f, 4, "cmpl-double", new Ins23xCreator(0x2f));
        values[0x2f] = CMPL_DOUBLE;
        CMPG_DOUBLE = new Opcode<>(0x30, 4, "cmpg-double", new Ins23xCreator(0x30));
        values[0x30] = CMPG_DOUBLE;
        CMP_LONG = new Opcode<>(0x31, 4, "cmp-long", new Ins23xCreator(0x31));
        values[0x31] = CMP_LONG;
        IF_EQ = new Opcode<>(0x32, 4, "if-eq", new Ins22tCreator(0x32));
        values[0x32] = IF_EQ;
        IF_NE = new Opcode<>(0x33, 4, "if-ne", new Ins22tCreator(0x33));
        values[0x33] = IF_NE;
        IF_LT = new Opcode<>(0x34, 4, "if-lt", new Ins22tCreator(0x34));
        values[0x34] = IF_LT;
        IF_GE = new Opcode<>(0x35, 4, "if-ge", new Ins22tCreator(0x35));
        values[0x35] = IF_GE;
        IF_GT = new Opcode<>(0x36, 4, "if-gt", new Ins22tCreator(0x36));
        values[0x36] = IF_GT;
        IF_LE = new Opcode<>(0x37, 4, "if-le", new Ins22tCreator(0x37));
        values[0x37] = IF_LE;
        IF_EQZ = new Opcode<>(0x38, 4, "if-eqz", new Ins21tCreator(0x38));
        values[0x38] = IF_EQZ;
        IF_NEZ = new Opcode<>(0x39, 4, "if-nez", new Ins21tCreator(0x39));
        values[0x39] = IF_NEZ;
        IF_LTZ = new Opcode<>(0x3a, 4, "if-ltz", new Ins21tCreator(0x3a));
        values[0x3a] = IF_LTZ;
        IF_GEZ = new Opcode<>(0x3b, 4, "if-gez", new Ins21tCreator(0x3b));
        values[0x3b] = IF_GEZ;
        IF_GTZ = new Opcode<>(0x3c, 4, "if-gtz", new Ins21tCreator(0x3c));
        values[0x3c] = IF_GTZ;
        IF_LEZ = new Opcode<>(0x3d, 4, "if-lez", new Ins21tCreator(0x3d));
        values[0x3d] = IF_LEZ;
        AGET = new Opcode<>(0x44, 4, "aget", new Ins23xCreator(0x44));
        values[0x44] = AGET;
        AGET_WIDE = new Opcode<>(0x45, 4, "aget-wide", new Ins23xCreator(0x45));
        values[0x45] = AGET_WIDE;
        AGET_OBJECT = new Opcode<>(0x46, 4, "aget-object", new Ins23xCreator(0x46));
        values[0x46] = AGET_OBJECT;
        AGET_BOOLEAN = new Opcode<>(0x47, 4, "aget-boolean", new Ins23xCreator(0x47));
        values[0x47] = AGET_BOOLEAN;
        AGET_BYTE = new Opcode<>(0x48, 4, "aget-byte", new Ins23xCreator(0x48));
        values[0x48] = AGET_BYTE;
        AGET_CHAR = new Opcode<>(0x49, 4, "aget-char", new Ins23xCreator(0x49));
        values[0x49] = AGET_CHAR;
        AGET_SHORT = new Opcode<>(0x4a, 4, "aget-short", new Ins23xCreator(0x4a));
        values[0x4a] = AGET_SHORT;
        APUT = new Opcode<>(0x4b, 4, "aput", new Ins23xAputCreator(0x4b));
        values[0x4b] = APUT;
        APUT_WIDE = new Opcode<>(0x4c, 4, "aput-wide", new Ins23xAputCreator(0x4c));
        values[0x4c] = APUT_WIDE;
        APUT_OBJECT = new Opcode<>(0x4d, 4, "aput-object", new Ins23xAputCreator(0x4d));
        values[0x4d] = APUT_OBJECT;
        APUT_BOOLEAN = new Opcode<>(0x4e, 4, "aput-boolean", new Ins23xAputCreator(0x4e));
        values[0x4e] = APUT_BOOLEAN;
        APUT_BYTE = new Opcode<>(0x4f, 4, "aput-byte", new Ins23xAputCreator(0x4f));
        values[0x4f] = APUT_BYTE;
        APUT_CHAR = new Opcode<>(0x50, 4, "aput-char", new Ins23xAputCreator(0x50));
        values[0x50] = APUT_CHAR;
        APUT_SHORT = new Opcode<>(0x51, 4, "aput-short", new Ins23xAputCreator(0x51));
        values[0x51] = APUT_SHORT;
        IGET = new Opcode<>(0x52, 4, "iget", SectionType.FIELD_ID, new Ins22cCreator(0x52));
        values[0x52] = IGET;
        IGET_WIDE = new Opcode<>(0x53, 4, "iget-wide", SectionType.FIELD_ID, new Ins22cCreator(0x53));
        values[0x53] = IGET_WIDE;
        IGET_OBJECT = new Opcode<>(0x54, 4, "iget-object", SectionType.FIELD_ID, new Ins22cCreator(0x54));
        values[0x54] = IGET_OBJECT;
        IGET_BOOLEAN = new Opcode<>(0x55, 4, "iget-boolean", SectionType.FIELD_ID, new Ins22cCreator(0x55));
        values[0x55] = IGET_BOOLEAN;
        IGET_BYTE = new Opcode<>(0x56, 4, "iget-byte", SectionType.FIELD_ID, new Ins22cCreator(0x56));
        values[0x56] = IGET_BYTE;
        IGET_CHAR = new Opcode<>(0x57, 4, "iget-char", SectionType.FIELD_ID, new Ins22cCreator(0x57));
        values[0x57] = IGET_CHAR;
        IGET_SHORT = new Opcode<>(0x58, 4, "iget-short", SectionType.FIELD_ID, new Ins22cCreator(0x58));
        values[0x58] = IGET_SHORT;
        IPUT = new Opcode<>(0x59, 4, "iput", SectionType.FIELD_ID, new Ins22cPutCreator(0x59));
        values[0x59] = IPUT;
        IPUT_WIDE = new Opcode<>(0x5a, 4, "iput-wide", SectionType.FIELD_ID, new Ins22cPutCreator(0x5a));
        values[0x5a] = IPUT_WIDE;
        IPUT_OBJECT = new Opcode<>(0x5b, 4, "iput-object", SectionType.FIELD_ID, new Ins22cPutCreator(0x5b));
        values[0x5b] = IPUT_OBJECT;
        IPUT_BOOLEAN = new Opcode<>(0x5c, 4, "iput-boolean", SectionType.FIELD_ID, new Ins22cPutCreator(0x5c));
        values[0x5c] = IPUT_BOOLEAN;
        IPUT_BYTE = new Opcode<>(0x5d, 4, "iput-byte", SectionType.FIELD_ID, new Ins22cPutCreator(0x5d));
        values[0x5d] = IPUT_BYTE;
        IPUT_CHAR = new Opcode<>(0x5e, 4, "iput-char", SectionType.FIELD_ID, new Ins22cPutCreator(0x5e));
        values[0x5e] = IPUT_CHAR;
        IPUT_SHORT = new Opcode<>(0x5f, 4, "iput-short", SectionType.FIELD_ID, new Ins22cPutCreator(0x5f));
        values[0x5f] = IPUT_SHORT;
        SGET = new Opcode<>(0x60, 4, "sget", SectionType.FIELD_ID, new Ins21cCreator(0x60));
        values[0x60] = SGET;
        SGET_WIDE = new Opcode<>(0x61, 4, "sget-wide", SectionType.FIELD_ID, new Ins21cCreator(0x61));
        values[0x61] = SGET_WIDE;
        SGET_OBJECT = new Opcode<>(0x62, 4, "sget-object", SectionType.FIELD_ID, new Ins21cCreator(0x62));
        values[0x62] = SGET_OBJECT;
        SGET_BOOLEAN = new Opcode<>(0x63, 4, "sget-boolean", SectionType.FIELD_ID, new Ins21cCreator(0x63));
        values[0x63] = SGET_BOOLEAN;
        SGET_BYTE = new Opcode<>(0x64, 4, "sget-byte", SectionType.FIELD_ID, new Ins21cCreator(0x64));
        values[0x64] = SGET_BYTE;
        SGET_CHAR = new Opcode<>(0x65, 4, "sget-char", SectionType.FIELD_ID, new Ins21cCreator(0x65));
        values[0x65] = SGET_CHAR;
        SGET_SHORT = new Opcode<>(0x66, 4, "sget-short", SectionType.FIELD_ID, new Ins21cCreator(0x66));
        values[0x66] = SGET_SHORT;
        SPUT = new Opcode<>(0x67, 4, "sput", SectionType.FIELD_ID, new Ins21cPutCreator(0x67));
        values[0x67] = SPUT;
        SPUT_WIDE = new Opcode<>(0x68, 4, "sput-wide", SectionType.FIELD_ID, new Ins21cPutCreator(0x68));
        values[0x68] = SPUT_WIDE;
        SPUT_OBJECT = new Opcode<>(0x69, 4, "sput-object", SectionType.FIELD_ID, new Ins21cPutCreator(0x69));
        values[0x69] = SPUT_OBJECT;
        SPUT_BOOLEAN = new Opcode<>(0x6a, 4, "sput-boolean", SectionType.FIELD_ID, new Ins21cPutCreator(0x6a));
        values[0x6a] = SPUT_BOOLEAN;
        SPUT_BYTE = new Opcode<>(0x6b, 4, "sput-byte", SectionType.FIELD_ID, new Ins21cPutCreator(0x6b));
        values[0x6b] = SPUT_BYTE;
        SPUT_CHAR = new Opcode<>(0x6c, 4, "sput-char", SectionType.FIELD_ID, new Ins21cPutCreator(0x6c));
        values[0x6c] = SPUT_CHAR;
        SPUT_SHORT = new Opcode<>(0x6d, 4, "sput-short", SectionType.FIELD_ID, new Ins21cPutCreator(0x6d));
        values[0x6d] = SPUT_SHORT;
        INVOKE_VIRTUAL = new Opcode<>(0x6e, 6, "invoke-virtual", SectionType.METHOD_ID, new Ins35cCreator(0x6e));
        values[0x6e] = INVOKE_VIRTUAL;
        INVOKE_SUPER = new Opcode<>(0x6f, 6, "invoke-super", SectionType.METHOD_ID, new Ins35cCreator(0x6f));
        values[0x6f] = INVOKE_SUPER;
        INVOKE_DIRECT = new Opcode<>(0x70, 6, "invoke-direct", SectionType.METHOD_ID, new Ins35cCreator(0x70));
        values[0x70] = INVOKE_DIRECT;
        INVOKE_STATIC = new Opcode<>(0x71, 6, "invoke-static", SectionType.METHOD_ID, new Ins35cCreator(0x71));
        values[0x71] = INVOKE_STATIC;
        INVOKE_INTERFACE = new Opcode<>(0x72, 6, "invoke-interface", SectionType.METHOD_ID, new Ins35cCreator(0x72));
        values[0x72] = INVOKE_INTERFACE;
        RETURN_VOID_NO_BARRIER = new Opcode<>(0x73, 2, "return-void-no-barrier", new Ins10xCreator(0x73));
        values[0x73] = RETURN_VOID_NO_BARRIER;
        INVOKE_VIRTUAL_RANGE = new Opcode<>(0x74, 6, "invoke-virtual/range", SectionType.METHOD_ID, new Ins3rcCreator(0x74));
        values[0x74] = INVOKE_VIRTUAL_RANGE;
        INVOKE_SUPER_RANGE = new Opcode<>(0x75, 6, "invoke-super/range", SectionType.METHOD_ID, new Ins3rcCreator(0x75));
        values[0x75] = INVOKE_SUPER_RANGE;
        INVOKE_DIRECT_RANGE = new Opcode<>(0x76, 6, "invoke-direct/range", SectionType.METHOD_ID, new Ins3rcCreator(0x76));
        values[0x76] = INVOKE_DIRECT_RANGE;
        INVOKE_STATIC_RANGE = new Opcode<>(0x77, 6, "invoke-static/range", SectionType.METHOD_ID, new Ins3rcCreator(0x77));
        values[0x77] = INVOKE_STATIC_RANGE;
        INVOKE_INTERFACE_RANGE = new Opcode<>(0x78, 6, "invoke-interface/range", SectionType.METHOD_ID, new Ins3rcCreator(0x78));
        values[0x78] = INVOKE_INTERFACE_RANGE;
        NEG_INT = new Opcode<>(0x7b, 2, "neg-int", new Ins12xCreator(0x7b));
        values[0x7b] = NEG_INT;
        NOT_INT = new Opcode<>(0x7c, 2, "not-int", new Ins12xCreator(0x7c));
        values[0x7c] = NOT_INT;
        NEG_LONG = new Opcode<>(0x7d, 2, "neg-long", new Ins12xCreator(0x7d));
        values[0x7d] = NEG_LONG;
        NOT_LONG = new Opcode<>(0x7e, 2, "not-long", new Ins12xCreator(0x7e));
        values[0x7e] = NOT_LONG;
        NEG_FLOAT = new Opcode<>(0x7f, 2, "neg-float", new Ins12xCreator(0x7f));
        values[0x7f] = NEG_FLOAT;
        NEG_DOUBLE = new Opcode<>(0x80, 2, "neg-double", new Ins12xCreator(0x80));
        values[0x80] = NEG_DOUBLE;
        INT_TO_LONG = new Opcode<>(0x81, 2, "int-to-long", new Ins12xCreator(0x81));
        values[0x81] = INT_TO_LONG;
        INT_TO_FLOAT = new Opcode<>(0x82, 2, "int-to-float", new Ins12xCreator(0x82));
        values[0x82] = INT_TO_FLOAT;
        INT_TO_DOUBLE = new Opcode<>(0x83, 2, "int-to-double", new Ins12xCreator(0x83));
        values[0x83] = INT_TO_DOUBLE;
        LONG_TO_INT = new Opcode<>(0x84, 2, "long-to-int", new Ins12xCreator(0x84));
        values[0x84] = LONG_TO_INT;
        LONG_TO_FLOAT = new Opcode<>(0x85, 2, "long-to-float", new Ins12xCreator(0x85));
        values[0x85] = LONG_TO_FLOAT;
        LONG_TO_DOUBLE = new Opcode<>(0x86, 2, "long-to-double", new Ins12xCreator(0x86));
        values[0x86] = LONG_TO_DOUBLE;
        FLOAT_TO_INT = new Opcode<>(0x87, 2, "float-to-int", new Ins12xCreator(0x87));
        values[0x87] = FLOAT_TO_INT;
        FLOAT_TO_LONG = new Opcode<>(0x88, 2, "float-to-long", new Ins12xCreator(0x88));
        values[0x88] = FLOAT_TO_LONG;
        FLOAT_TO_DOUBLE = new Opcode<>(0x89, 2, "float-to-double", new Ins12xCreator(0x89));
        values[0x89] = FLOAT_TO_DOUBLE;
        DOUBLE_TO_INT = new Opcode<>(0x8a, 2, "double-to-int", new Ins12xCreator(0x8a));
        values[0x8a] = DOUBLE_TO_INT;
        DOUBLE_TO_LONG = new Opcode<>(0x8b, 2, "double-to-long", new Ins12xCreator(0x8b));
        values[0x8b] = DOUBLE_TO_LONG;
        DOUBLE_TO_FLOAT = new Opcode<>(0x8c, 2, "double-to-float", new Ins12xCreator(0x8c));
        values[0x8c] = DOUBLE_TO_FLOAT;
        INT_TO_BYTE = new Opcode<>(0x8d, 2, "int-to-byte", new Ins12xCreator(0x8d));
        values[0x8d] = INT_TO_BYTE;
        INT_TO_CHAR = new Opcode<>(0x8e, 2, "int-to-char", new Ins12xCreator(0x8e));
        values[0x8e] = INT_TO_CHAR;
        INT_TO_SHORT = new Opcode<>(0x8f, 2, "int-to-short", new Ins12xCreator(0x8f));
        values[0x8f] = INT_TO_SHORT;
        ADD_INT = new Opcode<>(0x90, 4, "add-int", new Ins23xCreator(0x90));
        values[0x90] = ADD_INT;
        SUB_INT = new Opcode<>(0x91, 4, "sub-int", new Ins23xCreator(0x91));
        values[0x91] = SUB_INT;
        MUL_INT = new Opcode<>(0x92, 4, "mul-int", new Ins23xCreator(0x92));
        values[0x92] = MUL_INT;
        DIV_INT = new Opcode<>(0x93, 4, "div-int", new Ins23xCreator(0x93));
        values[0x93] = DIV_INT;
        REM_INT = new Opcode<>(0x94, 4, "rem-int", new Ins23xCreator(0x94));
        values[0x94] = REM_INT;
        AND_INT = new Opcode<>(0x95, 4, "and-int", new Ins23xCreator(0x95));
        values[0x95] = AND_INT;
        OR_INT = new Opcode<>(0x96, 4, "or-int", new Ins23xCreator(0x96));
        values[0x96] = OR_INT;
        XOR_INT = new Opcode<>(0x97, 4, "xor-int", new Ins23xCreator(0x97));
        values[0x97] = XOR_INT;
        SHL_INT = new Opcode<>(0x98, 4, "shl-int", new Ins23xCreator(0x98));
        values[0x98] = SHL_INT;
        SHR_INT = new Opcode<>(0x99, 4, "shr-int", new Ins23xCreator(0x99));
        values[0x99] = SHR_INT;
        USHR_INT = new Opcode<>(0x9a, 4, "ushr-int", new Ins23xCreator(0x9a));
        values[0x9a] = USHR_INT;
        ADD_LONG = new Opcode<>(0x9b, 4, "add-long", new Ins23xCreator(0x9b));
        values[0x9b] = ADD_LONG;
        SUB_LONG = new Opcode<>(0x9c, 4, "sub-long", new Ins23xCreator(0x9c));
        values[0x9c] = SUB_LONG;
        MUL_LONG = new Opcode<>(0x9d, 4, "mul-long", new Ins23xCreator(0x9d));
        values[0x9d] = MUL_LONG;
        DIV_LONG = new Opcode<>(0x9e, 4, "div-long", new Ins23xCreator(0x9e));
        values[0x9e] = DIV_LONG;
        REM_LONG = new Opcode<>(0x9f, 4, "rem-long", new Ins23xCreator(0x9f));
        values[0x9f] = REM_LONG;
        AND_LONG = new Opcode<>(0xa0, 4, "and-long", new Ins23xCreator(0xa0));
        values[0xa0] = AND_LONG;
        OR_LONG = new Opcode<>(0xa1, 4, "or-long", new Ins23xCreator(0xa1));
        values[0xa1] = OR_LONG;
        XOR_LONG = new Opcode<>(0xa2, 4, "xor-long", new Ins23xCreator(0xa2));
        values[0xa2] = XOR_LONG;
        SHL_LONG = new Opcode<>(0xa3, 4, "shl-long", new Ins23xCreator(0xa3));
        values[0xa3] = SHL_LONG;
        SHR_LONG = new Opcode<>(0xa4, 4, "shr-long", new Ins23xCreator(0xa4));
        values[0xa4] = SHR_LONG;
        USHR_LONG = new Opcode<>(0xa5, 4, "ushr-long", new Ins23xCreator(0xa5));
        values[0xa5] = USHR_LONG;
        ADD_FLOAT = new Opcode<>(0xa6, 4, "add-float", new Ins23xCreator(0xa6));
        values[0xa6] = ADD_FLOAT;
        SUB_FLOAT = new Opcode<>(0xa7, 4, "sub-float", new Ins23xCreator(0xa7));
        values[0xa7] = SUB_FLOAT;
        MUL_FLOAT = new Opcode<>(0xa8, 4, "mul-float", new Ins23xCreator(0xa8));
        values[0xa8] = MUL_FLOAT;
        DIV_FLOAT = new Opcode<>(0xa9, 4, "div-float", new Ins23xCreator(0xa9));
        values[0xa9] = DIV_FLOAT;
        REM_FLOAT = new Opcode<>(0xaa, 4, "rem-float", new Ins23xCreator(0xaa));
        values[0xaa] = REM_FLOAT;
        ADD_DOUBLE = new Opcode<>(0xab, 4, "add-double", new Ins23xCreator(0xab));
        values[0xab] = ADD_DOUBLE;
        SUB_DOUBLE = new Opcode<>(0xac, 4, "sub-double", new Ins23xCreator(0xac));
        values[0xac] = SUB_DOUBLE;
        MUL_DOUBLE = new Opcode<>(0xad, 4, "mul-double", new Ins23xCreator(0xad));
        values[0xad] = MUL_DOUBLE;
        DIV_DOUBLE = new Opcode<>(0xae, 4, "div-double", new Ins23xCreator(0xae));
        values[0xae] = DIV_DOUBLE;
        REM_DOUBLE = new Opcode<>(0xaf, 4, "rem-double", new Ins23xCreator(0xaf));
        values[0xaf] = REM_DOUBLE;
        ADD_INT_2ADDR = new Opcode<>(0xb0, 2, "add-int/2addr", new Ins12xCreator(0xb0));
        values[0xb0] = ADD_INT_2ADDR;
        SUB_INT_2ADDR = new Opcode<>(0xb1, 2, "sub-int/2addr", new Ins12xCreator(0xb1));
        values[0xb1] = SUB_INT_2ADDR;
        MUL_INT_2ADDR = new Opcode<>(0xb2, 2, "mul-int/2addr", new Ins12xCreator(0xb2));
        values[0xb2] = MUL_INT_2ADDR;
        DIV_INT_2ADDR = new Opcode<>(0xb3, 2, "div-int/2addr", new Ins12xCreator(0xb3));
        values[0xb3] = DIV_INT_2ADDR;
        REM_INT_2ADDR = new Opcode<>(0xb4, 2, "rem-int/2addr", new Ins12xCreator(0xb4));
        values[0xb4] = REM_INT_2ADDR;
        AND_INT_2ADDR = new Opcode<>(0xb5, 2, "and-int/2addr", new Ins12xCreator(0xb5));
        values[0xb5] = AND_INT_2ADDR;
        OR_INT_2ADDR = new Opcode<>(0xb6, 2, "or-int/2addr", new Ins12xCreator(0xb6));
        values[0xb6] = OR_INT_2ADDR;
        XOR_INT_2ADDR = new Opcode<>(0xb7, 2, "xor-int/2addr", new Ins12xCreator(0xb7));
        values[0xb7] = XOR_INT_2ADDR;
        SHL_INT_2ADDR = new Opcode<>(0xb8, 2, "shl-int/2addr", new Ins12xCreator(0xb8));
        values[0xb8] = SHL_INT_2ADDR;
        SHR_INT_2ADDR = new Opcode<>(0xb9, 2, "shr-int/2addr", new Ins12xCreator(0xb9));
        values[0xb9] = SHR_INT_2ADDR;
        USHR_INT_2ADDR = new Opcode<>(0xba, 2, "ushr-int/2addr", new Ins12xCreator(0xba));
        values[0xba] = USHR_INT_2ADDR;
        ADD_LONG_2ADDR = new Opcode<>(0xbb, 2, "add-long/2addr", new Ins12xCreator(0xbb));
        values[0xbb] = ADD_LONG_2ADDR;
        SUB_LONG_2ADDR = new Opcode<>(0xbc, 2, "sub-long/2addr", new Ins12xCreator(0xbc));
        values[0xbc] = SUB_LONG_2ADDR;
        MUL_LONG_2ADDR = new Opcode<>(0xbd, 2, "mul-long/2addr", new Ins12xCreator(0xbd));
        values[0xbd] = MUL_LONG_2ADDR;
        DIV_LONG_2ADDR = new Opcode<>(0xbe, 2, "div-long/2addr", new Ins12xCreator(0xbe));
        values[0xbe] = DIV_LONG_2ADDR;
        REM_LONG_2ADDR = new Opcode<>(0xbf, 2, "rem-long/2addr", new Ins12xCreator(0xbf));
        values[0xbf] = REM_LONG_2ADDR;
        AND_LONG_2ADDR = new Opcode<>(0xc0, 2, "and-long/2addr", new Ins12xCreator(0xc0));
        values[0xc0] = AND_LONG_2ADDR;
        OR_LONG_2ADDR = new Opcode<>(0xc1, 2, "or-long/2addr", new Ins12xCreator(0xc1));
        values[0xc1] = OR_LONG_2ADDR;
        XOR_LONG_2ADDR = new Opcode<>(0xc2, 2, "xor-long/2addr", new Ins12xCreator(0xc2));
        values[0xc2] = XOR_LONG_2ADDR;
        SHL_LONG_2ADDR = new Opcode<>(0xc3, 2, "shl-long/2addr", new Ins12xCreator(0xc3));
        values[0xc3] = SHL_LONG_2ADDR;
        SHR_LONG_2ADDR = new Opcode<>(0xc4, 2, "shr-long/2addr", new Ins12xCreator(0xc4));
        values[0xc4] = SHR_LONG_2ADDR;
        USHR_LONG_2ADDR = new Opcode<>(0xc5, 2, "ushr-long/2addr", new Ins12xCreator(0xc5));
        values[0xc5] = USHR_LONG_2ADDR;
        ADD_FLOAT_2ADDR = new Opcode<>(0xc6, 2, "add-float/2addr", new Ins12xCreator(0xc6));
        values[0xc6] = ADD_FLOAT_2ADDR;
        SUB_FLOAT_2ADDR = new Opcode<>(0xc7, 2, "sub-float/2addr", new Ins12xCreator(0xc7));
        values[0xc7] = SUB_FLOAT_2ADDR;
        MUL_FLOAT_2ADDR = new Opcode<>(0xc8, 2, "mul-float/2addr", new Ins12xCreator(0xc8));
        values[0xc8] = MUL_FLOAT_2ADDR;
        DIV_FLOAT_2ADDR = new Opcode<>(0xc9, 2, "div-float/2addr", new Ins12xCreator(0xc9));
        values[0xc9] = DIV_FLOAT_2ADDR;
        REM_FLOAT_2ADDR = new Opcode<>(0xca, 2, "rem-float/2addr", new Ins12xCreator(0xca));
        values[0xca] = REM_FLOAT_2ADDR;
        ADD_DOUBLE_2ADDR = new Opcode<>(0xcb, 2, "add-double/2addr", new Ins12xCreator(0xcb));
        values[0xcb] = ADD_DOUBLE_2ADDR;
        SUB_DOUBLE_2ADDR = new Opcode<>(0xcc, 2, "sub-double/2addr", new Ins12xCreator(0xcc));
        values[0xcc] = SUB_DOUBLE_2ADDR;
        MUL_DOUBLE_2ADDR = new Opcode<>(0xcd, 2, "mul-double/2addr", new Ins12xCreator(0xcd));
        values[0xcd] = MUL_DOUBLE_2ADDR;
        DIV_DOUBLE_2ADDR = new Opcode<>(0xce, 2, "div-double/2addr", new Ins12xCreator(0xce));
        values[0xce] = DIV_DOUBLE_2ADDR;
        REM_DOUBLE_2ADDR = new Opcode<>(0xcf, 2, "rem-double/2addr", new Ins12xCreator(0xcf));
        values[0xcf] = REM_DOUBLE_2ADDR;
        ADD_INT_LIT16 = new Opcode<>(0xd0, 4, "add-int/lit16", new Ins22sCreator(0xd0));
        values[0xd0] = ADD_INT_LIT16;
        RSUB_INT = new Opcode<>(0xd1, 4, "rsub-int", new Ins22sCreator(0xd1));
        values[0xd1] = RSUB_INT;
        MUL_INT_LIT16 = new Opcode<>(0xd2, 4, "mul-int/lit16", new Ins22sCreator(0xd2));
        values[0xd2] = MUL_INT_LIT16;
        DIV_INT_LIT16 = new Opcode<>(0xd3, 4, "div-int/lit16", new Ins22sCreator(0xd3));
        values[0xd3] = DIV_INT_LIT16;
        REM_INT_LIT16 = new Opcode<>(0xd4, 4, "rem-int/lit16", new Ins22sCreator(0xd4));
        values[0xd4] = REM_INT_LIT16;
        AND_INT_LIT16 = new Opcode<>(0xd5, 4, "and-int/lit16", new Ins22sCreator(0xd5));
        values[0xd5] = AND_INT_LIT16;
        OR_INT_LIT16 = new Opcode<>(0xd6, 4, "or-int/lit16", new Ins22sCreator(0xd6));
        values[0xd6] = OR_INT_LIT16;
        XOR_INT_LIT16 = new Opcode<>(0xd7, 4, "xor-int/lit16", new Ins22sCreator(0xd7));
        values[0xd7] = XOR_INT_LIT16;
        ADD_INT_LIT8 = new Opcode<>(0xd8, 4, "add-int/lit8", new Ins22bCreator(0xd8));
        values[0xd8] = ADD_INT_LIT8;
        RSUB_INT_LIT8 = new Opcode<>(0xd9, 4, "rsub-int/lit8", new Ins22bCreator(0xd9));
        values[0xd9] = RSUB_INT_LIT8;
        MUL_INT_LIT8 = new Opcode<>(0xda, 4, "mul-int/lit8", new Ins22bCreator(0xda));
        values[0xda] = MUL_INT_LIT8;
        DIV_INT_LIT8 = new Opcode<>(0xdb, 4, "div-int/lit8", new Ins22bCreator(0xdb));
        values[0xdb] = DIV_INT_LIT8;
        REM_INT_LIT8 = new Opcode<>(0xdc, 4, "rem-int/lit8", new Ins22bCreator(0xdc));
        values[0xdc] = REM_INT_LIT8;
        AND_INT_LIT8 = new Opcode<>(0xdd, 4, "and-int/lit8", new Ins22bCreator(0xdd));
        values[0xdd] = AND_INT_LIT8;
        OR_INT_LIT8 = new Opcode<>(0xde, 4, "or-int/lit8", new Ins22bCreator(0xde));
        values[0xde] = OR_INT_LIT8;
        XOR_INT_LIT8 = new Opcode<>(0xdf, 4, "xor-int/lit8", new Ins22bCreator(0xdf));
        values[0xdf] = XOR_INT_LIT8;
        SHL_INT_LIT8 = new Opcode<>(0xe0, 4, "shl-int/lit8", new Ins22bCreator(0xe0));
        values[0xe0] = SHL_INT_LIT8;
        SHR_INT_LIT8 = new Opcode<>(0xe1, 4, "shr-int/lit8", new Ins22bCreator(0xe1));
        values[0xe1] = SHR_INT_LIT8;
        USHR_INT_LIT8 = new Opcode<>(0xe2, 4, "ushr-int/lit8", new Ins22bCreator(0xe2));
        values[0xe2] = USHR_INT_LIT8;
        IGET_VOLATILE = new Opcode<>(0xe3, 4, "iget-volatile", SectionType.FIELD_ID, new Ins22cCreator(0xe3));
        values[0xe3] = IGET_VOLATILE;
        IPUT_VOLATILE = new Opcode<>(0xe4, 4, "iput-volatile", SectionType.FIELD_ID, new Ins22cPutCreator(0xe4));
        values[0xe4] = IPUT_VOLATILE;
        SGET_VOLATILE = new Opcode<>(0xe5, 4, "sget-volatile", SectionType.FIELD_ID, new Ins21cCreator(0xe5));
        values[0xe5] = SGET_VOLATILE;
        SPUT_VOLATILE = new Opcode<>(0xe6, 4, "sput-volatile", SectionType.FIELD_ID, new Ins21cPutCreator(0xe6));
        values[0xe6] = SPUT_VOLATILE;
        IGET_OBJECT_VOLATILE = new Opcode<>(0xe7, 4, "iget-object-volatile", SectionType.FIELD_ID, new Ins22cCreator(0xe7));
        values[0xe7] = IGET_OBJECT_VOLATILE;
        IGET_WIDE_VOLATILE = new Opcode<>(0xe8, 4, "iget-wide-volatile", SectionType.FIELD_ID, new Ins22cCreator(0xe8));
        values[0xe8] = IGET_WIDE_VOLATILE;
        IPUT_WIDE_VOLATILE = new Opcode<>(0xe9, 4, "iput-wide-volatile", SectionType.FIELD_ID, new Ins22cPutCreator(0xe9));
        values[0xe9] = IPUT_WIDE_VOLATILE;
        SGET_WIDE_VOLATILE = new Opcode<>(0xea, 4, "sget-wide-volatile", SectionType.FIELD_ID, new Ins21cCreator(0xea));
        values[0xea] = SGET_WIDE_VOLATILE;
        SPUT_WIDE_VOLATILE = new Opcode<>(0xeb, 4, "sput-wide-volatile", SectionType.FIELD_ID, new Ins21cPutCreator(0xeb));
        values[0xeb] = SPUT_WIDE_VOLATILE;
        IPUT_BYTE_QUICK = new Opcode<>(0xec, 4, "iput-byte-quick", SectionType.FIELD_ID, new Ins22csPutCreator(0xec));
        values[0xec] = IPUT_BYTE_QUICK;
        THROW_VERIFICATION_ERROR = new Opcode<>(0xed, 4, "throw-verification-error", new Ins20bcCreator(0xed));
        values[0xed] = THROW_VERIFICATION_ERROR;

        EXECUTE_INLINE = new Opcode<>(0xee, 6, "execute-inline", new Ins35miCreator(0xee));
        values[0xee] = EXECUTE_INLINE;
        EXECUTE_INLINE_RANGE = new Opcode<>(0xef, 6, "execute-inline/range", new Ins3rmiCreator(0xef));
        values[0xef] = EXECUTE_INLINE_RANGE;
        INVOKE_DIRECT_EMPTY = new Opcode<>(0xf0, 6, "invoke-direct-empty", SectionType.METHOD_ID, new Ins35cCreator(0xf0));
        values[0xf0] = INVOKE_DIRECT_EMPTY;
        RETURN_VOID_BARRIER = new Opcode<>(0xf1, 2, "return-void-barrier", new Ins10xCreator(0xf1));
        values[0xf1] = RETURN_VOID_BARRIER;
        IGET_QUICK = new Opcode<>(0xf2, 4, "iget-quick", SectionType.FIELD_ID, new Ins22csCreator(0xf2));
        values[0xf2] = IGET_QUICK;
        IGET_WIDE_QUICK = new Opcode<>(0xf3, 4, "iget-wide-quick", SectionType.FIELD_ID, new Ins22csCreator(0xf3));
        values[0xf3] = IGET_WIDE_QUICK;
        IGET_OBJECT_QUICK = new Opcode<>(0xf4, 4, "iget-object-quick", SectionType.FIELD_ID, new Ins22csCreator(0xf4));
        values[0xf4] = IGET_OBJECT_QUICK;
        IPUT_QUICK = new Opcode<>(0xf5, 4, "iput-quick", SectionType.FIELD_ID, new Ins22csPutCreator(0xf5));
        values[0xf5] = IPUT_QUICK;
        IPUT_WIDE_QUICK = new Opcode<>(0xf6, 4, "iput-wide-quick", SectionType.FIELD_ID, new Ins22csPutCreator(0xf6));
        values[0xf6] = IPUT_WIDE_QUICK;
        IPUT_OBJECT_QUICK = new Opcode<>(0xf7, 4, "iput-object-quick", SectionType.FIELD_ID, new Ins22csPutCreator(0xf7));
        values[0xf7] = IPUT_OBJECT_QUICK;
        INVOKE_VIRTUAL_QUICK = new Opcode<>(0xf8, 6, "invoke-virtual-quick", SectionType.METHOD_ID, new Ins35msCreator(0xf8));
        values[0xf8] = INVOKE_VIRTUAL_QUICK;
        INVOKE_VIRTUAL_QUICK_RANGE = new Opcode<>(0xf9, 6, "invoke-virtual-quick/range", SectionType.METHOD_ID, new Ins3rmsCreator(0xf9));
        values[0xf9] = INVOKE_VIRTUAL_QUICK_RANGE;
        INVOKE_SUPER_QUICK = new Opcode<>(0xfa, 6, "invoke-super-quick", SectionType.METHOD_ID, new Ins35msCreator(0xfa));
        values[0xfa] = INVOKE_SUPER_QUICK;
        INVOKE_SUPER_QUICK_RANGE = new Opcode<>(0xfb, 6, "invoke-super-quick/range", SectionType.METHOD_ID, new Ins3rmsCreator(0xfb));
        values[0xfb] = INVOKE_SUPER_QUICK_RANGE;



        INVOKE_CUSTOM = new Opcode<>(0xfc, 6, "invoke-custom", SectionType.CALL_SITE_ID, new Ins35cCreator(0xfc));
        values[0xfc] = INVOKE_CUSTOM;
        INVOKE_CUSTOM_RANGE = new Opcode<>(0xfd, 6, "invoke-custom/range", SectionType.CALL_SITE_ID, new Ins3rcCreator(0xfd));
        values[0xfd] = INVOKE_CUSTOM_RANGE;

        SPUT_OBJECT_VOLATILE = new Opcode<>(0xfe, 4, "sput-object-volatile", SectionType.FIELD_ID, new Ins21cPutCreator(0xfe));
        values[0xfe] = SPUT_OBJECT_VOLATILE;
        CONST_METHOD_TYPE = new Opcode<>(0xff, 4, "const-method-type", SectionType.METHOD_ID, new Ins21cCreator(0xff));
        values[0xff] = CONST_METHOD_TYPE;


        PACKED_SWITCH_PAYLOAD = new Opcode<>(0x100, -1, "packed-switch-payload", new InsPackedSwitchDataCreator(0x100));
        PAYLOADS[0] = PACKED_SWITCH_PAYLOAD;
        SPARSE_SWITCH_PAYLOAD = new Opcode<>(0x200, -1, "sparse-switch-payload", new InsSparseSwitchDataCreator(0x200));
        PAYLOADS[1] = SPARSE_SWITCH_PAYLOAD;
        ARRAY_PAYLOAD = new Opcode<>(0x300, -1, "array-data", new InsArrayDataCreator(0x300));
        PAYLOADS[2] = ARRAY_PAYLOAD;


        IPUT_BOOLEAN_QUICK = new Opcode<>(0xeb, 4, "iput-boolean-quick", SectionType.FIELD_ID, new Ins22csPutCreator(0xeb));
        VALUES_2[0] = IPUT_BOOLEAN_QUICK;
        IPUT_CHAR_QUICK = new Opcode<>(0xed, 4, "iput-char-quick", SectionType.FIELD_ID, new Ins22csPutCreator(0xed));
        VALUES_2[1] = IPUT_CHAR_QUICK;
        IPUT_SHORT_QUICK = new Opcode<>(0xee, 4, "iput-short-quick", SectionType.FIELD_ID, new Ins22csPutCreator(0xee));
        VALUES_2[2] = IPUT_SHORT_QUICK;
        IGET_BOOLEAN_QUICK = new Opcode<>(0xef, 4, "iget-boolean-quick", SectionType.FIELD_ID, new Ins22csCreator(0xef));
        VALUES_2[3] = IGET_BOOLEAN_QUICK;
        INVOKE_OBJECT_INIT_RANGE = new Opcode<>(0xf0, 6, "invoke-object-init/range", SectionType.METHOD_ID, new Ins3rcCreator(0xf0));
        VALUES_2[4] = INVOKE_OBJECT_INIT_RANGE;
        IGET_CHAR_QUICK = new Opcode<>(0xf1, 4, "iget-char-quick", SectionType.FIELD_ID, new Ins22csCreator(0xf1));
        VALUES_2[5] = IGET_CHAR_QUICK;
        IGET_SHORT_QUICK = new Opcode<>(0xf2, 4, "iget-short-quick", SectionType.FIELD_ID, new Ins22csCreator(0xf2));
        VALUES_2[6] = IGET_SHORT_QUICK;
        INVOKE_POLYMORPHIC = new Opcode<>(0xfa, 8, "invoke-polymorphic", SectionType.METHOD_ID, new Ins45ccCreator(0xfa));
        VALUES_2[7] = INVOKE_POLYMORPHIC;
        INVOKE_POLYMORPHIC_RANGE = new Opcode<>(0xfb, 8, "invoke-polymorphic/range", SectionType.METHOD_ID, new Ins4rccCreator(0xfb));
        VALUES_2[8] = INVOKE_POLYMORPHIC_RANGE;


        IPUT_OBJECT_VOLATILE = new Opcode<>(0xfc, 4, "iput-object-volatile", SectionType.FIELD_ID, new Ins22cCreator(0xfc));
        VALUES_2[9] = IPUT_OBJECT_VOLATILE;
        SGET_OBJECT_VOLATILE = new Opcode<>(0xfd, 4, "sget-object-volatile", SectionType.FIELD_ID, new Ins21cCreator(0xfd));
        VALUES_2[10] = SGET_OBJECT_VOLATILE;

        CONST_METHOD_HANDLE = new Opcode<>(0xfe, 4, "const-method-handle", SectionType.METHOD_HANDLE, new Ins21cCreator(0xfe));
        VALUES_2[11] = CONST_METHOD_HANDLE;


        IGET_BYTE_QUICK = new Opcode<>(0xf0, 4, "iget-byte-quick", SectionType.FIELD_ID, new Ins22csCreator(0xf0));
        VALUES_3[0] = IGET_BYTE_QUICK;

        for(Opcode<?> opcode : values){
            if(opcode == null){
                continue;
            }
            map.put(opcode.name, opcode);
        }

        for(Opcode<?> opcode : PAYLOADS){
            map.put(opcode.name, opcode);
        }

        for(Opcode<?> opcode : VALUES_2){
            map.put(opcode.name, opcode);
        }

        for(Opcode<?> opcode : VALUES_3){
            map.put(opcode.name, opcode);
        }

    }

    private final int value;
    private final int size;
    private final String name;
    private final SectionType<? extends IdItem> sectionType;
    private final InsCreator<T> creator;

    private Opcode(int value, int size, String name, SectionType<? extends IdItem> sectionType, InsCreator<T> creator){
        this.value = value;
        this.size = size;
        this.name = name;
        this.sectionType = sectionType;
        this.creator = creator;
    }
    private Opcode(int value, int size, String name, InsCreator<T> creator){
        this(value, size, name, null, creator);
    }


    public int getValue() {
        return value;
    }
    public int size() {
        return size;
    }
    public String getName() {
        return name;
    }
    public boolean hasOutRegisters() {
        SectionType<?> sectionType = getSectionType();
        if (sectionType == SectionType.METHOD_ID || sectionType == SectionType.CALL_SITE_ID) {
            return true;
        }
        return this == FILLED_NEW_ARRAY || this == FILLED_NEW_ARRAY_RANGE;
    }
    public boolean isFieldAccess(){
        return getSectionType() == SectionType.FIELD_ID;
    }
    public boolean isFieldGet(){
        if(getSectionType() != SectionType.FIELD_ID){
            return false;
        }
        return getName().charAt(1) == 'g';
    }
    public boolean isFieldPut(){
        if(getSectionType() != SectionType.FIELD_ID){
            return false;
        }
        return getName().charAt(1) == 'p';
    }
    public boolean isFieldAccessStatic(){
        if(getSectionType() != SectionType.FIELD_ID){
            return false;
        }
        return getName().charAt(0) == 's';
    }
    public boolean isFieldAccessVirtual(){
        if(getSectionType() != SectionType.FIELD_ID){
            return false;
        }
        return getName().charAt(0) == 'i';
    }
    public boolean isFieldStaticGet(){
        if(getSectionType() != SectionType.FIELD_ID){
            return false;
        }
        String name = getName();
        return name.charAt(0) == 's' && name.charAt(1) == 'g';
    }
    public boolean isFieldStaticPut(){
        if(getSectionType() != SectionType.FIELD_ID){
            return false;
        }
        String name = getName();
        return name.charAt(0) == 's' && name.charAt(1) == 'p';
    }
    public boolean isFieldVirtualGet(){
        if(getSectionType() != SectionType.FIELD_ID){
            return false;
        }
        String name = getName();
        return name.charAt(0) == 'i' && name.charAt(1) == 'g';
    }
    public boolean isFieldVirtualPut(){
        if(getSectionType() != SectionType.FIELD_ID){
            return false;
        }
        String name = getName();
        return name.charAt(0) == 'i' && name.charAt(1) == 'p';
    }
    public boolean isMethodInvoke(){
        return getSectionType() == SectionType.METHOD_ID;
    }
    public boolean isMethodInvokeStatic(){
        if(getSectionType() != SectionType.METHOD_ID){
            return false;
        }
        return getName().charAt(8) == 't';
    }
    public boolean isReturning(){
        String name = getName();
        return name.charAt(0) == 'r' &&
                name.charAt(2) == 't';
    }
    public boolean isMoveResultValue(){
        String name = getName();
        if(name.length() < 6){
            return false;
        }
        return name.charAt(0) == 'm' &&
                name.charAt(5) == 'r';
    }
    public boolean isMover(){
        Opcode<?> opcode = this;
        return opcode == MOVE ||
                opcode == MOVE_16 ||
                opcode == MOVE_FROM16 ||
                opcode == MOVE_OBJECT ||
                opcode == MOVE_OBJECT_16 ||
                opcode == MOVE_WIDE ||
                opcode == MOVE_WIDE_16 ||
                opcode == MOVE_WIDE_FROM16;
    }
    public SectionType<? extends IdItem> getSectionType(){
        return sectionType;
    }
    public RegisterFormat getRegisterFormat(){
        return creator.getRegisterFormat();
    }
    public OperandType getOperandType(){
        return creator.getOperandType();
    }
    @Override
    public T newInstance(){
        return creator.newInstance();
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getName());
        writer.append(' ');
    }
    @Override
    public String toString() {
        return getName();
    }

    public static Opcode<?> valueOf(int value){
        if(value <= 0xff){
            return VALUES[value];
        }
        for (Opcode<?> opcode : PAYLOADS){
            if(value == opcode.value){
                return opcode;
            }
        }
        return null;
    }
    public static Opcode<?> valueOf(String name){
        return NAME_MAP.get(name);
    }

    public static Iterator<Opcode<?>> values(){
        return CombiningIterator.four(
                new ArrayIterator<>(VALUES),
                new ArrayIterator<>(PAYLOADS),
                new ArrayIterator<>(VALUES_2),
                new ArrayIterator<>(VALUES_3));
    }
    public static Opcode<?> read(BlockReader reader) throws IOException {
        int value = reader.read();
        if(value == 0){
            value = reader.read() << 8;
            reader.offset(-1);
        }
        reader.offset(-1);
        return valueOf(value);
    }

    public static Opcode<?> parseSmali(SmaliReader reader, boolean skip) {
        reader.skipWhitespaces();
        if(!isPrefix(reader.get())){
            return null;
        }
        int i = reader.indexOfWhiteSpaceOrComment();
        i = i - reader.position();
        String name;
        if(skip){
            name = reader.readString(i);
        }else {
            name = reader.getString(i);
        }
        return valueOf(name);
    }
    public static Opcode<?> parse(String line){
        return parse(0, line);
    }
    public static Opcode<?> parse(int start, String smali){
        int i1 = StringsUtil.skipWhitespace(start, smali);
        if(i1 == smali.length()){
            return null;
        }
        int i2 = smali.indexOf(' ', i1 + 1);
        if(i2 < i1){
            return null;
        }
        return valueOf(smali.substring(i1, i2));
    }
    public static boolean isPrefix(byte b) {
        switch (b){
            case 'a':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'i':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'x':
                return true;
            default:
                return false;
        }
    }
    static abstract class InsCreator<T extends Ins> implements BlockCreator<T> {
        private final int opcodeValue;
        private Opcode<?> opcode;
        InsCreator(int opcodeValue){
            this.opcodeValue = opcodeValue;
        }
        Opcode<?> getOpcode(){
            if(opcode == null){
                opcode = Opcode.valueOf(opcodeValue);
            }
            return opcode;
        }
        public abstract RegisterFormat getRegisterFormat();
        public abstract OperandType getOperandType();
    }

    static class Ins10xCreator extends InsCreator<Ins10x> {
        Ins10xCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.NONE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.NONE;
        }
        @Override
        public Ins10x newInstance() {
            return new Ins10x(getOpcode());
        }
    }
    static class Ins11xCreator extends InsCreator<Ins11x> {
        Ins11xCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.NONE;
        }
        @Override
        public Ins11x newInstance() {
            return new Ins11x(getOpcode());
        }
    }
    static class Ins11xWriteCreator extends Ins11xCreator {
        Ins11xWriteCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE;
        }
    }
    static class Ins12xCreator extends InsCreator<Ins12x> {
        Ins12xCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE_READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.NONE;
        }
        @Override
        public Ins12x newInstance() {
            return new Ins12x(getOpcode());
        }
    }
    static class Ins20bcCreator extends InsCreator<Ins20bc> {
        Ins20bcCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.NONE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.NONE;
        }
        @Override
        public Ins20bc newInstance() {
            return new Ins20bc(getOpcode());
        }
    }
    static class Ins21cCreator extends InsCreator<Ins21c> {
        Ins21cCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.KEY;
        }
        @Override
        public Ins21c newInstance() {
            return new Ins21c(getOpcode());
        }
    }
    static class Ins21cPutCreator extends Ins21cCreator {
        Ins21cPutCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.READ;
        }
    }
    static class InsConstWideHigh16Creator extends InsCreator<InsConstWideHigh16> {
        InsConstWideHigh16Creator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.HEX;
        }
        @Override
        public InsConstWideHigh16 newInstance() {
            return new InsConstWideHigh16();
        }
        @Override
        Opcode<InsConstWideHigh16> getOpcode() {
            return Opcode.CONST_WIDE_HIGH16;
        }
    }
    static class Ins21sCreator extends InsCreator<Ins21s> {
        Ins21sCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.HEX;
        }
        @Override
        public Ins21s newInstance() {
            return new Ins21s(getOpcode());
        }
    }

    static class InsConstWide16Creator extends InsCreator<InsConstWide16> {
        InsConstWide16Creator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.HEX;
        }
        @Override
        public InsConstWide16 newInstance() {
            return new InsConstWide16();
        }
        @Override
        Opcode<?> getOpcode() {
            return Opcode.CONST_WIDE_16;
        }
    }
    static class InsConst16Creator extends InsCreator<InsConst16> {

        InsConst16Creator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.HEX;
        }
        @Override
        public InsConst16 newInstance() {
            return new InsConst16();
        }
        @Override
        Opcode<InsConst16> getOpcode() {
            return Opcode.CONST_16;
        }
    }
    static class Ins21tCreator extends InsCreator<Ins21t> {
        Ins21tCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.LABEL;
        }
        @Override
        public Ins21t newInstance() {
            return new Ins21t(getOpcode());
        }
    }
    static class Ins22bCreator extends InsCreator<Ins22b> {
        Ins22bCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE_READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.HEX;
        }
        @Override
        public Ins22b newInstance() {
            return new Ins22b(getOpcode());
        }
    }
    static class Ins22cCreator extends InsCreator<Ins22c> {
        Ins22cCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE_READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.KEY;
        }
        @Override
        public Ins22c newInstance() {
            return new Ins22c(getOpcode());
        }
    }
    static class Ins22cPutCreator extends Ins22cCreator {
        Ins22cPutCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.READ_READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.KEY;
        }
    }
    static class Ins22csCreator extends InsCreator<Ins22cs> {
        Ins22csCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE_READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.KEY;
        }
        @Override
        public Ins22cs newInstance() {
            return new Ins22cs(getOpcode());
        }
    }
    static class Ins22csPutCreator extends Ins22csCreator {
        Ins22csPutCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.READ_READ;
        }
    }
    static class Ins22sCreator extends InsCreator<Ins22s> {
        Ins22sCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE_READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.HEX;
        }
        @Override
        public Ins22s newInstance() {
            return new Ins22s(getOpcode());
        }
    }
    static class Ins22tCreator extends InsCreator<Ins22t> {
        Ins22tCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE_READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.LABEL;
        }
        @Override
        public Ins22t newInstance() {
            return new Ins22t(getOpcode());
        }
    }
    static class Ins22xCreator extends InsCreator<Ins22x> {
        Ins22xCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE_READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.NONE;
        }
        @Override
        public Ins22x newInstance() {
            return new Ins22x(getOpcode());
        }
    }
    static class Ins23xCreator extends InsCreator<Ins23x> {
        Ins23xCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE_READ_READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.NONE;
        }
        @Override
        public Ins23x newInstance() {
            return new Ins23x(getOpcode());
        }
    }
    static class Ins23xAputCreator extends Ins23xCreator {
        Ins23xAputCreator(int opcodeValue){
            super(opcodeValue);
        }
        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.READ_READ_READ;
        }
    }
    static class Ins31iCreator extends InsCreator<Ins31i> {
        Ins31iCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.HEX;
        }
        @Override
        public Ins31i newInstance() {
            return new Ins31i(getOpcode());
        }
    }
    static class InsConstWide32Creator extends InsCreator<InsConstWide32> {
        InsConstWide32Creator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.HEX;
        }
        @Override
        public InsConstWide32 newInstance() {
            return new InsConstWide32();
        }
        @Override
        Opcode<InsConstWide32> getOpcode() {
            return Opcode.CONST_WIDE_32;
        }
    }
    static class Ins32xCreator extends InsCreator<Ins32x> {
        Ins32xCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE_READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.NONE;
        }
        @Override
        public Ins32x newInstance() {
            return new Ins32x(getOpcode());
        }
    }
    static class Ins35cCreator extends InsCreator<Ins35c> {
        Ins35cCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.OUT;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.KEY;
        }
        @Override
        public Ins35c newInstance() {
            return new Ins35c(getOpcode());
        }
    }
    static class Ins35miCreator extends InsCreator<Ins35mi> {
        Ins35miCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE_READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.NONE;
        }
        @Override
        public Ins35mi newInstance() {
            return new Ins35mi(getOpcode());
        }
    }
    static class Ins35msCreator extends InsCreator<Ins35ms> {
        Ins35msCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.OUT;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.KEY;
        }
        @Override
        public Ins35ms newInstance() {
            return new Ins35ms(getOpcode());
        }
    }
    static class Ins3rcCreator extends InsCreator<Ins3rc> {
        Ins3rcCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.OUT_RANGE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.KEY;
        }
        @Override
        public Ins3rc newInstance() {
            return new Ins3rc(getOpcode());
        }
    }
    static class Ins3rmiCreator extends InsCreator<Ins3rmi> {
        Ins3rmiCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE_READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.NONE;
        }
        @Override
        public Ins3rmi newInstance() {
            return new Ins3rmi(getOpcode());
        }
    }
    static class Ins3rmsCreator extends InsCreator<Ins3rms> {
        Ins3rmsCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.OUT_RANGE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.KEY;
        }
        @Override
        public Ins3rms newInstance() {
            return new Ins3rms(getOpcode());
        }
    }
    static class Ins45ccCreator extends InsCreator<Ins45cc> {
        Ins45ccCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.OUT;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.KEY;
        }
        @Override
        public Ins45cc newInstance() {
            return new Ins45cc(getOpcode());
        }
    }
    static class Ins4rccCreator extends InsCreator<Ins4rcc> {
        Ins4rccCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.OUT_RANGE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.KEY;
        }
        @Override
        public Ins4rcc newInstance() {
            return new Ins4rcc(getOpcode());
        }
    }
    static class InsArrayDataCreator extends InsCreator<InsArrayData> {
        InsArrayDataCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.NONE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.DECIMAL;
        }
        @Override
        public InsArrayData newInstance() {
            return new InsArrayData();
        }
    }
    static class InsConst16HighCreator extends InsCreator<InsConst16High> {
        InsConst16HighCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.HEX;
        }
        @Override
        public InsConst16High newInstance() {
            return new InsConst16High();
        }
    }
    static class InsConst4Creator extends InsCreator<InsConst4> {
        InsConst4Creator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.HEX;
        }
        @Override
        public InsConst4 newInstance() {
            return new InsConst4();
        }
    }
    static class InsConstCreator extends InsCreator<InsConst> {
        InsConstCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.HEX;
        }
        @Override
        public InsConst newInstance() {
            return new InsConst();
        }
    }
    static class InsConstStringCreator extends InsCreator<InsConstString> {
        InsConstStringCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.KEY;
        }
        @Override
        public InsConstString newInstance() {
            return new InsConstString();
        }
    }
    static class InsConstStringJumboCreator extends InsCreator<InsConstStringJumbo> {
        InsConstStringJumboCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.KEY;
        }
        @Override
        public InsConstStringJumbo newInstance() {
            return new InsConstStringJumbo();
        }
    }
    static class InsConstWideCreator extends InsCreator<InsConstWide> {
        InsConstWideCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.WRITE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.HEX;
        }
        @Override
        public InsConstWide newInstance() {
            return new InsConstWide();
        }
    }
    static class InsFillArrayDataCreator extends InsCreator<InsFillArrayData> {
        InsFillArrayDataCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.LABEL;
        }
        @Override
        public InsFillArrayData newInstance() {
            return new InsFillArrayData();
        }
    }
    static class InsGotoCreator extends InsCreator<InsGoto> {
        InsGotoCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.NONE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.LABEL;
        }
        @Override
        public InsGoto newInstance() {
            return new InsGoto(getOpcode());
        }
    }
    static class InsNopCreator extends InsCreator<InsNop> {
        InsNopCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.NONE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.NONE;
        }
        @Override
        public InsNop newInstance() {
            return new InsNop();
        }
    }
    static class InsPackedSwitchCreator extends InsCreator<InsPackedSwitch> {
        InsPackedSwitchCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.LABEL;
        }
        @Override
        public InsPackedSwitch newInstance() {
            return new InsPackedSwitch();
        }
    }
    static class InsPackedSwitchDataCreator extends InsCreator<InsPackedSwitchData> {
        InsPackedSwitchDataCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.NONE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.HEX;
        }
        @Override
        public InsPackedSwitchData newInstance() {
            return new InsPackedSwitchData();
        }
    }
    static class InsSparseSwitchCreator extends InsCreator<InsSparseSwitch> {
        InsSparseSwitchCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.READ;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.LABEL;
        }
        @Override
        public InsSparseSwitch newInstance() {
            return new InsSparseSwitch();
        }
    }
    static class InsSparseSwitchDataCreator extends InsCreator<InsSparseSwitchData> {
        InsSparseSwitchDataCreator(int opcodeValue){
            super(opcodeValue);
        }

        @Override
        public RegisterFormat getRegisterFormat() {
            return RegisterFormat.NONE;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.NONE;
        }
        @Override
        public InsSparseSwitchData newInstance() {
            return new InsSparseSwitchData();
        }
    }

}