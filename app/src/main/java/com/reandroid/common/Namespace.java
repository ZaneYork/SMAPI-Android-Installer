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
package com.reandroid.common;

import android.text.TextUtils;

import com.reandroid.utils.ObjectsUtil;

public interface Namespace {
    String getPrefix();
    String getUri();

    static boolean isValidUri(String uri, int resourceId) {
        int packageId = (resourceId >> 24 ) & 0xff;
        if(packageId == 0) {
            if(TextUtils.isEmpty(uri)) {
                return true;
            }
            return isExternalUri(uri);
        }
        if(packageId == 0x1) {
            return URI_ANDROID.equals(uri);
        }
        if(URI_ANDROID.equals(uri) || !isValidUri(uri)) {
            return false;
        }
        return !isExternalUri(uri);
    }
    static boolean isValidUri(String uri, String packageName) {
        if(PREFIX_ANDROID.equals(packageName)) {
            return URI_ANDROID.equals(uri);
        }
        if(URI_ANDROID.equals(uri)) {
            return false;
        }
        return isValidUri(uri);
    }
    static boolean isValidUri(String uri) {
        if(uri == null || uri.length() < 3){
            return false;
        }
        // TODO: Properly check for valid uri
        return uri.contains("://");
    }
    static boolean isExternalUri(String uri) {
        return uri != null && !uri.contains("schemas.android.com");
    }
    static boolean isValidPrefix(String prefix, String packageName) {
        if(PREFIX_ANDROID.equals(packageName)) {
            return PREFIX_ANDROID.equals(prefix);
        }
        if(PREFIX_ANDROID.equals(prefix)) {
            return false;
        }
        return isValidPrefix(prefix);
    }
    static boolean isValidPrefix(String prefix) {
        if(prefix == null || prefix.length() == 0){
            return false;
        }
        char[] chars = prefix.toCharArray();
        if(!isValidPrefixChar(chars[0])){
            return false;
        }
        for(int i = 1; i < chars.length; i++){
            char ch = chars[i];
            if(isValidPrefixChar(ch) || isValidPrefixSymbol(ch)){
                continue;
            }
            return false;
        }
        return true;
    }
    static boolean isValidPrefixChar(char ch){
        return (ch <= 'Z' && ch >= 'A')
                || (ch <= 'z' && ch >= 'a');
    }
    static boolean isValidPrefixSymbol(char ch){
        return (ch <= '9' && ch >= '0')
                || ch == '_';
    }
    static String prefixForResourceId(int resourceId) {
        if(resourceId == 0) {
            return null;
        }
        int packageId = (resourceId >> 24) & 0xff;
        if(packageId == 0x1) {
            return PREFIX_ANDROID;
        }else if(packageId != 0){
            return PREFIX_APP;
        }else {
            return null;
        }
    }
    static String uriForResourceId(int resourceId) {
        if(resourceId == 0) {
            return null;
        }
        int packageId = (resourceId >> 24) & 0xff;
        if(packageId == 0x1) {
            return URI_ANDROID;
        }else if(packageId != 0){
            return URI_RES_AUTO;
        }else {
            return null;
        }
    }

    String URI_ANDROID = ObjectsUtil.of("http://schemas.android.com/apk/res/android");
    String URI_RES_AUTO = ObjectsUtil.of("http://schemas.android.com/apk/res-auto");
    String PREFIX_ANDROID = ObjectsUtil.of("android");
    String PREFIX_APP = ObjectsUtil.of("app");
}
