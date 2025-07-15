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
package com.reandroid.arsc.coder;

public class XmlSanitizer {

    public static String escapeQuote(String text){
        text = quoteWhitespace(text);
        text = escapeSpecialCharacter(text);
        return text;
    }
    public static String unEscapeUnQuote(String text){
        if(text == null || text.length() < 2){
            return text;
        }
        char first = text.charAt(0);
        if(first == '"'){
            return unQuoteWhitespace(text);
        }
        return unEscapeSpecialCharacter(text);
    }
    public static String escapeSpecialCharacter(String text){
        if(shouldEscapeSpecial(text)){
            text = '\\' + text;
        }
        return text;
    }
    public static String escapeDecodedValue(String text){
        if(shouldEscapeDecoded(text) || shouldEscapeSpecial(text)){
            text = '\\' + text;
        }
        return text;
    }
    public static String unEscapeSpecialCharacter(String text){
        if(shouldUnEscape(text)){
            text = text.substring(1);
        }
        return text;
    }
    public static String quoteWhitespace(String text){
        if(!shouldQuote(text)){
            return text;
        }
        return "\"" + text + "\"";
    }
    public static String unQuoteWhitespace(String text){
        if(!shouldUnQuote(text)){
            return text;
        }
        return text.substring(1, text.length()-1);
    }

    private static boolean shouldUnEscape(String text){
        if(text == null || text.length() < 2){
            return false;
        }
        if(text.charAt(0) != '\\'){
            return false;
        }
        return isAlreadyEscaped(text, 1)
                || looksDecoded(text, 1)
                || startsWithSpecialCharacter(text, 1);
    }
    private static boolean shouldEscapeSpecial(String text){
        if(android.text.TextUtils.isEmpty(text)){
            return false;
        }
        return isAlreadyEscaped(text, 0)
                || startsWithSpecialCharacter(text, 0);
    }
    private static boolean shouldEscapeDecoded(String text){
        if(android.text.TextUtils.isEmpty(text)){
            return false;
        }
        return looksDecoded(text, 0);
    }
    private static boolean isAlreadyEscaped(String text, int offset){
        int len = text.length();
        if(len <= offset){
            return false;
        }
        return text.charAt(offset) == '\\';
    }
    private static boolean looksDecoded(String text, int offset){
        int len = text.length();
        if(len <= offset || len > 14){
            return false;
        }
        if(!looksNumber(text, offset) && !isBoolean(text, offset)){
            return false;
        }
        return ValueCoder.encode(text.substring(offset)) != null;
    }
    private static boolean isBoolean(String text, int offset){
        text = text.substring(offset);
        return "true".equals(text)
                || "false".equals(text);
    }
    private static boolean looksNumber(String text, int offset){
        char ch = text.charAt(offset);
        if(isNumber(ch)){
            return true;
        }
        offset ++;
        if(offset == text.length() || ch != '-'){
            return false;
        }
        ch = text.charAt(offset);
        return isNumber(ch);
    }
    private static boolean isNumber(char ch){
        return ch <= '9' && ch >= '0';
    }
    private static boolean startsWithSpecialCharacter(String text, int offset){
        if(text.length() < offset + 2){
            return false;
        }
        return isSpecialCharacter(text.charAt(offset));
    }
    private static boolean shouldUnQuote(String text){
        if(text == null || text.length() < 3){
            return false;
        }
        if(text.charAt(0) != '"' || text.charAt(text.length() -1) != '"'){
            return false;
        }
        return isWhiteSpace(text, 1) || isQuotedWhiteSpace(text, 1);
    }
    private static boolean shouldQuote(String text){
        if(text == null){
            return false;
        }
        return isWhiteSpace(text, 0) || isQuotedWhiteSpace(text, 0);
    }
    private static boolean isQuotedWhiteSpace(String text, int offset){
        if(text == null){
            return false;
        }
        int len = text.length();
        if(len <= offset){
            return false;
        }
        len = len - offset - 1;
        if(text.charAt(offset) != '"'){
            return false;
        }
        if(text.charAt(len) != '"'){
            return false;
        }
        return isWhiteSpace(text, offset + 1);
    }
    private static boolean isWhiteSpace(String text, int offset){
        if(text == null){
            return false;
        }
        int len = text.length() - 1;
        if(len <= offset){
            return false;
        }
        char[] chars = text.toCharArray();
        len = chars.length - offset;
        for(int i = offset; i < len; i++){
            if(!isWhiteSpace(chars[i])){
                return false;
            }
        }
        return true;
    }
    private static boolean isWhiteSpace(char ch){
        switch (ch){
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                return true;
            default:
                return false;
        }
    }
    private static boolean isSpecialCharacter(char ch){
        switch (ch){
            case '@':
            case '?':
            case '#':
                return true;
            default:
                return false;
        }
    }

}
