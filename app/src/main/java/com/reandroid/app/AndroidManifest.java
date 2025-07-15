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
package com.reandroid.app;

import com.reandroid.utils.ObjectsUtil;

@SuppressWarnings("unused")
public interface AndroidManifest {

    String getPackageName();
    void setPackageName(String packageName);

    String getApplicationClassName();
    void setApplicationClassName(String className);

    String getMainActivityClassName();
    void setMainActivityClassName(String className);

    Integer getVersionCode();
    void setVersionCode(int version);
    String getVersionName();
    void setVersionName(String name);
    Integer getPlatformBuildVersionCode();
    void setPlatformBuildVersionCode(int version);
    Object getPlatformBuildVersionName();
    void setPlatformBuildVersionName(Object name);

    Integer getCompileSdkVersion();
    void setCompileSdkVersion(int version);
    String getCompileSdkVersionCodename();
    void setCompileSdkVersionCodename(String name);

    Integer getMinSdkVersion();
    void setMinSdkVersion(int version);

    Integer getTargetSdkVersion();
    void setTargetSdkVersion(int version);

    default AndroidApiLevel getPlatformBuild(){
        Integer api = getPlatformBuildVersionCode();
        if(api != null){
            return AndroidApiLevel.forApi(api);
        }
        return null;
    }
    default void setPlatformBuild(AndroidApiLevel apiLevel){
        setPlatformBuildVersionCode(apiLevel.getApi());
        setPlatformBuildVersionName(apiLevel.getVersion());
    }
    default AndroidApiLevel getCompileSdk(){
        Integer api = getCompileSdkVersion();
        if(api != null){
            return AndroidApiLevel.forApi(api);
        }
        return null;
    }
    default void setCompileSdk(AndroidApiLevel apiLevel){
        setCompileSdkVersion(apiLevel.getApi());
        setCompileSdkVersionCodename(apiLevel.getVersion());
    }

    int ID_authorities = ObjectsUtil.of(0x01010018);
    int ID_compileSdkVersionCodename = ObjectsUtil.of(0x01010573);
    int ID_compileSdkVersion = ObjectsUtil.of(0x01010572);
    int ID_configChanges = ObjectsUtil.of(0x0101001f);
    int ID_debuggable = ObjectsUtil.of(0x0101000f);
    int ID_exported = ObjectsUtil.of(0x01010010);
    int ID_extractNativeLibs = ObjectsUtil.of(0x010104ea);
    int ID_host = ObjectsUtil.of(0x01010028);
    int ID_icon = ObjectsUtil.of(0x01010002);
    int ID_id = ObjectsUtil.of(0x010100d0);
    int ID_isFeatureSplit = ObjectsUtil.of(0x0101055b);
    int ID_isSplitRequired = ObjectsUtil.of(0x01010591);
    int ID_label = ObjectsUtil.of(0x01010001);
    int ID_maxSdkVersion = ObjectsUtil.of(0x01010271);
    int ID_minSdkVersion = ObjectsUtil.of(0x0101020c);
    int ID_name = ObjectsUtil.of(0x01010003);
    int ID_requiredSplitTypes = ObjectsUtil.of(0x0101064e);
    int ID_resource = ObjectsUtil.of(0x01010025);
    int ID_roundIcon = ObjectsUtil.of(0x0101052c);
    int ID_screenOrientation = ObjectsUtil.of(0x0101001e);
    int ID_splitTypes = ObjectsUtil.of(0x0101064f);
    int ID_targetActivity = ObjectsUtil.of(0x01010202);
    int ID_targetSdkVersion = ObjectsUtil.of(0x01010270);
    int ID_theme = ObjectsUtil.of(0x01010000);
    int ID_value = ObjectsUtil.of(0x01010024);
    int ID_versionCode = ObjectsUtil.of(0x0101021b);
    int ID_versionName = ObjectsUtil.of(0x0101021c);

