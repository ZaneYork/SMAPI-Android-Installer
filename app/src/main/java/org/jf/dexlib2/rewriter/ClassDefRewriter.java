/*
 * Copyright 2014, Google Inc.
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

package org.jf.dexlib2.rewriter;

import org.jf.dexlib2.base.reference.BaseTypeReference;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.util.collection.Iterables;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ClassDefRewriter implements Rewriter<ClassDef> {

    protected final Rewriters rewriters;

    public ClassDefRewriter( Rewriters rewriters) {
        this.rewriters = rewriters;
    }


    @Override
    public ClassDef rewrite( ClassDef classDef) {
        return new RewrittenClassDef(classDef);
    }

    protected class RewrittenClassDef extends BaseTypeReference implements ClassDef {
         protected ClassDef classDef;

        public RewrittenClassDef( ClassDef classdef) {
            this.classDef = classdef;
        }

        @Override  public String getType() {
            return rewriters.getTypeRewriter().rewrite(classDef.getType());
        }

        @Override public int getAccessFlags() {
            return classDef.getAccessFlags();
        }

        @Override  public String getSuperclass() {
            return RewriterUtils.rewriteNullable(rewriters.getTypeRewriter(), classDef.getSuperclass());
        }

        @Override  public List<String> getInterfaces() {
            return RewriterUtils.rewriteList(rewriters.getTypeRewriter(), classDef.getInterfaces());
        }

        @Override  public String getSourceFile() {
            return classDef.getSourceFile();
        }

        @Override  public Set<? extends Annotation> getAnnotations() {
            return RewriterUtils.rewriteSet(rewriters.getAnnotationRewriter(), classDef.getAnnotations());
        }

        @Override  public Iterable<? extends Field> getStaticFields() {
            return RewriterUtils.rewriteIterable(rewriters.getFieldRewriter(), classDef.getStaticFields());
        }

        @Override  public Iterable<? extends Field> getInstanceFields() {
            return RewriterUtils.rewriteIterable(rewriters.getFieldRewriter(), classDef.getInstanceFields());
        }


        @Override
        public Iterable<? extends Field> getFields() {
            return new Iterable<Field>() {

                @Override
                public Iterator<Field> iterator() {
                    return Iterables.concat(getStaticFields().iterator(), getInstanceFields().iterator());
                }
            };
        }

        @Override  public Iterable<? extends Method> getDirectMethods() {
            return RewriterUtils.rewriteIterable(rewriters.getMethodRewriter(), classDef.getDirectMethods());
        }

        @Override  public Iterable<? extends Method> getVirtualMethods() {
            return RewriterUtils.rewriteIterable(rewriters.getMethodRewriter(), classDef.getVirtualMethods());
        }


        @Override
        public Iterable<? extends Method> getMethods() {
            return new Iterable<Method>() {

                @Override
                public Iterator<Method> iterator() {
                    return Iterables.concat(getDirectMethods().iterator(), getVirtualMethods().iterator());
                }
            };
        }
    }
}
