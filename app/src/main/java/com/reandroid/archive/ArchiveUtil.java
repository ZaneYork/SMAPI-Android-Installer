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
package com.reandroid.archive;

import android.text.TextUtils;

public class ArchiveUtil {
    public static String sanitizePath(String path){
        if(TextUtils.isEmpty(path)){
            return null;
        }
        path = path.replace('\\', '/');
        char[] chars = path.toCharArray();
        int length = chars.length;
        StringBuilder builder = new StringBuilder(length);
        StringBuilder dots = null;
        boolean skip = false;
        int last = length - 1;
        for(int i = 0; i < length; i++){
            char ch = chars[i];
            if(skip){
                builder.append(ch);
            }else if(ch == '.'){
                if(dots == null){
                    dots = new StringBuilder(5);
                }
                dots.append(ch);
            }else if(ch == '/'){
                dots = null;
                if(i == last){
                    builder.append(ch);
                }
            }else {
                builder = new StringBuilder(length);
                if(dots != null){
                    builder.append(dots.toString());
                    dots = null;
                }
                builder.append(ch);
                skip = true;
            }
        }
        if(dots == null){
            return builder.toString();
        }
        String tmp = dots.toString();
        if(tmp.length() > 2){
            return tmp;
        }
        return null;
    }
}
