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
package com.reandroid.dex.refactor;

import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.KeyPair;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.key.TypeKeyReference;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.ObjectsUtil;

import java.util.*;

public class RenameTypes extends Rename<TypeKey, TypeKey>{

    private int arrayDepth;
    private boolean renameSignatures;
    private boolean renameSource;
    private boolean noRenameSourceForNoPackageClass;
    private boolean fixAccessibility;
    private Set<String> renamedStrings;

    public RenameTypes(){
        super();
        this.arrayDepth = DEFAULT_ARRAY_DEPTH;
        this.renameSignatures = true;
        this.renameSource = true;
        this.noRenameSourceForNoPackageClass = true;
        this.fixAccessibility = true;
        this.renamedStrings = new HashSet<>();
    }

    @Override
    public int apply(DexClassRepository classRepository) {
        Map<String, String> map = buildRenameMap();
        this.renamedStrings = new HashSet<>(map.size());
        renameStringIds(classRepository, map);
        renameExternalTypeKeyReferences(classRepository, map);
        int size = renamedStrings.size();
        if(size != 0) {
            classRepository.clearPoolMap();
        }
        fixAccessibility(classRepository);
        renamedStrings.clear();
        renamedStrings = null;
        return size;
    }
    private void renameStringIds(DexClassRepository classRepository, Map<String, String> map) {
        Iterator<StringId> iterator = classRepository.getClonedItems(SectionType.STRING_ID);
        while (iterator.hasNext()){
            StringId stringId = iterator.next();
            stringId.addUsageType(UsageMarker.USAGE_DEFINITION);
            String text = map.get(stringId.getString());
            if(text != null){
                setString(stringId, text);
            }else {
                renameSignatures(map, stringId);
            }
        }
    }
    private void renameExternalTypeKeyReferences(DexClassRepository classRepository, Map<String, String> map) {
        List<TypeKeyReference> referenceList = classRepository.getExternalTypeKeyReferenceList();
        for(TypeKeyReference reference : referenceList) {
            renameExternalTypeKeyReference(reference, map);
        }
    }
    private void renameExternalTypeKeyReference(TypeKeyReference reference, Map<String, String> map) {
        TypeKey typeKey = reference.getTypeKey();
        if(typeKey == null) {
            return;
        }
        String replace = map.get(typeKey.getTypeName());
        if(replace == null) {
            replace = map.get(typeKey.getSourceName());
        }
        TypeKey replaceKey = TypeKey.parse(replace);
        if(replaceKey != null) {
            reference.setTypeKey(replaceKey);
            renamedStrings.add(replace);
        }
    }
    private void fixAccessibility(DexClassRepository classRepository) {
        Set<String> renamedSet = this.renamedStrings;
        if(!this.fixAccessibility || renamedSet == null || renamedSet.isEmpty()) {
            return;
        }
        Iterator<DexClass> iterator = classRepository.getDexClasses(
                typeKey -> renamedSet.contains(typeKey.getTypeName()));
        while (iterator.hasNext()) {
            iterator.next().fixAccessibility();
        }
    }
    private void renameSignatures(Map<String, String> map, StringId stringId){
        if(!stringId.containsUsage(UsageMarker.USAGE_SIGNATURE_TYPE)){
            return;
        }
        String text = stringId.getString();
        if(text.indexOf('L') < 0){
            return;
        }
        String[] signatures = DexUtils.splitSignatures(text);
        int length = signatures.length;
        boolean found = false;
        for(int i = 0; i < length; i++){
            String type = signatures[i];
            String replace = map.get(type);
            if(replace != null){
                signatures[i] = replace;
                found = true;
            }
        }
        if(found){
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < length; i++){
                builder.append(signatures[i]);
            }
            setString(stringId, builder.toString());
        }
    }
    private void setString(StringId stringId, String value) {
        stringId.setString(value);
        renamedStrings.add(value);
    }

    public void setArrayDepth(int arrayDepth) {
        if(arrayDepth < 0){
            arrayDepth = DEFAULT_ARRAY_DEPTH;
        }
        this.arrayDepth = arrayDepth;
    }
    public void setRenameSignatures(boolean renameSignatures) {
        this.renameSignatures = renameSignatures;
    }
    public void setRenameSource(boolean renameSource) {
        this.renameSource = renameSource;
    }
    public void setNoRenameSourceForNoPackageClass(boolean noRenameSourceForNoPackageClass) {
        this.noRenameSourceForNoPackageClass = noRenameSourceForNoPackageClass;
    }
    public void setFixAccessibility(boolean fixAccessibility) {
        this.fixAccessibility = fixAccessibility;
    }

    private Map<String, String> buildRenameMap() {
        List<KeyPair<TypeKey, TypeKey>> list = sortedList();
        boolean renameSignatures = this.renameSignatures;
        boolean renameSource = this.renameSource;
        boolean noRenameSourceForNoPackageClass = this.noRenameSourceForNoPackageClass;

        int estimatedSize = 1;
        if(renameSignatures){
            estimatedSize = estimatedSize + 2;
        }
        if(renameSource){
            estimatedSize = estimatedSize + 1;
        }
        if(arrayDepth > 0){
            estimatedSize = estimatedSize + arrayDepth + 1;
        }
        estimatedSize = list.size() * estimatedSize;

        Map<String, String> map = new HashMap<>(estimatedSize);

        int size = list.size();
        int arrayDepth = this.arrayDepth + 1;

        for(int i = 0; i < size; i++){

            KeyPair<TypeKey, TypeKey> keyPair = list.get(i);
            TypeKey first = keyPair.getFirst();
            TypeKey second = keyPair.getSecond();

            String name1 = first.getTypeName();
            String name2 = second.getTypeName();
            map.put(name1, name2);

            if(renameSignatures){
                name1 = first.getTypeName();
                name2 = second.getTypeName();

                name1 = name1.replace(';', '<');
                name2 = name2.replace(';', '<');

                map.put(name1, name2);

                name1 = first.getTypeName();
                name2 = second.getTypeName();

                name1 = name1.substring(0, name1.length() - 1);
                name2 = name2.substring(0, name2.length() - 1);

                map.put(name1, name2);
            }

            for(int j = 1; j < arrayDepth; j++){
                name1 = first.getArrayType(j);
                name2 = second.getArrayType(j);
                map.put(name1, name2);
                if(renameSignatures && j == 1){
                    name1 = name1.replace(';', '<');
                    name2 = name2.replace(';', '<');
                    map.put(name1, name2);
                }
            }
            if(renameSource){
                name1 = first.getTypeName();
                if(!noRenameSourceForNoPackageClass || name1.indexOf('/') > 0){
                    name1 = first.getSourceName();
                    name2 = second.getSourceName();
                    map.put(name1, name2);
                }
            }
        }
        return map;
    }

    private Map<String, String> buildSignatureRenameMap() {
        List<KeyPair<TypeKey, TypeKey>> list = sortedList();
        boolean renameSignatures = this.renameSignatures;
        boolean renameSource = this.renameSource;
        boolean noRenameSourceForNoPackageClass = this.noRenameSourceForNoPackageClass;

        int estimatedSize = 1;
        if(renameSignatures){
            estimatedSize = estimatedSize + 2;
        }
        if(renameSource){
            estimatedSize = estimatedSize + 1;
        }
        if(arrayDepth > 0){
            estimatedSize = estimatedSize + arrayDepth + 1;
        }
        estimatedSize = list.size() * estimatedSize;

        Map<String, String> map = new HashMap<>(estimatedSize);

        int size = list.size();
        int arrayDepth = this.arrayDepth + 1;

        for(int i = 0; i < size; i++){

            KeyPair<TypeKey, TypeKey> keyPair = list.get(i);
            TypeKey first = keyPair.getFirst();
            TypeKey second = keyPair.getSecond();

            String name1 = first.getTypeName();
            String name2 = second.getTypeName();
            map.put(name1, name2);

            if(renameSignatures){
                name1 = first.getTypeName();
                name2 = second.getTypeName();

                name1 = name1.replace(';', '<');
                name2 = name2.replace(';', '<');

                map.put(name1, name2);

                name1 = first.getTypeName();
                name2 = second.getTypeName();

                name1 = name1.substring(0, name1.length() - 1);
                name2 = name2.substring(0, name2.length() - 1);

                map.put(name1, name2);
            }

            for(int j = 1; j < arrayDepth; j++){
                name1 = first.getArrayType(j);
                name2 = second.getArrayType(j);
                map.put(name1, name2);
                if(renameSignatures && j == 1){
                    name1 = name1.replace(';', '<');
                    name2 = name2.replace(';', '<');
                    map.put(name1, name2);
                }
            }
            if(renameSource){
                name1 = first.getTypeName();
                if(!noRenameSourceForNoPackageClass || name1.indexOf('/') > 0){
                    name1 = first.getSourceName();
                    name2 = second.getSourceName();
                    map.put(name1, name2);
                }
            }
        }
        return map;
    }

    public static final int DEFAULT_ARRAY_DEPTH = ObjectsUtil.of(3);
}
