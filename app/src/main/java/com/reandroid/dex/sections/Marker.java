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
package com.reandroid.dex.sections;

import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.model.DexFile;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;

import java.util.Iterator;

// Copied partially from AOSP
public class Marker {
    public static final String VERSION = "version";
    public static final String MIN_API = "min-api";
    public static final String DESUGARED_LIBRARY_IDENTIFIERS = "desugared-library-identifiers";
    public static final String SHA1 = "sha-1";
    public static final String COMPILATION_MODE = "compilation-mode";
    public static final String HAS_CHECKSUMS = "has-checksums";
    public static final String BACKEND = "backend";
    public static final String PG_MAP_ID = "pg-map-id";
    public static final String R8_MODE = "r8-mode";
    private static final String ANDROID_PLATFORM_BUILD = "platform";
    private static final char PREFIX_CHAR = '~';
    private static final String PREFIX = "~~";
    private static final String D8_PREFIX = PREFIX + Tool.D8 + "{";
    private static final String R8_PREFIX = PREFIX + Tool.R8 + "{";
    private static final String L8_PREFIX = PREFIX + Tool.L8 + "{";
    private JSONObject jsonObject;
    private Tool tool;
    private StringId stringId;
    public Marker(Tool tool) {
        this(tool, new JSONObject());
    }
    public Marker(Tool tool, JSONObject jsonObject) {
        this.tool = tool;
        this.jsonObject = jsonObject;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }
    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }
    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public StringId getStringId() {
        return stringId;
    }
    public void setStringId(StringId stringId) {
        this.stringId = stringId;
        if(stringId != null){
            stringId.addUsageType(UsageMarker.USAGE_MARKER);
        }
    }
    public void removeSelf(){
        StringId stringId = getStringId();
        if(stringId != null){
            setStringId(null);
            stringId.removeSelf();
        }
    }
    public void save(){
        StringId stringData = getStringId();
        if(stringData != null){
            stringData.setString(buildString());
        }
    }

    public Tool getTool() {
        return tool;
    }
    public boolean isD8() {
        return tool == Tool.D8;
    }
    public boolean isR8() {
        return tool == Tool.R8;
    }
    public boolean isL8() {
        return tool == Tool.L8;
    }
    public boolean isRelocator() {
        return tool == Tool.Relocator;
    }
    public String getVersion() {
        return jsonObject.optString(VERSION, null);
    }
    public Marker setVersion(String version) {
        jsonObject.put(VERSION, version);
        return this;
    }
    public boolean isDesugared() {
        // For both DEX and CF output from D8 and R8 a min-api setting implies that the code has been
        // desugared, as even the highest min-api require desugaring of lambdas.
        return hasMinApi();
    }
    public boolean hasMinApi() {
        return jsonObject.has(MIN_API);
    }
    public Long getMinApi() {
        return jsonObject.getLong(MIN_API);
    }
    public Marker setMinApi(Long minApi) {
        jsonObject.put(MIN_API, minApi);
        return this;
    }
    public boolean hasDesugaredLibraryIdentifiers() {
        return jsonObject.has(DESUGARED_LIBRARY_IDENTIFIERS);
    }
    public String[] getDesugaredLibraryIdentifiers() {
        if (jsonObject.has(DESUGARED_LIBRARY_IDENTIFIERS)) {
            JSONArray array = jsonObject.getJSONArray(DESUGARED_LIBRARY_IDENTIFIERS);
            String[] identifiers = new String[array.length()];
            for (int i = 0; i < array.length(); i++) {
                identifiers[i] = array.getString(i);
            }
            return identifiers;
        }
        return new String[0];
    }
    public Marker setDesugaredLibraryIdentifiers(String[] identifiers) {
        if(identifiers == null || identifiers.length == 0){
            jsonObject.remove(DESUGARED_LIBRARY_IDENTIFIERS);
            return this;
        }
        JSONArray jsonIdentifiers = new JSONArray();
        for (String identifier : identifiers) {
            jsonIdentifiers.put(identifier);
        }
        jsonObject.put(DESUGARED_LIBRARY_IDENTIFIERS, jsonIdentifiers);
        return this;
    }
    public String getSha1() {
        return jsonObject.optString(SHA1, null);
    }
    public Marker setSha1(String sha1) {
        jsonObject.put(SHA1, sha1);
        return this;
    }
    public String getCompilationMode() {
        return jsonObject.optString(COMPILATION_MODE, null);
    }

    public Marker setCompilationMode(String mode) {
        jsonObject.put(COMPILATION_MODE, mode);
        return this;
    }

    public boolean hasBackend() {
        return jsonObject.has(BACKEND);
    }
    public String getBackend() {
        if (!hasBackend()) {
            // Before adding backend we would always compile to dex if min-api was specified.
            return hasMinApi()
                    ? StringsUtil.toLowercase(Backend.DEX.name())
                    : StringsUtil.toLowercase(Backend.CF.name());
        }
        return jsonObject.getString(BACKEND);
    }
    public boolean isCfBackend() {
        return getBackend().equals(StringsUtil.toLowercase(Backend.CF.name()));
    }
    public boolean isDexBackend() {
        return getBackend().equals(StringsUtil.toLowercase(Backend.DEX.name()));
    }
    public Marker setBackend(Backend backend) {
        if(backend == null){
            jsonObject.remove(BACKEND);
        }else {
            jsonObject.put(BACKEND, StringsUtil.toLowercase(backend.name()));
        }
        return this;
    }
    public Boolean getHasChecksums() {
        if(jsonObject.has(HAS_CHECKSUMS)){
            return jsonObject.getBoolean(HAS_CHECKSUMS);
        }
        return null;
    }
    public Marker setHasChecksums(Boolean hasChecksums) {
        jsonObject.put(HAS_CHECKSUMS, hasChecksums);
        return this;
    }
    public String getPgMapId() {
        return jsonObject.optString(PG_MAP_ID, null);
    }
    public Marker setPgMapId(String pgMapId) {
        jsonObject.put(PG_MAP_ID, pgMapId);
        return this;
    }
    public String getR8Mode() {
        return jsonObject.getString(R8_MODE);
    }
    public Marker setR8Mode(String r8Mode) {
        jsonObject.put(R8_MODE, r8Mode);
        return this;
    }
    public Boolean isAndroidPlatformBuild() {
        if(jsonObject.has(ANDROID_PLATFORM_BUILD)){
            return jsonObject.getBoolean(ANDROID_PLATFORM_BUILD);
        }
        return null;
    }
    public Marker setAndroidPlatformBuild(Boolean value) {
        jsonObject.put(ANDROID_PLATFORM_BUILD, value);
        return this;
    }
    public String buildString() {
        jsonObject.sort(CompareUtil.getComparableComparator(), true);
        return PREFIX + tool + jsonObject;
    }
    @Override
    public String toString() {
        return PREFIX + tool + jsonObject;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Marker) {
            Marker other = (Marker) obj;
            return (tool == other.tool) && jsonObject.equals(other.jsonObject);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return tool.hashCode() + 3 * jsonObject.hashCode();
    }

    public static Iterator<Marker> parse(DexFile dexFile){
        return parse(dexFile.getSection(SectionType.STRING_ID));
    }
    public static Iterator<Marker> parse(Section<StringId> stringIdSection){
        if(stringIdSection == null){
            return EmptyIterator.of();
        }
        return parse(stringIdSection.iterator());
    }
    public static Iterator<Marker> parse(Iterator<StringId> iterator){
        return ComputeIterator.of(iterator, Marker::parse);
    }
    public static Marker parse(StringId stringId) {
        String str = stringId.getString();
        if (hasMarkerPrefix(str)) {
            Marker marker = null;
            if (str.startsWith(D8_PREFIX)) {
                marker = internalParse(Tool.D8, str.substring(D8_PREFIX.length() - 1));
            }else if (str.startsWith(R8_PREFIX)) {
                marker = internalParse(Tool.R8, str.substring(R8_PREFIX.length() - 1));
            }else if (str.startsWith(L8_PREFIX)) {
                marker = internalParse(Tool.L8, str.substring(L8_PREFIX.length() - 1));
            }
            if(marker != null){
                marker.setStringId(stringId);
            }
            return marker;
        }
        return null;
    }
    public static Marker parse(String dexString) {
        if (hasMarkerPrefix(dexString)) {
            if (dexString.startsWith(D8_PREFIX)) {
                return internalParse(Tool.D8, dexString.substring(D8_PREFIX.length() - 1));
            }
            if (dexString.startsWith(R8_PREFIX)) {
                return internalParse(Tool.R8, dexString.substring(R8_PREFIX.length() - 1));
            }
            if (dexString.startsWith(L8_PREFIX)) {
                return internalParse(Tool.L8, dexString.substring(L8_PREFIX.length() - 1));
            }
        }
        return null;
    }
    public static Marker createR8() {
        return parse(R8_TEMPLATE);
    }
    public static Marker createD8() {
        return parse(D8_TEMPLATE);
    }
    public static boolean hasMarkerPrefix(String content) {
        if(content == null || content.length() < 3){
            return false;
        }
        return content.charAt(0) == PREFIX_CHAR && content.charAt(1) == PREFIX_CHAR;
    }

    private static Marker internalParse(Tool tool, String str) {
        try {
            return new Marker(tool, new JSONObject(str));
        } catch (Exception e) {
        }
        return null;
    }

    public enum Tool {
        D8,
        GlobalSyntheticsGenerator,
        L8,
        R8,
        Relocator,
        TraceReferences;
        public static Tool[] valuesR8andD8() {
            return new Tool[] {Tool.D8, Tool.R8};
        }
    }
    public enum Backend {
        CF,
        DEX
    }

    public static final String R8_TEMPLATE = "~~R8{\"backend\":\"dex\",\"compilation-mode\":\"release\",\"has-checksums\":false,\"r8-mode\":\"compatibility\",\"version\":\"3.2.74\"}";
    public static final String D8_TEMPLATE = "~~D8{\"backend\":\"dex\",\"compilation-mode\":\"release\",\"has-checksums\":false,\"min-api\":24,\"version\":\"4.0.48\"}";
}

