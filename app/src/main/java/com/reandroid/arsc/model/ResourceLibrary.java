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
package com.reandroid.arsc.model;

import com.reandroid.common.Namespace;

public interface ResourceLibrary extends Namespace {
    int getId();
    String getName();
    boolean packageNameMatches(String packageName);

    static String toPrefix(String name){
        if(name == null){
            return null;
        }
        int i = name.lastIndexOf('.');
        if(i > 0){
            name = name.substring(i + 1);
        }
        return name;
    }
    static boolean packageNameMatches(ResourceLibrary lib, String packageName){
        if(packageName == null){
            return false;
        }
        if(PREFIX_ANDROID.equals(lib.getName())){
            return PREFIX_ANDROID.equals(packageName);
        }
        return packageName.equals(lib.getName())
                || packageName.equals(lib.getPrefix());
    }
}
