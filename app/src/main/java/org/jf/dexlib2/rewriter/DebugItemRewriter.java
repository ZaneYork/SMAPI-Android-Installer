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

import org.jf.dexlib2.DebugItemType;
import org.jf.dexlib2.iface.debug.*;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.iface.reference.TypeReference;


public class DebugItemRewriter implements Rewriter<DebugItem> {

    protected final Rewriters rewriters;

    public DebugItemRewriter( Rewriters rewriters) {
        this.rewriters = rewriters;
    }


    @Override
    public DebugItem rewrite( DebugItem value) {
        switch (value.getDebugItemType()) {
            case DebugItemType.START_LOCAL:
                return new RewrittenStartLocal((StartLocal)value);
            case DebugItemType.END_LOCAL:
                return new RewrittenEndLocal((EndLocal)value);
            case DebugItemType.RESTART_LOCAL:
                return new RewrittenRestartLocal((RestartLocal)value);
            default:
                return value;
        }
    }

    protected class BaseRewrittenLocalInfoDebugItem<T extends DebugItem & LocalInfo> implements DebugItem, LocalInfo {
         protected T debugItem;

        public BaseRewrittenLocalInfoDebugItem ( T debugItem) {
            this.debugItem = debugItem;
        }

        @Override public int getDebugItemType() {
            return debugItem.getDebugItemType();
        }

        @Override public int getCodeAddress() {
            return debugItem.getCodeAddress();
        }

        @Override  public String getName() {
            return debugItem.getName();
        }

        @Override  public String getType() {
            return RewriterUtils.rewriteNullable(rewriters.getTypeRewriter(), debugItem.getType());
        }

        @Override  public String getSignature() {
            return debugItem.getSignature();
        }
    }

    protected class RewrittenStartLocal extends BaseRewrittenLocalInfoDebugItem<StartLocal> implements StartLocal {
        public RewrittenStartLocal( StartLocal debugItem) {
            super(debugItem);
        }

        @Override public int getRegister() {
            return debugItem.getRegister();
        }

        @Override  public StringReference getNameReference() {
            return debugItem.getNameReference();
        }

        @Override  public TypeReference getTypeReference() {
            TypeReference typeReference = debugItem.getTypeReference();
            if (typeReference == null) {
                return null;
            }

            return RewriterUtils.rewriteTypeReference(rewriters.getTypeRewriter(), typeReference);
        }

        @Override  public StringReference getSignatureReference() {
            return debugItem.getSignatureReference();
        }
    }

    protected class RewrittenEndLocal extends BaseRewrittenLocalInfoDebugItem<EndLocal> implements EndLocal {
        public RewrittenEndLocal( EndLocal instruction) {
            super(instruction);
        }

        public int getRegister() {
            return debugItem.getRegister();
        }
    }

    protected class RewrittenRestartLocal extends BaseRewrittenLocalInfoDebugItem<RestartLocal>
            implements RestartLocal {
        public RewrittenRestartLocal( RestartLocal instruction) {
            super(instruction);
        }

        public int getRegister() {
            return debugItem.getRegister();
        }
    }
}
