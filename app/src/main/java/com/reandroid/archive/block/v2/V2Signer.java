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

import com.reandroid.archive.block.*;
import com.reandroid.utils.collection.InstanceIterator;

import java.util.Iterator;
import java.util.List;

public class V2Signer extends LengthPrefixedBlock {
    private final V2Signature v2Signature;
    private final CertificateBlockList certificateBlockList;
    private final BottomBlock unknown;
    public V2Signer() {
        super(3, false);
        this.v2Signature = new V2Signature();
        this.certificateBlockList = new CertificateBlockList();
        this.unknown = new BottomBlock();
        addChild(this.v2Signature);
        addChild(this.certificateBlockList);
        addChild(this.unknown);
    }
    public Iterator<CertificateBlock> getCertificates(){
        return InstanceIterator.of(certificateBlockList.iterator(), CertificateBlock.class);
    }
    public void addCertificateBlock(CertificateBlockV2 certificateBlockV2){
        certificateBlockList.add(certificateBlockV2);
    }
    public void removeCertificateBlock(CertificateBlockV2 certificateBlockV2){
        certificateBlockList.remove(certificateBlockV2);
    }
    @Override
    public String toString(){
        return super.toString()+", sig="+v2Signature+", certs="+certificateBlockList;
    }
}
