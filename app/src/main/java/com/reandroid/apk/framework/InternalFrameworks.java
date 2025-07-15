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

import com.reandroid.apk.AndroidFrameworks;
import com.reandroid.apk.FrameworkApk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class InternalFrameworks extends FrameworkManager{

    public static final InternalFrameworks INSTANCE = new InternalFrameworks();

    private Map<Integer, String> resourcePaths;

    private InternalFrameworks(){
        super();
    }

    @Override
    public FrameworkApk get(int version) {
        return null;
    }
    public FrameworkApk getBestMatch(int version){
        Integer nearest = getNearestVersion(version);
        if(nearest == null){
            return null;
        }
        synchronized (AndroidFrameworks.class){
            int best = nearest;
            FrameworkApk current = getCurrent();
            if(current != null && best == current.getVersionCode()){
                return current;
            }
            try {
                return loadResource(best);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
    @Override
    public FrameworkApk getLatest() {
        synchronized (AndroidFrameworks.class){
            int latest = getLatestVersion();
            FrameworkApk current = getCurrent();
            if(current != null && latest == current.getVersionCode()){
                return current;
            }
            FrameworkApk frameworkApk;
            try {
                frameworkApk = loadResource(latest);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            if(current == null){
                setCurrent(frameworkApk);
            }
            return frameworkApk;
        }
    }
    @Override
    public Integer getNearestVersion(int version) {
        Map<Integer, String> pathMap = getResourcePaths();
        if(pathMap.containsKey(version)){
            return version;
        }
        int highest = 0;
        int best = 0;
        int prevDifference = 0;
        for(int id:pathMap.keySet()){
            if(highest==0){
                highest = id;
                best = id;
                prevDifference = version*2 + 1000;
                continue;
            }
            if(id>highest){
                highest = id;
            }
            int diff = id-version;
            if(diff<0){
                diff=-diff;
            }
            if(diff<prevDifference || (diff==prevDifference && id>best)){
                best = id;
                prevDifference = diff;
            }
        }
        return best;
    }
    @Override
    public Integer getLatestVersion() {
        Map<Integer, String> pathMap = getResourcePaths();
        int highest = 0;
        for(int id:pathMap.keySet()){
            if(highest==0){
                highest = id;
                continue;
            }
            if(id>highest){
                highest = id;
            }
        }
        return highest;
    }
    private Map<Integer, String> getResourcePaths(){
        if(resourcePaths != null){
            return resourcePaths;
        }
        synchronized (this){
            resourcePaths = scanAvailableResourcePaths();
            return resourcePaths;
        }
    }
    private FrameworkApk loadResource(int version) throws IOException {
        String path = getResourcePaths().get(version);
        if(path == null){
            throw new IOException("No resource found for version: " + version);
        }
        String simpleName = toSimpleName(path);
        return FrameworkApk.loadApkBuffer(simpleName, AndroidFrameworks.class.getResourceAsStream(path));
    }
    private Map<Integer, String> scanAvailableResourcePaths(){
        Map<Integer, String> results = new HashMap<>();
        int maxSearch = 35;
        for(int version = 21; version < maxSearch; version++){
            String path = toResourcePath(version);
            if(!isAvailable(path)){
                continue;
            }
            results.put(version, path);
            if((version + 1) == maxSearch){
                maxSearch++;
            }
        }
        return results;
    }
    private static String toSimpleName(String path){
        int i = path.lastIndexOf('/');
        if(i<0){
            i = path.lastIndexOf(File.separatorChar);
        }
        if(i>0){
            i++;
            path = path.substring(i);
        }
        i = path.lastIndexOf('.');
        if(i>=0){
            path = path.substring(0, i);
        }
        return path;
    }
    private static boolean isAvailable(String path){
        InputStream inputStream = InternalFrameworks.class.getResourceAsStream(path);
        if(inputStream == null){
            return false;
        }
        closeQuietly(inputStream);
        return true;
    }
    private static void closeQuietly(InputStream stream){
        if(stream == null){
            return;
        }
        try {
            stream.close();
        } catch (IOException ignored) {
        }
    }
    private static String toResourcePath(int version){
        return ANDROID_RESOURCE_DIRECTORY + ANDROID_PACKAGE
                + '-' + version
                +FRAMEWORK_EXTENSION;
    }
    private static final String ANDROID_RESOURCE_DIRECTORY = "/frameworks/android/";
    private static final String ANDROID_PACKAGE = "android";
    private static final String FRAMEWORK_EXTENSION = ".apk";
}
