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

import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.value.EncodedValue;


public class RewriterModule {

    public Rewriter<DexFile> getDexFileRewriter( Rewriters rewriters) {
        return new DexFileRewriter(rewriters);
    }


    public Rewriter<ClassDef> getClassDefRewriter( Rewriters rewriters) {
        return new ClassDefRewriter(rewriters);
    }


    public Rewriter<Field> getFieldRewriter( Rewriters rewriters) {
        return new FieldRewriter(rewriters);
    }


    public Rewriter<Method> getMethodRewriter( Rewriters rewriters) {
        return new MethodRewriter(rewriters);
    }


    public Rewriter<MethodParameter> getMethodParameterRewriter( Rewriters rewriters) {
        return new MethodParameterRewriter(rewriters);
    }


    public Rewriter<MethodImplementation> getMethodImplementationRewriter( Rewriters rewriters) {
        return new MethodImplementationRewriter(rewriters);
    }


    public Rewriter<Instruction> getInstructionRewriter( Rewriters rewriters) {
        return new InstructionRewriter(rewriters);
    }


    public Rewriter<TryBlock<? extends ExceptionHandler>> getTryBlockRewriter( Rewriters rewriters) {
        return new TryBlockRewriter(rewriters);
    }


    public Rewriter<ExceptionHandler> getExceptionHandlerRewriter( Rewriters rewriters) {
        return new ExceptionHandlerRewriter(rewriters);
    }


    public Rewriter<DebugItem> getDebugItemRewriter( Rewriters rewriters) {
        return new DebugItemRewriter(rewriters);
    }


    public Rewriter<String> getTypeRewriter( Rewriters rewriters) {
        return new TypeRewriter();
    }


    public Rewriter<FieldReference> getFieldReferenceRewriter( Rewriters rewriters) {
        return new FieldReferenceRewriter(rewriters);
    }


    public Rewriter<MethodReference> getMethodReferenceRewriter( Rewriters rewriters) {
        return new MethodReferenceRewriter(rewriters);
    }


    public Rewriter<Annotation> getAnnotationRewriter( Rewriters rewriters) {
        return new AnnotationRewriter(rewriters);
    }


    public Rewriter<AnnotationElement> getAnnotationElementRewriter( Rewriters rewriters) {
        return new AnnotationElementRewriter(rewriters);
    }


    public Rewriter<EncodedValue> getEncodedValueRewriter( Rewriters rewriters) {
        return new EncodedValueRewriter(rewriters);
    }
}
