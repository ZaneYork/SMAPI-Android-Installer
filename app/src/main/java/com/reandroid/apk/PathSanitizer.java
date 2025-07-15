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

import com.reandroid.archive.InputSource;
import com.reandroid.utils.HexUtil;
import com.reandroid.identifiers.Identifier;
import com.reandroid.utils.StringsUtil;

import java.util.*;

public class PathSanitizer {
    private final Collection<? extends InputSource> sourceList;
    private final boolean sanitizeResourceFiles;
    private Collection<ResFile> resFileList;
    private APKLogger apkLogger;
    private final Set<String> mSanitizedPaths;
    private boolean mCaseInsensitive;
    private int mUniqueName;
    public PathSanitizer(Collection<? extends InputSource> sourceList, boolean sanitizeResourceFiles){
        this.sourceList = sourceList;
        this.mSanitizedPaths = new HashSet<>();
        this.sanitizeResourceFiles = sanitizeResourceFiles;
        this.mCaseInsensitive = Identifier.CASE_INSENSITIVE_FS;
    }
    public PathSanitizer(Collection<? extends InputSource> sourceList){
        this(sourceList, false);
    }
    public void sanitize(){
        mSanitizedPaths.clear();
        logMessage("Sanitizing paths ...");
        sanitizeCaseInsensitiveOs();
        sanitizeResFiles();
        for(InputSource inputSource:sourceList){
            sanitize(inputSource, 1, false);
        }
    }
    public void setResourceFileList(Collection<ResFile> resFileList){
        this.resFileList = resFileList;
    }

    public boolean isCaseInsensitive(){
        return mCaseInsensitive;
    }
    public void setCaseInsensitive(boolean caseInsensitive){
        mCaseInsensitive = caseInsensitive;
    }
    private void sanitizeCaseInsensitiveOs(){
        if(!Identifier.CASE_INSENSITIVE_FS){
            return;
        }
        logMessage("[WIN/MAC] Checking duplicate case insensitive paths ...");
        mUniqueName = 0;
        Map<String, InputSource> uniqueMap = new HashMap<>();
        for(InputSource inputSource : sourceList){
            String path = inputSource.getAlias().toLowerCase();
            InputSource exist = uniqueMap.get(path);
            if(exist == null){
                uniqueMap.put(path, inputSource);
                continue;
            }
            sanitizeCaseInsensitiveOs(inputSource);
            sanitizeCaseInsensitiveOs(exist);
            uniqueMap.remove(path);
        }
    }
    private void sanitizeCaseInsensitiveOs(InputSource inputSource){
        String path = inputSource.getAlias();
        mUniqueName++;
        String uniqueName = createUniqueName(mUniqueName + path);
        String alias;
        int i = path.lastIndexOf('/');
        if(i > 0){
            alias = path.substring(0, i) + "/" + uniqueName;
        }else {
            alias = uniqueName;
        }
        inputSource.setAlias(alias);
        String msg = "'" + path + "' -> '" + alias + "'";
        if(mUniqueName < 10){
            logMessage("Case sensitive path renamed: " + msg);
        }else {
            logVerbose(msg);
        }
    }
    private void sanitizeResFiles(){
        Collection<ResFile> resFileList = this.resFileList;
        if(resFileList == null){
            return;
        }
        boolean sanitizeRes = this.sanitizeResourceFiles;
        Set<String> sanitizedPaths = this.mSanitizedPaths;
        if(sanitizeRes){
            logMessage("Sanitizing resource files ...");
        }
        for(ResFile resFile:resFileList){
            if(sanitizeRes){
                sanitize(resFile);
            }else {
                sanitizedPaths.add(resFile.getFilePath());
            }
        }
    }
    private void sanitize(ResFile resFile){
        InputSource inputSource = resFile.getInputSource();
        String replace = sanitize(inputSource, 3, true);
        if(replace==null){
            return;
        }
        resFile.setFilePath(replace);
    }
    private String sanitize(InputSource inputSource, int depth, boolean fixedDepth){
        String name = inputSource.getName();
        if(mSanitizedPaths.contains(name)){
            return null;
        }
        mSanitizedPaths.add(name);
        String alias = inputSource.getAlias();
        if(shouldIgnore(alias)){
            return null;
        }
        String replace = sanitize(alias, depth, fixedDepth);
        if(alias.equals(replace)){
            return null;
        }
        inputSource.setAlias(replace);
        if(alias.length() > 20){
            alias = ".. " + alias.substring(alias.length()-20);
        }
        logVerbose("'" + alias + "' -> '" + replace + "'");
        return replace;
    }

