/*
 * Copyright 2020, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * Neither the name of Google Inc. nor the names of its
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

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.extra.DexMarker;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DexFileRewriter implements Rewriter<DexFile> {
    
    protected final Rewriters rewriters;

    public DexFileRewriter( Rewriters rewriters) {
        this.rewriters = rewriters;
    }

    
    @Override
    public DexFile rewrite( DexFile value) {
        return new RewrittenDexFile(value);
    }

    protected class RewrittenDexFile implements DexFile {
        
        protected final DexFile dexFile;
        private final List<DexMarker> markerList;

        public RewrittenDexFile( DexFile dexFile) {
            this.dexFile = dexFile;
            this.markerList = new ArrayList<>(dexFile.getMarkers());
        }

        @Override
        public List<DexMarker> getMarkers() {
            return markerList;
        }
        
        @Override
        public Set<? extends ClassDef> getClasses() {
            return RewriterUtils.rewriteSet(rewriters.getClassDefRewriter(), dexFile.getClasses());
        }

        
        @Override
        public Opcodes getOpcodes() {
            return dexFile.getOpcodes();
        }
    }
}
