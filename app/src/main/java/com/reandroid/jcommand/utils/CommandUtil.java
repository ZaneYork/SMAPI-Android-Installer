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
package com.reandroid.jcommand.utils;

public class CommandUtil {

    public static String quoteString(String str) {
        if(needsQuote(str)) {
            str = "\"" + str + "\"";
        }
        return str;
    }
    private static boolean needsQuote(String str) {
        int length = str.length();
        if(length == 0) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if(c == ' ' || c == '\n' || c == '\t' || c == '"' || c == '\'') {
                return true;
            }
        }
        return false;
    }
    public static boolean containsIgnoreCase(String[] elements, String str) {
        for (String s : elements) {
            if(str.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }
    public static String asString(String[] elements) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        int length = elements.length;
        for(int i = 0; i < length; i++) {
            if(i != 0) {
                builder.append(", ");
            }
            builder.append(elements[i]);
        }
        builder.append(']');
        return builder.toString();
    }

    public static void printTwoColumns(StringBuilder builder, String tab, int totalWidth, String[][] table){
        printTwoColumns(builder, tab, "  ", totalWidth, table);
    }
    public static void printTwoColumns(StringBuilder builder, String tab, String columnSeparator, int totalWidth, String[][] table) {
        ensureNunNull(table);
        int leftWidth = 0;
        for(String[] col : table) {
            int len = col[0].length();
            if (len > leftWidth){
                leftWidth = len;
            }
        }
        int maxRight = totalWidth - leftWidth;
        for(int i = 0; i < table.length; i++){
            String[] col = table[i];
            if (i != 0){
                builder.append("\n");
            }
            printRow(true, builder, tab, leftWidth, maxRight, col[0], columnSeparator, col[1]);
        }
    }
    private static void printRow(boolean indentLeft, StringBuilder builder, String tab, int leftWidth, int maxRight, String left, String separator, String right){
        builder.append(tab);
        if (indentLeft){
            builder.append(left);
        }
        fillSpace(builder, leftWidth - left.length());
        if (!indentLeft){
            builder.append(left);
        }
        builder.append(separator);
        char[] rightChars = right.toCharArray();
        int rightWidth = 0;
        boolean spacePrefixSeen = false;
        for(int i = 0; i < rightChars.length; i++){
            char ch = rightChars[i];
            if (i==0){
                builder.append(ch);
                rightWidth++;
                continue;
            }
            if (ch == '\n' || (rightWidth > 0 && rightWidth%maxRight == 0)){
                builder.append('\n');
                builder.append(tab);
                fillSpace(builder, leftWidth+separator.length());
                rightWidth = 0;
                spacePrefixSeen = false;
            }
            if (ch != '\n'){
                boolean skipFirstSpace = (rightWidth == 0 && ch == ' ');
                if (!skipFirstSpace || spacePrefixSeen){
                    builder.append(ch);
                    rightWidth++;
                } else {
                    spacePrefixSeen = true;
                }
            }
        }
    }
    private static void fillSpace(StringBuilder builder, int count){
        for(int i = 0; i < count; i++){
            builder.append(' ');
        }
    }
    private static void ensureNunNull(String[][] table) {
        for(String[] col : table) {
            int colLength = col.length;
            for(int j = 0; j < colLength; j++) {
                if(col[j] == null) {
                    col[j] = "null";
                }
            }
        }
    }
    public static String of(String str) {
        return str;
    }
}
