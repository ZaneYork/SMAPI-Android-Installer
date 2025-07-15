package com.reandroid.arsc.value.bag;

import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.arsc.value.ValueType;

public abstract class BagItem {
    protected final ResValueMap mBagItem;
    private final ValueType valueType;
    private final int data;
    private final StringItem string;

    protected BagItem(ResValueMap bagItem) {
        this.mBagItem = bagItem;
        this.valueType = null;
        this.string = null;
        this.data = 0;
    }

    protected BagItem(ValueType valueType, int data) {
        if (valueType == ValueType.STRING) {
            throw new IllegalArgumentException("Use the string constructor instead");
        }
        this.mBagItem = null;
        this.string = null;
        this.valueType = valueType;
        this.data = data;
    }

    protected BagItem(StringItem str) {
        this.string = str;
        this.mBagItem = null;
        this.valueType = ValueType.STRING;
        this.data = 0;
    }

    public ValueType getValueType() {
        if (mBagItem != null) {
            return mBagItem.getValueType();
        }
        return valueType;
    }

    public int getValue() {
        if (mBagItem != null) {
            return mBagItem.getData();
        } else if (valueType == ValueType.STRING) {
            return string.getIndex();
        } else {
            return data;
        }
    }

    public void copyTo(ResValueMap target) {
        if (mBagItem != null) {
            target.setTypeAndData(mBagItem.getValueType(), mBagItem.getData());
        } else if (valueType == ValueType.STRING) {
            TableStringPool targetStrPool = (TableStringPool) target.getStringPool();
            if (targetStrPool == string.getParent(TableStringPool.class)) {
                target.setTypeAndData(ValueType.STRING, string.getIndex());
            } else {
                target.setTypeAndData(ValueType.STRING, targetStrPool.getOrCreate(string.get()).getIndex());
            }
        } else {
            target.setTypeAndData(valueType, data);
        }
    }

    public ResValueMap getBagItem() {
        return mBagItem;
    }

    public boolean hasStringValue() {
        return getValueType() == ValueType.STRING;
    }

    public boolean hasReferenceValue() {
        return getValueType() == ValueType.REFERENCE;
    }

    public String getStringValue() {
        if (mBagItem != null) {
            return mBagItem.getValueAsString();
        } else if (valueType == ValueType.STRING) {
            return string.getHtml();
        } else {
            throw new IllegalArgumentException("Not a string");
        }
    }
}
