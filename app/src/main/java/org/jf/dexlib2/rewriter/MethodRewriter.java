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

import org.jf.dexlib2.HiddenApiRestriction;
import org.jf.dexlib2.base.reference.BaseMethodReference;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;

import java.util.List;
import java.util.Set;

public class MethodRewriter implements Rewriter<Method> {
    
    protected final Rewriters rewriters;

    public MethodRewriter( Rewriters rewriters) {
        this.rewriters = rewriters;
    }

    
    @Override
    public Method rewrite( Method value) {
        return new RewrittenMethod(value);
    }

    protected class RewrittenMethod extends BaseMethodReference implements Method {
         protected Method method;

        public RewrittenMethod( Method method) {
            this.method = method;
        }

        @Override  public String getDefiningClass() {
            return rewriters.getMethodReferenceRewriter().rewrite(method).getDefiningClass();
        }

        @Override  public String getName() {
            return rewriters.getMethodReferenceRewriter().rewrite(method).getName();
        }

        @Override  public List<? extends CharSequence> getParameterTypes() {
            return rewriters.getMethodReferenceRewriter().rewrite(method).getParameterTypes();
        }

        @Override  public List<? extends MethodParameter> getParameters() {
            // We can't use the MethodReferenceRewriter to rewrite the parameters, because we would lose
            // parameter names and annotations. If a method rewrite involves changing parameters, it needs
            // to be handled here as well as in the MethodReferenceRewriter

            return RewriterUtils.rewriteList(rewriters.getMethodParameterRewriter(), method.getParameters());
        }

        @Override  public String getReturnType() {
            return rewriters.getMethodReferenceRewriter().rewrite(method).getReturnType();
        }

        @Override public int getAccessFlags() {
            return method.getAccessFlags();
        }

        @Override  public Set<? extends Annotation> getAnnotations() {
            return RewriterUtils.rewriteSet(rewriters.getAnnotationRewriter(), method.getAnnotations());
        }

         @Override public Set<HiddenApiRestriction> getHiddenApiRestrictions() {
            return method.getHiddenApiRestrictions();
        }

        @Override  public MethodImplementation getImplementation() {
            return RewriterUtils.rewriteNullable(rewriters.getMethodImplementationRewriter(),
                    method.getImplementation());
        }
    }
}
