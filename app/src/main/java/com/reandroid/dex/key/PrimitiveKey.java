package com.reandroid.dex.key;

import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public abstract class PrimitiveKey implements Key {

    public PrimitiveKey() {
    }

    @Override
    public boolean isPrimitiveKey() { return true; }

    public abstract Object getValue();

    public boolean isNumber() { return false; }
    public boolean isBoolean() { return false; }
    public boolean isByte() { return false; }
    public boolean isChar() { return false; }
    public boolean isDouble() { return false; }
    public boolean isFloat() { return false; }
    public boolean isInteger() { return false; }
    public boolean isLong() { return false; }
    public boolean isShort() { return false; }


    @Override
    public abstract int compareTo(Object obj);
    @Override
    public abstract void append(SmaliWriter writer) throws IOException;

    public static PrimitiveKey of(boolean value) {
        PrimitiveKey key;
        if (value) {
            key = TRUE_KEY;
            if (key == null) {
                key = new BooleanKey(true);
                TRUE_KEY = key;
            }
        } else {
            key = FALSE_KEY;
            if (key == null) {
                key = new BooleanKey(false);
                FALSE_KEY = key;
            }
        }
        return key;
    }
    public static PrimitiveKey of(byte value) { return new ByteKey(value); }
    public static PrimitiveKey of(char value) { return new CharKey(value); }
    public static PrimitiveKey of(double value) { return new DoubleKey(value); }
    public static PrimitiveKey of(float value) { return new FloatKey(value); }
    public static PrimitiveKey of(int value) { return new IntegerKey(value); }
    public static PrimitiveKey of(long value) { return new LongKey(value); }
    public static PrimitiveKey of(short value) { return new ShortKey(value); }

    public static abstract class NumberKey extends PrimitiveKey {

        public NumberKey() {
            super();
        }

        @Override
        public abstract Number getValue();

        @Override
        public boolean isNumber() {
            return true;
        }

        @Override
        public String toString() {
            return getValue().toString();
        }
    }

    public static class BooleanKey extends PrimitiveKey {

        private final boolean value;

        public BooleanKey(boolean value) {
            super();
            this.value = value;
        }

        public boolean value() {
            return value;
        }
        @Override
        public boolean isBoolean() {
            return true;
        }
        @Override
        public Boolean getValue() {
            return value();
        }
        @Override
        public int compareTo(Object obj) {
            if (obj == null || obj.getClass() != getClass()) {
                return 0;
            }
            return CompareUtil.compare(value(), ((BooleanKey) obj).value());
        }
        @Override
        public int hashCode() {
            return value() ? 1 : 0;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((BooleanKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.append(value() ? "true" : "false");
        }

        @Override
        public String toString() {
            return value() ? "true" : "false";
        }
    }

    public static class ByteKey extends NumberKey {

        private final byte value;

        public ByteKey(byte value) {
            super();
            this.value = value;
        }

        public byte value() {
            return value;
        }
        @Override
        public Byte getValue() {
            return value;
        }
        @Override
        public boolean isByte() {
            return true;
        }

        @Override
        public int compareTo(Object obj) {
            if (obj == this) {
                return 0;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return 0;
            }
            return Byte.compare(value(), ((ByteKey) obj).value());
        }
        @Override
        public int hashCode() {
            return value();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((ByteKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.appendHex(value());
        }

        @Override
        public String toString() {
            return HexUtil.toSignedHex(value()) + "t";
        }
    }

    public static class CharKey extends PrimitiveKey {

        private final char value;

        public CharKey(char value) {
            super();
            this.value = value;
        }

        public char value() {
            return value;
        }
        @Override
        public Character getValue() {
            return value;
        }
        @Override
        public boolean isChar() {
            return true;
        }

        @Override
        public int compareTo(Object obj) {
            if (obj == this) {
                return 0;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return 0;
            }
            return Character.compare(value(), ((CharKey) obj).value());
        }
        @Override
        public int hashCode() {
            return value();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((CharKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.append(DexUtils.quoteChar(value()));
        }

        @Override
        public String toString() {
            return DexUtils.quoteChar(value());
        }
    }

    public static class DoubleKey extends NumberKey {

        private final double value;

        public DoubleKey(double value) {
            super();
            this.value = value;
        }

        public double value() {
            return value;
        }
        @Override
        public Double getValue() {
            return value;
        }
        @Override
        public boolean isDouble() {
            return true;
        }

        @Override
        public int compareTo(Object obj) {
            if (obj == this) {
                return 0;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return 0;
            }
            return Double.compare(value(), ((DoubleKey) obj).value());
        }
        @Override
        public int hashCode() {
            return Double.hashCode(value());
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((DoubleKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.append(value());
        }

        @Override
        public String toString() {
            return Double.toString(value());
        }
    }

    public static class FloatKey extends NumberKey {

        private final float value;

        public FloatKey(float value) {
            super();
            this.value = value;
        }

        public float value() {
            return value;
        }
        @Override
        public Float getValue() {
            return value;
        }
        @Override
        public boolean isFloat() {
            return true;
        }

        @Override
        public int compareTo(Object obj) {
            if (obj == this) {
                return 0;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return 0;
            }
            return Float.compare(value(), ((FloatKey) obj).value());
        }
        @Override
        public int hashCode() {
            return Float.hashCode(value());
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((FloatKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.append(value());
        }

        @Override
        public String toString() {
            return value() + "f";
        }
    }

    public static class IntegerKey extends NumberKey {

        private final int value;

        public IntegerKey(int value) {
            super();
            this.value = value;
        }

        public int value() {
            return value;
        }
        @Override
        public Integer getValue() {
            return value();
        }
        @Override
        public boolean isInteger() {
            return true;
        }

        @Override
        public int compareTo(Object obj) {
            if (obj == null || obj.getClass() != getClass()) {
                return 0;
            }
            return CompareUtil.compare(value(), ((IntegerKey) obj).value());
        }
        @Override
        public int hashCode() {
            return value();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((IntegerKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.appendHex(value());
        }
    }

    public static class LongKey extends NumberKey {

        private final long value;

        public LongKey(long value) {
            super();
            this.value = value;
        }

        public long value() {
            return value;
        }
        @Override
        public Long getValue() {
            return value;
        }
        @Override
        public boolean isLong() {
            return true;
        }

        @Override
        public int compareTo(Object obj) {
            if (obj == this) {
                return 0;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return 0;
            }
            return Long.compare(value(), ((LongKey) obj).value());
        }
        @Override
        public int hashCode() {
            return Long.hashCode(value());
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((LongKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.appendHex(value());
        }
    }

    public static class ShortKey extends NumberKey {

        private final short value;

        public ShortKey(short value) {
            super();
            this.value = value;
        }

        public short value() {
            return value;
        }
        @Override
        public Short getValue() {
            return value;
        }
        @Override
        public boolean isShort() {
            return true;
        }

        @Override
        public int compareTo(Object obj) {
            if (obj == this) {
                return 0;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return 0;
            }
            return Short.compare(value(), ((ShortKey) obj).value());
        }
        @Override
        public int hashCode() {
            return value();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return value() == ((ShortKey) obj).value();
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.appendHex(value());
        }
        @Override
        public String toString() {
            return HexUtil.toSignedHex(value()) + "S";
        }
    }

    private static PrimitiveKey TRUE_KEY;
    private static PrimitiveKey FALSE_KEY;
}
