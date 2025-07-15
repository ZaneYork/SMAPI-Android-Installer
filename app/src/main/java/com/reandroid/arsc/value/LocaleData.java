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
package com.reandroid.arsc.value;

import java.util.Arrays;

/**
 *
 * Converted/copied from AOSP: frameworks/base/libs/androidfw/LocaleData.cpp
 *
 * */

public class LocaleData {
    public static int packLocale(char[] language, char[] region) {
        return (((int) language[0]) << 24) | (((int) language[1]) << 16) |
        (((int) region[0]) << 8) | ((int) region[1]);
    }
    public static int dropRegion(int packed_locale) {
        return packed_locale & 0xFFFF0000;
    }
    public static boolean hasRegion(int packed_locale) {
        return (packed_locale & 0x0000FFFF) != 0;
    }
    public static int findParent(int packed_locale, char[] script) {
        LocaleDataTables.ScriptParent[] SCRIPT_PARENTS = LocaleDataTables.SCRIPT_PARENTS;
        if (hasRegion(packed_locale)) {
            for (int i = 0; i < SCRIPT_PARENTS_COUNT; i++) {
                if (Arrays.equals(script, SCRIPT_PARENTS[i].script)) {
                    int[] lookup_result = LocaleUtil.find(SCRIPT_PARENTS[i].map, packed_locale);
                    if (lookup_result != null) {
                        return lookup_result[1];
                    }
                    break;
                }
            }
            return dropRegion(packed_locale);
        }
        return PACKED_ROOT;
    }
    public static int findAncestors(int[] out, int[] stop_list_index,
                      int packed_locale, char[] script,
                      int[] stop_list, int stop_set_length) {
        int ancestor = packed_locale;
        int count = 0;
        do {
            if (out != null) {
                out[count] = ancestor;
            }
            count++;
            for (int i = 0; i < stop_set_length; i++) {
                if (stop_list[i] == ancestor) {
                    stop_list_index[0] =  i;
                    return count;
                }
            }
            ancestor = findParent(ancestor, script);
        } while (ancestor != PACKED_ROOT);
        stop_list_index[0] = -1;
        return count;
    }

    public static int findDistance(int supported,
                     char[] script,
                     int[] request_ancestors,
                     int request_ancestors_count) {
        int[] request_ancestors_index = new int[1];
        int supported_ancestor_count = findAncestors(
                null, request_ancestors_index,
                supported, script,
                request_ancestors, request_ancestors_count);
        return supported_ancestor_count + request_ancestors_index[0] - 1;
    }

    public static boolean isRepresentative(int language_and_region, char[] script) {
        long packed_locale = (
                (((long) language_and_region) << 32) |
                        (((long) script[0]) << 24) |
                        (((long) script[1]) << 16) |
                        (((long) script[2]) <<  8) |
                        ((long) script[3]));
        //return LocaleUtil.count(LocaleDataTables.REPRESENTATIVE_LOCALES, packed_locale) != 0;
        return LocaleUtil.contains(LocaleDataTables.REPRESENTATIVE_LOCALES, packed_locale);
    }
    public static boolean isSpecialSpanish(int language_and_region) {
        return (language_and_region == US_SPANISH || language_and_region == MEXICAN_SPANISH);
    }

