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

import com.reandroid.archive.block.pad.SchemePadding;
import com.reandroid.archive.block.stamp.SchemeStampV1;
import com.reandroid.archive.block.stamp.SchemeStampV2;
import com.reandroid.archive.block.v2.SchemeV2;
import com.reandroid.archive.block.v3.SchemeV3;
import com.reandroid.archive.block.v3.SchemeV31;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class SignatureInfo extends LengthPrefixedBlock implements BlockLoad {

    private final IntegerItem idItem;
    private final SingleBlockContainer<SignatureScheme> schemeContainer;

    public SignatureInfo() {
        super(2, true);
        this.idItem = new IntegerItem();
        this.schemeContainer = new SingleBlockContainer<>();
        addChild(this.idItem);
        addChild(this.schemeContainer);
        this.idItem.setBlockLoad(this);
    }

    public Iterator<CertificateBlock> getCertificates() {
        SignatureScheme scheme = getSignatureScheme();
        if(scheme != null){
            return scheme.getCertificates();
        }
        return EmptyIterator.of();
    }

    public int getIdValue(){
        return idItem.get();
    }
    public SignatureId getId(){
        return SignatureId.valueOf(getIdValue());
    }
    public void setId(int id){
        idItem.set(id);
    }
    public void setId(SignatureId signatureId){
        setId(signatureId == null? 0 : signatureId.getId());
    }
    public SignatureScheme getSignatureScheme(){
        return schemeContainer.getItem();
    }
    public void setSignatureScheme(SignatureScheme signatureScheme){
        schemeContainer.setItem(signatureScheme);
    }

    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender == this.idItem){
            onIdLoaded();
        }
    }
    private void onIdLoaded(){
        SignatureId signatureId = getId();
        SignatureScheme scheme;
        if(signatureId == SignatureId.V2){
            scheme = new SchemeV2();
        }else if(signatureId == SignatureId.V3){
            scheme = new SchemeV3();
        }else if(signatureId == SignatureId.V31){
            scheme = new SchemeV31();
        }else if(signatureId == SignatureId.STAMP_V1){
            scheme = new SchemeStampV1();
        }else if(signatureId == SignatureId.STAMP_V2){
            scheme = new SchemeStampV2();
        }else if(signatureId == SignatureId.PADDING){
            scheme = new SchemePadding();
        }else {
            scheme = new UnknownScheme(signatureId);
        }
        schemeContainer.setItem(scheme);
    }
    public void writeRaw(File file) throws IOException{
        File dir = file.getParentFile();
        if(dir != null && !dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        writeBytes(outputStream);
        outputStream.close();
    }
    public File writeRawToDirectory(File dir) throws IOException{
        String name = getIndex() + "_" + getId().toFileName();
        File file = new File(dir, name);
        writeRaw(file);
        return file;
    }
    public void read(File file) throws IOException {
        super.readBytes(new BlockReader(file));
    }
    @Override
    public String toString() {
        return getId() + ", scheme: " + getSignatureScheme();
    }

}
