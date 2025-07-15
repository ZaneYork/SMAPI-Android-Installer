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
package com.reandroid.arsc.value.style;

import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.arsc.value.bag.MapBag;
import com.reandroid.xml.XMLUtil;

import java.util.List;

public class StyleBag extends MapBag<Integer, StyleBagItem> {
    private StyleBag(com.reandroid.arsc.value.Entry entry) {
        super(entry);
    }

    public String getParentResourceName() {
        int id = getParentId();
        if (id == 0) {
            return null;
        }
        com.reandroid.arsc.value.Entry entry = getEntry();
        if (entry == null) {
            return null;
        }
        ResTableMapEntry tableMapEntry = entry.getResTableMapEntry();
        if(tableMapEntry != null){
            return tableMapEntry.decodeParentId();
        }
        return null;
    }

    public int getParentId() {
        return getTableEntry().getParentId();
    }
    public void setParentId(int id) {
        getTableEntry().setParentId(id);
    }

    public int getResourceId() {
        com.reandroid.arsc.value.Entry entry = getEntry();
        if (entry == null) {
            return 0;
        }
        return entry.getResourceId();
    }

    @Override
    protected StyleBagItem createBagItem(ResValueMap valueMap, boolean copied) {
        if (copied) {
            return StyleBagItem.copyOf(valueMap);
        }
        return StyleBagItem.create(valueMap);
    }

    @Override
    protected ResValueMap newKey(Integer attrId) {
        ResValueMap valueMap = new ResValueMap();
        valueMap.setParent(getMapArray());
        valueMap.setNameId(attrId);
        return valueMap;
    }

    @Override
    protected Integer getKeyFor(ResValueMap valueMap) {
        return valueMap.getNameId();
    }

    public static int resolve(TableBlock tableBlock, String name) {
        return tableBlock.getAttrResource(XMLUtil.splitPrefix(name), XMLUtil.splitName(name)).getResourceId();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        String type = getTypeName();
        builder.append(type);
        builder.append(" name=\"");
        builder.append(getName());
        builder.append("\"");
        String parent = getParentResourceName();
        if (parent != null) {
            builder.append(" parent=\"");
            builder.append(parent);
            builder.append("\"");
        }
        builder.append("\">");
        for (StyleBagItem item : values()) {
            builder.append("\n    ");
            builder.append(item.toString());
        }
        builder.append("\n</");
        builder.append(type);
        builder.append(">");
        return builder.toString();
    }

    /**
     * The result of this is not always 100% accurate,
     * in addition to this use your methods to cross check like type-name == "plurals"
     **/
    public static boolean isStyle(com.reandroid.arsc.value.Entry entry) {
        StyleBag style = create(entry);
        if (style == null) {
            return false;
        }

        TableBlock tableBlock = entry.getPackageBlock().getTableBlock();
        if (tableBlock == null) {
            return false;
        }
        List<ResValueMap> list = style.getMapArray().getChildes();
        if (list.size() == 0) {
            return false;
        }

        for (ResValueMap item : list) {
            if (item == null || tableBlock.getResource(item.getNameId()) == null) {
                return false;
            }
        }
        return true;
    }

    public static StyleBag create(com.reandroid.arsc.value.Entry entry) {
        if (entry == null || !entry.isComplex()) {
            return null;
        }
        return new StyleBag(entry);
    }
}
