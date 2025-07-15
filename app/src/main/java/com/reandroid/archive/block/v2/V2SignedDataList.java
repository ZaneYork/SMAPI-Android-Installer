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

import com.reandroid.archive.block.CertificateBlock;
import com.reandroid.archive.block.LengthPrefixedList;
import com.reandroid.utils.collection.IterableIterator;

import java.util.Iterator;

public class V2SignedDataList extends LengthPrefixedList<V2SignedData> {

    public V2SignedDataList() {
        super(false);
    }

    public Iterator<CertificateBlock> getCertificates() {
        return new IterableIterator<V2SignedData, CertificateBlock>(iterator()) {
            @Override
            public Iterator<CertificateBlock> iterator(V2SignedData element) {
                return element.getCertificates();
            }
        };
    }
    @Override
    public V2SignedData newInstance() {
        return new V2SignedData();
    }
}
