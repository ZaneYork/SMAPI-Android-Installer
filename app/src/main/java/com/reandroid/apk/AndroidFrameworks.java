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
package com.reandroid.apk;

import com.reandroid.apk.framework.FrameworkManager;
import com.reandroid.apk.framework.InternalFrameworks;

public class AndroidFrameworks {

    private static FrameworkManager frameworkManager;
    private static FrameworkApk mCurrent;

    public static void setCurrent(FrameworkApk current){
        synchronized (AndroidFrameworks.class){
            mCurrent = current;
        }
    }
    public static FrameworkApk getCurrent(){
        FrameworkApk current = mCurrent;
        if(current==null){
            return null;
        }
        if(current.isDestroyed()){
            mCurrent = null;
            return null;
        }
        return current;
    }
    public static FrameworkApk getLatest() {
        return getFrameworkManager().getLatest();
    }
    public static FrameworkApk getBestMatch(int version){
        return getFrameworkManager().getBestMatch(version);
    }
    public static FrameworkManager getFrameworkManager(){
        synchronized (AndroidFrameworks.class){
            FrameworkManager manager = AndroidFrameworks.frameworkManager;
            if(manager == null){
                manager = InternalFrameworks.INSTANCE;
                AndroidFrameworks.frameworkManager = manager;
            }
            return manager;
        }
    }
    public static void setFrameworkManager(FrameworkManager frameworkManager) {
        synchronized (AndroidFrameworks.class){
            AndroidFrameworks.frameworkManager = frameworkManager;
        }
    }
}
