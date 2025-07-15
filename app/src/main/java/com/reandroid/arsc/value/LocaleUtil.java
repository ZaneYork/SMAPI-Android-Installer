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

public class LocaleUtil {

    public static void memset(char[] out, char src, int length){
        for(int i = 0; i < length; i++){
            out[i] = src;
        }
    }
    public static void memcpy(char[] out, byte[] src, int length){
        for(int i = 0; i < length; i++){
            out[i] = (char) src[i];
        }
    }
    public static boolean contains(long[] locales, long packed_locale){
        int length = locales.length;
        for(int i = 0; i < length; i++){
            if(locales[i] == packed_locale){
                return true;
            }
        }
        return false;
    }
    public static int count(long[] locales, long packed_locale){
        int result = 0;
        int length = locales.length;
        for(int i = 0; i < length; i++){
            if(locales[i] == packed_locale){
                result++;
            }
        }
        return result;
    }
    public static int[] find(int[][] map, int packed_locale){
        int length = map.length;
        for(int i = 0; i < length; i++){
            int[] lookup_result = map[i];
            if(lookup_result[0] == packed_locale){
                return lookup_result;
            }
        }
        return null;
    }
}
