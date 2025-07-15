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

public class CertificateBlockV2 extends LengthPrefixedBytes implements CertificateBlock{

    public CertificateBlockV2() {
        super(false);
    }
    @Override
    public byte[] getCertificateBytes() {
        return getByteArray().toArray().clone();
    }
    @Override
    public void setCertificate(byte[] bytes){
        getByteArray().set(bytes);
    }
}
