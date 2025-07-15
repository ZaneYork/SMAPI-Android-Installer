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
package com.reandroid.utils;

public class HexUtil {
    public static String toSignedHex(long num){
        boolean negative = num < 0;
        String prefix = "0x";
        if(negative){
            num = -num;
            prefix = "-0x";
        }
        return toHex(prefix, num, 1);
    }
    public static String toSignedHex(int num){
        boolean negative = num < 0;
        String prefix = "0x";
        if(negative){
            num = -num;
            prefix = "-0x";
        }
        return toHex(prefix, num, 1);
    }
    public static String toHex2(byte num){
        return toHex((long)(num & 0x00000000000000ffL), 2);
    }
    public static String toHex2(String prefix, byte num){
        return toHex(prefix, (long)(num & 0x00000000000000ffL), 2);
    }
    public static String toHex4(short num){
        return toHex((long)(num & 0x000000000000ffffL), 4);
    }
    public static String toHex8(int num){
        return toHex(num, 8);
    }
    public static String toHex8(long num){
        return toHex(num, 8);
    }
    public static String toHex(int num, int minLength){
        return toHex((0x00000000ffffffffL & num), minLength);
    }
    public static String toHex(long num, int minLength){
        String hex = Long.toHexString(num);
        StringBuilder builder = new StringBuilder();
        builder.append('0');
        builder.append('x');
        int rem = minLength - hex.length();
        for(int i=0; i < rem; i++){
            builder.append('0');
        }
        builder.append(hex);
        return builder.toString();
    }
    public static String toHexNoPrefix8(int num){
        return toHex(null, (0x00000000ffffffffL & num), 8);
    }
    public static String toHexNoPrefix(int num, int minLength){
        return toHex(null, (0x00000000ffffffffL & num), minLength);
    }
    public static String toHex8(String prefix, int num){
        return toHex(prefix, (0x00000000ffffffffL & num), 8);
    }
    public static String toHex(String prefix, int num, int minLength){
        return toHex(prefix, (0x00000000ffffffffL & num), minLength);
    }
    public static String toHex(String prefix, long num, int minLength){
        String hex = Long.toHexString(num);
        StringBuilder builder = new StringBuilder();
        if(prefix != null){
            builder.append(prefix);
        }
        int rem = minLength - hex.length();
        for(int i=0; i < rem; i++){
            builder.append('0');
        }
        builder.append(hex);
        return builder.toString();
    }
    public static String toHexString(byte[] bytes){
        int length = bytes.length;
        StringBuilder builder = new StringBuilder(length * 2);
        for(int i = 0; i < length; i++){
            int b = bytes[i];
            builder.append(toHexChar((b >> 4) & 0xf));
            builder.append(toHexChar(b & 0xf));
        }
        return builder.toString();
    }
    public static byte[] fromHexSting(String hexString){
        int length = hexString.length();
        if(length % 2 != 0){
            throw new NumberFormatException("Odd hex string length: " + length);
        }
        byte[] results = new byte[length / 2];
        for(int i = 0; i < length; i++){
            int value = decodeHexChar(hexString.charAt(i)) << 4;
            i ++;
            value |= decodeHexChar(hexString.charAt(i));
            results[i / 2] = (byte) (value & 0xff);
        }
        return results;
    }
    public static int parseHex(String hexString){
        return (int) parseHexLong(hexString);
    }
    public static byte parseHexByte(String hex){
        String hexString = hex;
        boolean negative = false;
        if(hexString.charAt(0) == '-'){
            hexString = hexString.substring(1);
            negative = true;
        }
        if(hexString.charAt(0) == '+'){
            hexString = hexString.substring(1);
        }
        int i = hexString.length() - 1;
        if(hexString.charAt(i) == 't'){
            hexString = hexString.substring(0, i);
        }
        hexString = trim0x(hexString);
        i = Integer.parseInt(hexString, 16);
        if((i & 0xff) != i){
            throw new NumberFormatException("Invalid byte hex '" + hex + "'");
        }
        if(negative){
            i = -i;
        }
        return (byte) i;
    }
    public static short parseHexShort(String hex){
        String hexString = hex;
        boolean negative = false;
        if(hexString.charAt(0) == '-'){
            hexString = hexString.substring(1);
            negative = true;
        }
        if(hexString.charAt(0) == '+'){
            hexString = hexString.substring(1);
        }
        int i = hexString.length() - 1;
        char postfix = hexString.charAt(i);
        if(postfix == 'S' || postfix == 's'){
            hexString = hexString.substring(0, i);
        }
        hexString = trim0x(hexString);
        i = Integer.parseInt(hexString, 16);
        if((i & 0xffff) != i){
            throw new NumberFormatException("Invalid short hex '" + hex + "'");
        }
        if(negative){
            i = -i;
        }
        return (short) i;
    }
    public static int parseHexInteger(String hex){
        return (int) parseHexLong(hex);
    }
    public static long parseHexLong(String hex){
        String hexString = hex;
        boolean negative = false;
        if(hexString.charAt(0) == '-'){
            hexString = hexString.substring(1);
            negative = true;
        }
        if(hexString.charAt(0) == '+'){
            hexString = hexString.substring(1);
        }
        int i = hexString.length() - 1;
        if(hexString.charAt(i) == 'L'){
            hexString = hexString.substring(0, i);
        }
        hexString = trim0x(hexString);
        long result = 0;
        int length = hexString.length();
        for(i = 0; i < length; i++){
            result = result << 4;
            int v = decodeHexChar(hexString.charAt(i));
            if(v == -1){
                throw new NumberFormatException("Invalid hex char for string '" + hexString + "'");
            }
            result = result | v;
        }
        if(negative){
            result = -result;
        }
        return result;
    }
    public static int decodeHex(String hex, int def){
        String hexString = hex;
        boolean negative = false;
        if(hexString.charAt(0) == '-'){
            hexString = hexString.substring(1);
            negative = true;
        }
        if(hexString.charAt(0) == '+'){
            hexString = hexString.substring(1);
        }
        int i = hexString.length() - 1;
        if(hexString.charAt(i) == 'L'){
            hexString = hexString.substring(0, i);
        }
        hexString = trim0x(hexString);
        int result = 0;
        int length = hexString.length();
        for(i = 0; i < length; i++){
            result = result << 4;
            int v = decodeHexChar(hexString.charAt(i));
            if(v == -1){
                return def;
            }
            result = result | v;
        }
        if(negative){
            result = -result;
        }
        return result;
    }
    public static int decodeHexChar(byte b){
        return decodeHexChar((char) (0xff & b));
    }
    public static int decodeHexChar(char ch){
        if(ch <= '9' && ch >= '0'){
            return ch - '0';
        }
        if(ch <= 'f' && ch >= 'a'){
            return 10 + (ch - 'a');
        }
        if(ch <= 'F' && ch >= 'A'){
            return 10 + (ch - 'A');
        }
        return -1;
    }
    public static boolean isHexChar(char ch){
        if(ch <= '9' && ch >= '0'){
            return true;
        }
        if(ch <= 'f' && ch >= 'a'){
            return true;
        }
        return ch <= 'F' && ch >= 'A';
    }
    public static char toHexChar(int i){
        if(i >= 0){
            if(i < 10){
                i = i + '0';
                return (char) i;
            }
            if(i <= 16){
                i = i - 10;
                i = i + 'a';
                return (char) i;
            }
        }
        return 0;
    }
    private static String trim0x(String hexString){
        if(hexString == null || hexString.length() < 3){
            return hexString;
        }
        if(hexString.charAt(0) == '0' && hexString.charAt(1) == 'x'){
            hexString = hexString.substring(2);
        }
        return hexString;
    }
}
