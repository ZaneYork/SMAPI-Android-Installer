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
package com.reandroid.archive.block;

import com.reandroid.arsc.coder.CoderHex;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.utils.HexUtil;

import java.util.Objects;

public class SignatureId implements Comparable<SignatureId>{
    private final String name;
    private final int id;
    private final int sort;

    private SignatureId(String name, int id, int sort) {
        this.name = name;
        this.id = id;
        this.sort = sort;
    }
    public String name() {
        return name;
    }
    public int getId() {
        return id;
    }
    public String toFileName() {
        if (this.name != null) {
            return name + FILE_EXT_RAW;
        }
        return HexUtil.toHex8(id) + FILE_EXT_RAW;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SignatureId that = (SignatureId) obj;
        return id == that.id;
    }
    @Override
    public int compareTo(SignatureId signatureId) {
        return Integer.compare(sort, signatureId.sort);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    @Override
    public String toString() {
        String name = this.name;
        if (name != null) {
            return name;
        }
        return "UNKNOWN(" + HexUtil.toHex8(id) + ")";
    }
    public static SignatureId valueOf(String name) {
        if (name == null) {
            return null;
        }
        String ext = FILE_EXT_RAW;
        if (name.endsWith(ext)) {
            name = name.substring(0, name.length() - ext.length());
        }
        for (SignatureId signatureId : VALUES) {
            if (name.equalsIgnoreCase(signatureId.name())) {
                return signatureId;
            }
        }
        EncodeResult hex = CoderHex.INS.encode(name);
        if (hex != null) {
            return new SignatureId(null, hex.value, 99);
        }
        return null;
    }
    public static SignatureId valueOf(int id) {
        for (SignatureId signatureId : VALUES) {
            if (id == signatureId.getId()) {
                return signatureId;
            }
        }
        return new SignatureId(null, id, 99);
    }

    public static SignatureId[] values() {
        return VALUES.clone();
    }

    public static final SignatureId V2 = new SignatureId("V2", 0x7109871A, 0);
    public static final SignatureId V3 = new SignatureId("V3", 0xF05368C0, 1);
    public static final SignatureId V31 = new SignatureId("V31", 0x1B93AD61, 2);
    public static final SignatureId STAMP_V1 = new SignatureId("STAMP_V1", 0x2B09189E, 3);
    public static final SignatureId STAMP_V2 = new SignatureId("STAMP_V2", 0x6DFF800D, 4);
    public static final SignatureId PADDING = new SignatureId("PADDING", 0x42726577, 9999);
    public static final SignatureId NULL = new SignatureId("NULL", 0x0, 999);

    private static final SignatureId[] VALUES = new SignatureId[]{
            V2, V3, V31, STAMP_V1, STAMP_V2, PADDING, NULL
    };

    public static final String FILE_EXT_RAW = ".signature.info.bin";
}
