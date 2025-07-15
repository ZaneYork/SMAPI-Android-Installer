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
package com.reandroid.apk.framework;

import com.reandroid.apk.FrameworkApk;

public abstract class FrameworkManager {
    private FrameworkApk mCurrent;

    public FrameworkManager(){
    }
    public abstract FrameworkApk get(int version);
    public abstract FrameworkApk getBestMatch(int version);
    public abstract Integer getNearestVersion(int version);
    public abstract Integer getLatestVersion();
    public abstract FrameworkApk getLatest();

    public void setCurrent(FrameworkApk current){
        synchronized (this){
            mCurrent = current;
        }
    }
    public FrameworkApk getCurrent(){
        synchronized (this){
            FrameworkApk current = mCurrent;
            if(current == null){
                return null;
            }
            if(current.isDestroyed()){
                mCurrent = null;
                return null;
            }
            return current;
        }
    }
}