    public static int localeDataCompareRegions(
            char[] left_region, char[] right_region,
            char[] requested_language, char[] requested_script,
            char[] requested_region) {

        if (left_region[0] == right_region[0] && left_region[1] == right_region[1]) {
            return 0;
        }
        int left = packLocale(requested_language, left_region);
        int right = packLocale(requested_language, right_region);
        int request = packLocale(requested_language, requested_region);

        // If one and only one of the two locales is a special Spanish locale, we
        // replace it with es-419. We don't do the replacement if the other locale
        // is already es-419, or both locales are special Spanish locales (when
        // es-US is being compared to es-MX).
        boolean leftIsSpecialSpanish = isSpecialSpanish(left);
        boolean rightIsSpecialSpanish = isSpecialSpanish(right);
        if (leftIsSpecialSpanish && !rightIsSpecialSpanish && right != LATIN_AMERICAN_SPANISH) {
            left = LATIN_AMERICAN_SPANISH;
        } else if (rightIsSpecialSpanish && !leftIsSpecialSpanish && left != LATIN_AMERICAN_SPANISH) {
            right = LATIN_AMERICAN_SPANISH;
        }

        int[] request_ancestors = new int [LocaleDataTables.MAX_PARENT_DEPTH+1];
        int[] left_right_index = new int[0];
        // Find the parents of the request, but stop as soon as we saw left or right
        int[][] left_and_right = {{left, right}};
        int ancestor_count = findAncestors(
                request_ancestors, left_right_index,
                request, requested_script,
                left_and_right[0], left_and_right.length);
        if (left_right_index[0] == 0) { // We saw left earlier
            return 1;
        }
        if (left_right_index[0] == 1) { // We saw right earlier
            return -1;
        }

        // If we are here, neither left nor right are an ancestor of the
        // request. This means that all the ancestors have been computed and
        // the last ancestor is just the language by itself. We will use the
        // distance in the parent tree for determining the better match.
        int left_distance = findDistance(
                left, requested_script, request_ancestors, ancestor_count);
        int right_distance = findDistance(
                right, requested_script, request_ancestors, ancestor_count);
        if (left_distance != right_distance) {
            return (int) right_distance - (int) left_distance; // smaller distance is better
        }

        // If we are here, left and right are equidistant from the request. We will
        // try and see if any of them is a representative locale.
        boolean left_is_representative = isRepresentative(left, requested_script);
        boolean right_is_representative = isRepresentative(right, requested_script);
        if (left_is_representative != right_is_representative) {
            return (left_is_representative ? 1 : 0) - (right_is_representative ? 1 : 0);
        }

        // We have no way of figuring out which locale is a better match. For
        // the sake of stability, we consider the locale with the lower region
        // code (in dictionary order) better, with two-letter codes before
        // three-digit codes (since two-letter codes are more specific).

        return (int)((right & 0xffffffff00000000L) - (left & 0xffffffff00000000L));
    }
    public static void localeDataComputeScript(char[] out, char[] language, char[] region) {
        if (language[0] == '\0') {
            LocaleUtil.memset(out, '\0', SCRIPT_LENGTH);
            return;
        }
        int lookup_key = packLocale(language, region);
        int[] lookup_result = LocaleUtil.find(LocaleDataTables.LIKELY_SCRIPTS, lookup_key);
        if (lookup_result == null) {
            // We couldn't find the locale. Let's try without the region
            if (region[0] != '\0') {
                lookup_key = dropRegion(lookup_key);
                lookup_result = LocaleUtil.find(LocaleDataTables.LIKELY_SCRIPTS, lookup_key);
                if (lookup_result != null) {
                    LocaleUtil.memcpy(out, LocaleDataTables.SCRIPT_CODES[lookup_result[1]],
                            SCRIPT_LENGTH);
                    return;
                }
            }
            // We don't know anything about the locale
            LocaleUtil.memset(out, '\0', SCRIPT_LENGTH);
            return;
        } else {
            // We found the locale.
            LocaleUtil.memcpy(out, LocaleDataTables.SCRIPT_CODES[lookup_result[1]], SCRIPT_LENGTH);
        }
    }
    public static boolean localeDataIsCloseToUsEnglish(char[] region) {
        int locale = packLocale(ENGLISH_CHARS, region);
        int[] stop_list_index = new int[1];
        findAncestors(null, stop_list_index, locale, LATIN_CHARS, ENGLISH_STOP_LIST, 2);
        // A locale is like US English if we see "en" before "en-001" in its ancestor list.
        return stop_list_index[0] == 0; // 'en' is first in ENGLISH_STOP_LIST
    }




    public static final int[] ENGLISH_STOP_LIST = {
            0x656E0000, // en
            0x656E8400, // en-001
    };
    public static final char[] ENGLISH_CHARS = {'e', 'n'};
    public static final char[] LATIN_CHARS = {'L', 'a', 't', 'n'};

    public static int SCRIPT_LENGTH = 4;

    public static int US_SPANISH = 0x65735553; // es-US
    public static int MEXICAN_SPANISH = 0x65734D58; // es-MX
    public static int LATIN_AMERICAN_SPANISH = 0x6573A424; // es-419

    static final int SCRIPT_PARENTS_COUNT = LocaleDataTables.SCRIPT_PARENTS.length;
    static final int PACKED_ROOT = 0; // to represent the root locale

}