    String NAME_authorities = ObjectsUtil.of("authorities");
    String NAME_compileSdkVersionCodename = ObjectsUtil.of("compileSdkVersionCodename");
    String NAME_compileSdkVersion = ObjectsUtil.of("compileSdkVersion");
    String NAME_configChanges = ObjectsUtil.of("configChanges");
    String NAME_coreApp = ObjectsUtil.of("coreApp");
    String NAME_debuggable = ObjectsUtil.of("debuggable");
    String NAME_exported = ObjectsUtil.of("exported");
    String NAME_extractNativeLibs = ObjectsUtil.of("extractNativeLibs");
    String NAME_host = ObjectsUtil.of("host");
    String NAME_icon = ObjectsUtil.of("icon");
    String NAME_id = ObjectsUtil.of("id");
    String NAME_installLocation = ObjectsUtil.of("installLocation");
    String NAME_isFeatureSplit = ObjectsUtil.of("isFeatureSplit");
    String NAME_isSplitRequired = ObjectsUtil.of("isSplitRequired");
    String NAME_label = ObjectsUtil.of("label");
    String NAME_maxSdkVersion = ObjectsUtil.of("maxSdkVersion");
    String NAME_minSdkVersion = ObjectsUtil.of("minSdkVersion");
    String NAME_name = ObjectsUtil.of("name");
    String NAME_PACKAGE = ObjectsUtil.of("package");
    String NAME_platformBuildVersionCode = ObjectsUtil.of("platformBuildVersionCode");
    String NAME_platformBuildVersionName = ObjectsUtil.of("platformBuildVersionName");
    String NAME_requiredSplitTypes = ObjectsUtil.of("requiredSplitTypes");
    String NAME_resource = ObjectsUtil.of("resource");
    String NAME_roundIcon = ObjectsUtil.of("roundIcon");
    String NAME_screenOrientation = ObjectsUtil.of("screenOrientation");
    String NAME_split = ObjectsUtil.of("split");
    String NAME_splitTypes = ObjectsUtil.of("splitTypes");
    String NAME_targetActivity = ObjectsUtil.of("targetActivity");
    String NAME_targetSdkVersion = ObjectsUtil.of("targetSdkVersion");
    String NAME_theme = ObjectsUtil.of("theme");
    String NAME_value = ObjectsUtil.of("value");
    String NAME_versionCode = ObjectsUtil.of("versionCode");
    String NAME_versionName = ObjectsUtil.of("versionName");

    String TAG_action = ObjectsUtil.of("action");
    String TAG_activity_alias = ObjectsUtil.of("activity-alias");
    String TAG_activity = ObjectsUtil.of("activity");
    String TAG_application = ObjectsUtil.of("application");
    String TAG_category = ObjectsUtil.of("category");
    String TAG_data = ObjectsUtil.of("data");
    String TAG_intent_filter = ObjectsUtil.of("intent-filter");
    String TAG_manifest = ObjectsUtil.of("manifest");
    String TAG_meta_data = ObjectsUtil.of("meta-data");
    String TAG_package = ObjectsUtil.of("package");
    String TAG_permission = ObjectsUtil.of("permission");
    String TAG_provider = ObjectsUtil.of("provider");
    String TAG_receiver = ObjectsUtil.of("receiver");
    String TAG_service = ObjectsUtil.of("service");
    String TAG_uses_feature = ObjectsUtil.of("uses-feature");
    String TAG_uses_library = ObjectsUtil.of("uses-library");
    String TAG_uses_permission = ObjectsUtil.of("uses-permission");
    String TAG_uses_sdk = ObjectsUtil.of("uses-sdk");

    String VALUE_android_intent_action_MAIN = ObjectsUtil.of("android.intent.action.MAIN");

    String FILE_NAME = ObjectsUtil.of("AndroidManifest.xml");
    String FILE_NAME_BIN = ObjectsUtil.of("AndroidManifest.xml.bin");
    String FILE_NAME_JSON = ObjectsUtil.of("AndroidManifest.xml.json");

    String EMPTY_MANIFEST_TAG = ObjectsUtil.of("x");

}
