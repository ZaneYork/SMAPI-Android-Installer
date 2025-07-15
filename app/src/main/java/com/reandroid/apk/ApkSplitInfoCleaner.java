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

import com.reandroid.archive.ZipEntryMap;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResValue;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.FilterIterator;

import java.util.Iterator;
import java.util.List;

public class ApkSplitInfoCleaner {

    public static void cleanSplitInfo(ApkModule apkModule){
        AndroidManifestBlock manifestBlock = apkModule.getAndroidManifest();
        cleanSplitInfoAttributes(manifestBlock.getManifestElement());
        cleanSplitInfoMeta(apkModule);
        cleanActivities(apkModule);
    }

    private static void cleanActivities(ApkModule apkModule){
        AndroidManifestBlock manifestBlock = apkModule.getAndroidManifest();
        ResXmlElement manifest = manifestBlock.getManifestElement();
        List<ResXmlElement> removeList = CollectionUtil.toList(
                FilterIterator.of(manifest.recursiveElements(), ApkSplitInfoCleaner::isSplitElement));
        for(ResXmlElement metaElement : removeList){
            cleanElement(apkModule, metaElement);
        }
    }
    private static void cleanSplitInfoMeta(ApkModule apkModule){
        AndroidManifestBlock manifestBlock = apkModule.getAndroidManifest();
        ResXmlElement manifest = manifestBlock.getManifestElement();
        List<ResXmlElement> removeList = CollectionUtil.toList(
                FilterIterator.of(manifest.recursiveElements(), ApkSplitInfoCleaner::isSplitMetaElement));
        for(ResXmlElement metaElement : removeList){
            cleanElement(apkModule, metaElement);
        }
    }
    private static void cleanElement(ApkModule apkModule, ResXmlElement metaElement){
        if(metaElement.getAttributeCount() < 2){
            metaElement.removeSelf();
            return;
        }
        Iterator<ResXmlAttribute> iterator = metaElement.getAttributes();
        while (iterator.hasNext()){
            ResXmlAttribute attribute = iterator.next();
            if(attribute.getValueType() == ValueType.REFERENCE){
                cleanElement(apkModule, attribute.getData());
            }
        }
        metaElement.removeSelf();
    }
    private static void cleanElement(ApkModule apkModule, int resourceId){
        if(resourceId == 0){
            return;
        }
        TableBlock tableBlock = apkModule.getTableBlock();
        List<Entry> resolvedList = tableBlock.resolveReference(resourceId);
        ZipEntryMap zipEntryMap = apkModule.getZipEntryMap();
        for(Entry entry : resolvedList){
            ResValue resValue = entry.getResValue();
            if(resValue != null){
                zipEntryMap.remove(resValue.getValueAsString());
                resValue.setValueAsBoolean(false);
            }
            entry.setNull(true);
            SpecTypePair specTypePair = entry.getTypeBlock().getParentSpecTypePair();
            specTypePair.removeNullEntries(entry.getId());
        }
    }
    private static void cleanSplitInfoAttributes(ResXmlElement manifest){
        List<ResXmlAttribute> removeList = CollectionUtil.toList(
                FilterIterator.of(manifest.recursiveAttributes(), attribute -> {
                    int resourceId = attribute.getNameId();
                    if(resourceId != 0){
                        return resourceId == AndroidManifestBlock.ID_isSplitRequired ||
                                resourceId == AndroidManifestBlock.ID_isFeatureSplit ||
                                resourceId == AndroidManifestBlock.ID_extractNativeLibs;
                    }
                    return attribute.equalsName(AndroidManifestBlock.NAME_requiredSplitTypes) ||
                            attribute.equalsName(AndroidManifestBlock.NAME_splitTypes);

                }));
        for(ResXmlAttribute attribute : removeList){
            attribute.removeSelf();
        }
    }
    static boolean isSplitElement(ResXmlElement element){
        return element != null &&

                ( element.equalsName(AndroidManifestBlock.TAG_activity) ||
                element.equalsName(AndroidManifestBlock.TAG_service) ) &&

                isSplitElement(AndroidManifestBlock.getAndroidNameValue(element));
    }
    private static boolean isSplitElement(String name){
        if(name == null){
            return false;
        }
        return name.startsWith("com.google.android.play.core.missingsplits.")
                || name.startsWith("com.google.android.play.core.assetpacks.");
    }
    static boolean isSplitMetaElement(ResXmlElement element){
        return element != null &&
                element.equalsName(AndroidManifestBlock.TAG_meta_data) &&
                isSplitMetaNamePrefix(AndroidManifestBlock.getAndroidNameValue(element));
    }
    private static boolean isSplitMetaNamePrefix(String name){
        if(name == null){
            return false;
        }
        return name.startsWith("com.android.vending.")
                || name.startsWith("com.android.stamp.")
                || name.startsWith("com.android.dynamic.apk.");
    }
}
