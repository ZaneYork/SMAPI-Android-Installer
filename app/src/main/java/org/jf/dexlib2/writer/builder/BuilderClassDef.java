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

package org.jf.dexlib2.writer.builder;

import org.jf.dexlib2.base.reference.BaseTypeReference;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.util.MethodUtil;
import org.jf.dexlib2.writer.DexWriter;
import org.jf.dexlib2.writer.builder.BuilderEncodedValues.BuilderArrayEncodedValue;
import org.jf.util.collection.ArraySet;
import org.jf.util.collection.Iterables;
import org.jf.util.collection.ListUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class BuilderClassDef extends BaseTypeReference implements ClassDef {
     final BuilderTypeReference type;
    final int accessFlags;
     final BuilderTypeReference superclass;
     final BuilderTypeList interfaces;
     final BuilderStringReference sourceFile;
     final BuilderAnnotationSet annotations;
     final Set<BuilderField> staticFields;
     final Set<BuilderField> instanceFields;
     final Set<BuilderMethod> directMethods;
     final Set<BuilderMethod> virtualMethods;
     final BuilderArrayEncodedValue staticInitializers;

    int classDefIndex = DexWriter.NO_INDEX;
    int annotationDirectoryOffset = DexWriter.NO_OFFSET;

    BuilderClassDef( BuilderTypeReference type,
                    int accessFlags,
                     BuilderTypeReference superclass,
                     BuilderTypeList interfaces,
                     BuilderStringReference sourceFile,
                     BuilderAnnotationSet annotations,
                     Set<BuilderField> staticFields,
                     Set<BuilderField> instanceFields,
                     Iterable<? extends BuilderMethod> methods,
                     BuilderArrayEncodedValue staticInitializers) {
        if (methods == null) {
            methods = ListUtil.of();
        }
        if (staticFields == null) {
            staticFields = ArraySet.of();
        }
        if (instanceFields == null) {
            instanceFields = ArraySet.of();
        }

        this.type = type;
        this.accessFlags = accessFlags;
        this.superclass = superclass;
        this.interfaces = interfaces;
        this.sourceFile = sourceFile;
        this.annotations = annotations;
        this.staticFields = staticFields;
        this.instanceFields = instanceFields;
        ArraySet<BuilderMethod> set = ArraySet.copyOf(Iterables.filter(methods, MethodUtil.METHOD_IS_DIRECT));
        this.directMethods = set.sort();
        set=ArraySet.copyOf(Iterables.filter(methods, MethodUtil.METHOD_IS_VIRTUAL));;
        this.virtualMethods = set.sort();
        this.staticInitializers = staticInitializers;
    }


    @Override
    public String getType() { return type.getType(); }
    @Override
    public int getAccessFlags() { return accessFlags; }
    
    @Override
    public String getSuperclass() { return superclass==null?null:superclass.getType(); }
    
    @Override
    public String getSourceFile() { return sourceFile==null?null:sourceFile.getString(); }

    @Override
    public BuilderAnnotationSet getAnnotations() { return annotations; }

    @Override
    public Set<BuilderField> getStaticFields() { return staticFields; }

    @Override
    public Set<BuilderField> getInstanceFields() { return instanceFields; }

    @Override
    public Set<BuilderMethod> getDirectMethods() { return directMethods; }

    @Override
    public Set<BuilderMethod> getVirtualMethods() { return virtualMethods; }


    @Override
    public List<String> getInterfaces() {
        return ListUtil.transform(this.interfaces, new Function<BuilderTypeReference, String>() {
            @Override
            public String apply(BuilderTypeReference builderTypeReference) {
                return builderTypeReference.toString();
            }
        });
    }


    @Override
    public Collection<BuilderField> getFields() {
        ArraySet<BuilderField> results = new ArraySet<>(staticFields.size() + instanceFields.size());
        results.addAll(staticFields);
        results.addAll(instanceFields);
        results.sort();
        return results;
    }


    @Override
    public Collection<BuilderMethod> getMethods() {
        ArraySet<BuilderMethod> results = new ArraySet<>(directMethods.size() + virtualMethods.size());
        results.addAll(directMethods);
        results.addAll(virtualMethods);
        results.sort();
        return results;
    }
}
