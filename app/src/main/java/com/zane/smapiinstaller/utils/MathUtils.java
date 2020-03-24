package com.zane.smapiinstaller.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class MathUtils {

    /**
     * Returns the value of the {@code long} argument;
     * throwing an exception if the value overflows an {@code int}.
     *
     * @param value the long value
     * @return the argument as an int
     * @throws ArithmeticException if the {@code argument} overflows an int
     * @since  1.8
     */
    public static int toIntExact(long value) {
        if ((int)value != value) {
            throw new ArithmeticException("integer overflow");
        }
        return (int)value;
    }

    /**
     * Converts this {@code BigInteger} to an {@code int}, checking
     * for lost information.  If the value of this {@code BigInteger}
     * is out of the range of the {@code int} type, then an
     * {@code ArithmeticException} is thrown.
     *
     * @return this {@code BigInteger} converted to an {@code int}.
     * @throws ArithmeticException if the value of {@code this} will
     * not exactly fit in a {@code int}.
     * @see BigInteger#intValue
     * @since  1.8
     */
    public static int intValueExact(BigInteger value) {
        if (value.bitLength() <= 31) {
            return value.intValue();
        } else {
            throw new ArithmeticException("BigInteger out of int range");
        }
    }

    /**
     * Converts this {@code BigInteger} to a {@code long}, checking
     * for lost information.  If the value of this {@code BigInteger}
     * is out of the range of the {@code long} type, then an
     * {@code ArithmeticException} is thrown.
     *
     * @return this {@code BigInteger} converted to a {@code long}.
     * @throws ArithmeticException if the value of {@code this} will
     * not exactly fit in a {@code long}.
     * @see BigInteger#longValue
     * @since  1.8
     */
    public static long longValueExact(BigInteger value) {
        if (value.bitLength() <= 63) {
            return value.longValue();
        } else {
            throw new ArithmeticException("BigInteger out of long range");
        }
    }
}
