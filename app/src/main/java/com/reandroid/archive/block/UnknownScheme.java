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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

// General purpose block to consume the specified bytes of BlockReader
// TODO: No class should override this, implement all like SchemeV2
public class UnknownScheme extends SignatureScheme{
    private final ByteArray byteArray;
    public UnknownScheme(SignatureId signatureId) {
        super(1, signatureId);
        this.byteArray = new ByteArray();
        addChild(byteArray);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        SignatureInfo signatureInfo = getSignatureInfo();
        int size = (int) signatureInfo.getDataSize() - 4;
        byteArray.setSize(size);
        super.onReadBytes(reader);
    }
    @Override
    public Iterator<CertificateBlock> getCertificates() {
        //TODO: implement to all schemes
        return EmptyIterator.of();
    }
}
