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
package com.reandroid.arsc.chunk;

import android.text.TextUtils;

import com.reandroid.apk.xmlencoder.EncodeException;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PolicyFlag {

    public static final String ATTR_type;

    public static final PolicyFlag PUBLIC;
    public static final PolicyFlag SYSTEM_PARTITION;
    public static final PolicyFlag VENDOR_PARTITION;
    public static final PolicyFlag PRODUCT_PARTITION;
    public static final PolicyFlag SIGNATURE;
    public static final PolicyFlag ODM_PARTITION;
    public static final PolicyFlag OEM_PARTITION;
    public static final PolicyFlag ACTOR_SIGNATURE;
    public static final PolicyFlag CONFIG_SIGNATURE;

    private static final PolicyFlag[] VALUES;
    private static final Map<String, PolicyFlag> nameMap;

    static {

        ATTR_type = ObjectsUtil.of("type");

        PUBLIC = new PolicyFlag(0x00000001, "public");
        SYSTEM_PARTITION = new PolicyFlag(0x00000002, "system");
        VENDOR_PARTITION = new PolicyFlag(0x00000004, "vendor");
        PRODUCT_PARTITION = new PolicyFlag(0x00000008, "product");
        SIGNATURE = new PolicyFlag(0x00000010, "signature");
        ODM_PARTITION = new PolicyFlag(0x00000020, "odm");
        OEM_PARTITION = new PolicyFlag(0x00000040, "oem");
        ACTOR_SIGNATURE = new PolicyFlag(0x00000080, "actor");
        CONFIG_SIGNATURE = new PolicyFlag(0x00000100, "config");

        PolicyFlag[] values = new PolicyFlag[]{
                PUBLIC,
                SYSTEM_PARTITION,
                VENDOR_PARTITION,
                PRODUCT_PARTITION,
                SIGNATURE,
                ODM_PARTITION,
                OEM_PARTITION,
                ACTOR_SIGNATURE,
                CONFIG_SIGNATURE
        };
        VALUES = values;
        Map<String, PolicyFlag> map = new HashMap<>();
        nameMap = map;
        for (PolicyFlag policyFlag : values) {
            map.put(policyFlag.name(), policyFlag);
        }
    }

    private final int flag;
    private final String name;

    private PolicyFlag(int flag, String name) {
        this.flag = flag;
        this.name = name;
    }

    public int flag() {
        return this.flag;
    }

    public String name() {
        return name;
    }

    public boolean isSet(int flagsValue) {
        int f = this.flag();
        return (f & flagsValue) == f;
    }

    public static PolicyFlag[] valuesOf(int flagValue) {
        if (flagValue == 0) {
            return null;
        }
        PolicyFlag[] values = VALUES;
        PolicyFlag[] tmp = new PolicyFlag[values.length];
        int count = 0;
        for (int i = 0; i < values.length; i++) {
            PolicyFlag flag = values[i];
            if (flag.isSet(flagValue)) {
                tmp[i] = flag;
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        if (count == tmp.length) {
            return tmp;
        }
        PolicyFlag[] results = new PolicyFlag[count];
        int j = 0;
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i] != null) {
                results[j] = tmp[i];
                j++;
            }
        }
        return results;
    }

    public static int sum(PolicyFlag[] flagsList) {
        if (flagsList == null || flagsList.length == 0) {
            return 0;
        }
        int results = 0;
        for (PolicyFlag flags : flagsList) {
            if (flags != null) {
                results |= flags.flag();
            }
        }
        return results;
    }

    public static boolean contains(PolicyFlag[] flagsList, PolicyFlag policyFlag) {
        if (flagsList == null || flagsList.length == 0) {
            return policyFlag == null;
        }
        if (policyFlag == null) {
            return false;
        }
        for (PolicyFlag flags : flagsList) {
            if (policyFlag.equals(flags)) {
                return true;
            }
        }
        return false;
    }

    public static int parse(XmlPullParser parser) throws IOException {
        String policyFlags = parser.getAttributeValue(null, ATTR_type);
        if (TextUtils.isEmpty(policyFlags)) {
            return 0;
        }

        int result = 0;

        String[] flagNames = StringsUtil.split(policyFlags, '|');
        for (String name : flagNames) {
            name = name.trim();
            if (name.length() != 0) {
                PolicyFlag flag = nameOf(name);
                if (flag == null) {
                    throw new EncodeException("Unknown policy flag '" + name
                            + "' " + parser.getPositionDescription());
                }
                result |= flag.flag();
            }
        }
        return result;
    }
    public static void serialize(XmlSerializer serializer, int policyFlags) throws IOException {
        String type = toString(policyFlags);
        if (!TextUtils.isEmpty(type)) {
            serializer.attribute(null, ATTR_type, type);
        }
    }
    public static String toString(int policyFlags) {
        if (policyFlags == 0) {
            return StringsUtil.EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        boolean appendOnce = false;
        for (PolicyFlag policyFlag : VALUES) {
            if (policyFlag.isSet(policyFlags)) {
                if (appendOnce) {
                    builder.append('|');
                }
                builder.append(policyFlag.name());
                appendOnce = true;
            }
        }
        return builder.toString();
    }

    public static String toString(PolicyFlag[] flagsList) {
        if (flagsList == null || flagsList.length == 0) {
            return StringsUtil.EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        boolean appendOnce = false;
        for (PolicyFlag flags : flagsList) {
            if (flags == null) {
                continue;
            }
            if (appendOnce) {
                builder.append('|');
            }
            builder.append(flags.name());
            appendOnce = true;
        }
        return builder.toString();
    }

    public static PolicyFlag[] valuesOf(String flagsString) {
        if (flagsString == null) {
            return null;
        }
        flagsString = flagsString.trim();
        String[] namesList = flagsString.split("\\s*\\|\\s*");
        PolicyFlag[] tmp = new PolicyFlag[namesList.length];
        int count = 0;
        for (int i = 0; i < namesList.length; i++) {
            PolicyFlag flags = nameOf(namesList[i]);
            if (flags != null) {
                tmp[i] = flags;
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        if (count == tmp.length) {
            return tmp;
        }
        PolicyFlag[] results = new PolicyFlag[count];
        int j = 0;
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i] != null) {
                results[j] = tmp[i];
                j++;
            }
        }
        return results;
    }

    public static PolicyFlag nameOf(String name) {
        return nameMap.get(name);
    }
}
