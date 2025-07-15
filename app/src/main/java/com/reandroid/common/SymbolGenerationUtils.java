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

// Copied and modified from AOSP
package com.reandroid.common;

import com.reandroid.utils.collection.CollectionUtil;

import java.util.Set;

public class SymbolGenerationUtils {

    public static String generateLowercase(int index) {
        return generate(index, false);
    }
    public static String generateMixedCase(int index) {
        return generate(index, true);
    }
    public static String generate(int index, boolean mixedCase) {
        int size = 1;
        int number = index + 1;
        int maximumNumberOfCharacters = mixedCase ? TOTAL_CHARACTERS : LOWERCASE_AND_SUFFIX;
        int firstNumberOfCharacters = maximumNumberOfCharacters - SUFFIX_LENGTH;

        int availableCharacters;
        for(availableCharacters = firstNumberOfCharacters; number > availableCharacters; ++size) {
            number = (number - 1) / availableCharacters;
            availableCharacters = maximumNumberOfCharacters;
        }

        char[] characters = new char[size];
        number = index + 1;
        int i = 0;
        availableCharacters = firstNumberOfCharacters;

        int firstLetterPadding;
        for(firstLetterPadding = SUFFIX_LENGTH; number > availableCharacters; firstLetterPadding = 0) {
            characters[i++] = CHARACTERS[(number - 1) % availableCharacters + firstLetterPadding];
            number = (number - 1) / availableCharacters;
            availableCharacters = maximumNumberOfCharacters;
        }

        characters[i] = CHARACTERS[number - 1 + firstLetterPadding];

        String symbol = new String(characters);
        if(isReserved(symbol)) {
            return generate(index + 1, mixedCase);
        }
        return symbol;
    }
    private static boolean isReserved(String symbol) {
        return symbol.length() > 1 && RESERVED_NAMES.contains(symbol);
    }

    static {

        char[] charArray = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                .toCharArray();

        CHARACTERS = charArray;
        int length = charArray.length;
        TOTAL_CHARACTERS = length;
        LOWERCASE_AND_SUFFIX = length - 26;
        SUFFIX_LENGTH = 10;

        RESERVED_NAMES = CollectionUtil.newHashSet(
                "boolean", "byte", "char",
                "double", "float", "int",
                "long", "short", "void", "it",
                "by", "class");
    }

    private static final char[] CHARACTERS;
    private static final int TOTAL_CHARACTERS;
    private static final int LOWERCASE_AND_SUFFIX;
    private static final int SUFFIX_LENGTH;

    private static final Set<String> RESERVED_NAMES;
}
