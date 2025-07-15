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
package com.reandroid.archive.block;

import com.reandroid.arsc.container.ExpandableBlockContainer;

import java.util.Iterator;

public abstract class SignatureScheme extends ExpandableBlockContainer {

    private final SignatureId signatureId;

    public SignatureScheme(int childesCount, SignatureId signatureId){
        super(childesCount);
        this.signatureId = signatureId;
    }

    public abstract Iterator<CertificateBlock> getCertificates();
    public SignatureId getSignatureId() {
        return signatureId;
    }

    public SignatureInfo getSignatureInfo(){
        return getParent(SignatureInfo.class);
    }
    @Override
    public String toString(){
        return "id=" + getSignatureId();
    }
}
