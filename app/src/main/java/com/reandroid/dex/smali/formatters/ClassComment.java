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
package com.reandroid.dex.smali.formatters;

import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;
import java.util.Iterator;

public interface ClassComment extends SmaliComment {

    void writeComment(SmaliWriter writer, TypeKey typeKey) throws IOException;

    class ClassExtendComment implements ClassComment {

        private final DexClassRepository classRepository;

        public ClassExtendComment(DexClassRepository classRepository) {
            this.classRepository = classRepository;
        }
        @Override
        public void writeComment(SmaliWriter writer, TypeKey typeKey) throws IOException {
            DexClass dexClass = classRepository.getDexClass(typeKey);
            if(dexClass == null || dexClass.isFinal()) {
                return;
            }
            Iterator<DexClass> iterator = dexClass.getExtending();
            while (iterator.hasNext()) {
                TypeKey key = iterator.next().getKey();
                writer.appendComment("extended-by: ");
                writer.appendComment(key.getTypeName());
                writer.newLine();
            }

        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ClassExtendComment)) {
                return false;
            }
            ClassExtendComment that = (ClassExtendComment) obj;
            return classRepository == that.classRepository;
        }
        @Override
        public int hashCode() {
            return ObjectsUtil.hash(classRepository, getClass());
        }
    }
    class ClassImplementComment implements ClassComment {

        private final DexClassRepository classRepository;

        public ClassImplementComment(DexClassRepository classRepository) {
            this.classRepository = classRepository;
        }
        @Override
        public void writeComment(SmaliWriter writer, TypeKey typeKey) throws IOException {
            DexClass dexClass = classRepository.getDexClass(typeKey);
            if(dexClass == null || !dexClass.isInterface()) {
                return;
            }
            Iterator<DexClass> iterator = dexClass.getImplementations();
            while (iterator.hasNext()) {
                TypeKey key = iterator.next().getKey();
                writer.appendComment("implemented-by: ");
                writer.appendComment(key.getTypeName());
                writer.newLine();
            }
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ClassImplementComment)) {
                return false;
            }
            ClassImplementComment that = (ClassImplementComment) obj;
            return classRepository == that.classRepository;
        }
        @Override
        public int hashCode() {
            return ObjectsUtil.hash(classRepository, getClass());
        }
    }
}
