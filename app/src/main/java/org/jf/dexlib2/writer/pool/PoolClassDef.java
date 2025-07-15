/*
 * Copyright 2013, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.dexlib2.writer.pool;

import org.jf.dexlib2.base.reference.BaseTypeReference;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.util.collection.ArraySet;
import org.jf.util.collection.Iterables;
import org.jf.util.collection.ListUtil;

import java.util.Collection;
import java.util.List;
import java.util.Set;

class PoolClassDef extends BaseTypeReference implements ClassDef {
     final ClassDef classDef;
     final TypeListPool.Key<List<String>> interfaces;
     final Set<Field> staticFields;
     final Set<Field> instanceFields;
     final Set<PoolMethod> directMethods;
     final Set<PoolMethod> virtualMethods;

    int classDefIndex = DexPool.NO_INDEX;
    int annotationDirectoryOffset = DexPool.NO_OFFSET;

    PoolClassDef( ClassDef classDef) {
        this.classDef = classDef;

        interfaces = new TypeListPool.Key<List<String>>(ListUtil.copyOf(classDef.getInterfaces()));
        staticFields = ArraySet.copyOf(classDef.getStaticFields());
        instanceFields = ArraySet.copyOf(classDef.getInstanceFields());
        directMethods = ArraySet.copyOf(
                Iterables.transform(classDef.getDirectMethods(), PoolMethod.TRANSFORM));
        virtualMethods = ArraySet.copyOf(
                Iterables.transform(classDef.getVirtualMethods(), PoolMethod.TRANSFORM));
    }

    
    @Override
    public String getType() {
        return classDef.getType();
    }

    @Override
    public int getAccessFlags() {
        return classDef.getAccessFlags();
    }

    
    @Override
    public String getSuperclass() {
        return classDef.getSuperclass();
    }

    
    @Override
    public List<String> getInterfaces() {
        return interfaces.types;
    }

    
    @Override
    public String getSourceFile() {
        return classDef.getSourceFile();
    }

    
    @Override
    public Set<? extends Annotation> getAnnotations() {
        return classDef.getAnnotations();
    }

    
    @Override
    public Set<Field> getStaticFields() {
        return staticFields;
    }

    
    @Override
    public Set<Field> getInstanceFields() {
        return instanceFields;
    }

    
    @Override
    public Collection<Field> getFields() {
        return ListUtil.sortedCopy(
                Iterables.concat(staticFields.iterator(), instanceFields.iterator()));
    }

    
    @Override
    public Set<PoolMethod> getDirectMethods() {
        return directMethods;
    }

    
    @Override
    public Set<PoolMethod> getVirtualMethods() {
        return virtualMethods;
    }

    
    @Override
    public Collection<PoolMethod> getMethods() {
        return ListUtil.sortedCopy(
                Iterables.concat(directMethods.iterator(), virtualMethods.iterator()));
    }
}
