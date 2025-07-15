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
package com.reandroid.archive.block.v2;

import com.reandroid.archive.block.BottomBlock;
import com.reandroid.archive.block.CertificateBlock;
import com.reandroid.archive.block.LengthPrefixedBlock;

import java.util.Iterator;

public class V2SignedData extends LengthPrefixedBlock {

    private final V2Signer signer;
    private final BottomBlock unknown;

    public V2SignedData() {
        super(2, false);
        this.signer = new V2Signer();
        this.unknown = new BottomBlock();
        addChild(this.signer);
        addChild(this.unknown);
    }

    public Iterator<CertificateBlock> getCertificates(){
        return getSigner().getCertificates();
    }
    public V2Signer getSigner() {
        return signer;
    }
}