    private String sanitize(String name, int depth, boolean fixedDepth){
        StringBuilder builder = new StringBuilder();

        String[] nameSplit = StringsUtil.split(name, '/');

        boolean is_assets = false;

        boolean pathIsLong = name.length() >= MAX_PATH_LENGTH;
        int length = nameSplit.length;
        for(int i=0; i < length; i++){
            String split = nameSplit[i];
            if(i == 0){
                is_assets = "assets".equals(split);
            }
            if(!isGoodSimpleName(split, is_assets) || (pathIsLong && i>=depth)){
                split = createUniqueName(name);
                appendPathName(builder, split);
                break;
            }
            if(fixedDepth && i>=(depth-1)){
                if(i < length-1){
                    split = createUniqueName(name);
                }
                appendPathName(builder, split);
                break;
            }
            appendPathName(builder, split);
        }
        return builder.toString();
    }
    private boolean shouldIgnore(String path){
        return path.startsWith("lib/") && path.endsWith(".so");
    }

    public void setApkLogger(APKLogger apkLogger) {
        this.apkLogger = apkLogger;
    }
    private String getLogTag(){
        return "[SANITIZE]: ";
    }
    void logMessage(String msg){
        APKLogger logger = this.apkLogger;
        if(logger!=null){
            logger.logMessage(getLogTag()+msg);
        }
    }
    void logVerbose(String msg){
        APKLogger logger = this.apkLogger;
        if(logger!=null){
            logger.logVerbose(getLogTag()+msg);
        }
    }

    private static void appendPathName(StringBuilder builder, String name){
        if(builder.length()>0){
            builder.append('/');
        }
        builder.append(name);
    }
    private static String createUniqueName(String name){
        int hash = name.hashCode();
        return "alias_" + HexUtil.toHexNoPrefix8(hash);
    }
    private static boolean isGoodSimpleName(String name, boolean ignoreSpace){
        if(name == null){
            return false;
        }
        int length = name.length();
        if(length == 0 || length >= MAX_NAME_LENGTH){
            return false;
        }
        boolean spaceFound = false;
        boolean symbolFound = false;
        for(int i = 0; i < length; i++){
            char ch = name.charAt(i);
            if(ignoreSpace && ch == ' '){
                if(spaceFound || i == 0 || i == (length - 1)){
                    return false;
                }
                spaceFound = true;
                continue;
            }
            spaceFound = false;
            if(isGoodFileNameChar(ch)){
                symbolFound = false;
                continue;
            }
            if(isGoodFileNameSymbol(ch)){
                if(symbolFound){
                    return false;
                }
                symbolFound = true;
                continue;
            }
            return false;
        }
        return true;
    }

    public static String sanitizeSimpleName(String name){
        if(name == null){
            return null;
        }
        boolean skipNext = true;
        int currentLength = 0;
        int lengthMax = MAX_NAME_LENGTH;
        int length = name.length();
        StringBuilder builder = new StringBuilder(length);
        for(int i = 0; i < length; i++){
            if(currentLength >= lengthMax){
                break;
            }
            char ch = name.charAt(i);
            if(isGoodFileNameSymbol(ch)){
                if(!skipNext){
                    builder.append(ch);
                    currentLength++;
                }
                skipNext = true;
                continue;
            }
            if(!isGoodFileNameChar(ch)){
                skipNext = true;
                continue;
            }
            builder.append(ch);
            currentLength ++;
            skipNext = false;
        }
        if(currentLength == 0){
            return null;
        }
        return builder.toString();
    }

    private static boolean isGoodFileNameSymbol(char ch){
        return ch == '.'
                || ch == '+'
                || ch == '-'
                || ch == '#';
    }
    private static boolean isGoodFileNameChar(char ch){
        return ch == '_'
                || (ch >= '0' && ch <= '9')
                || (ch >= 'A' && ch <= 'Z')
                || (ch >= 'a' && ch <= 'z');
    }

    public static PathSanitizer create(ApkModule apkModule){
        PathSanitizer pathSanitizer = new PathSanitizer(
                apkModule.getZipEntryMap().listInputSources());
        pathSanitizer.setApkLogger(apkModule.getApkLogger());
        pathSanitizer.setResourceFileList(apkModule.listResFiles());
        return pathSanitizer;
    }

    private static final int MAX_NAME_LENGTH = 140;
    private static final int MAX_PATH_LENGTH = 4096;
}
