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
package com.reandroid.arsc.chunk.xml;

import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;

public class ResXmlEndNamespace extends ResXmlNamespaceChunk {
    public ResXmlEndNamespace() {
        super(ChunkType.XML_END_NAMESPACE);
    }
    public ResXmlStartNamespace getStart(){
        return (ResXmlStartNamespace) getPair();
    }
    public void setStart(ResXmlStartNamespace namespace){
        setPair(namespace);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock = reader.readHeaderBlock();
        if(headerBlock.getChunkSize() < 8){
            super.onReadChildes(reader);
        }else {
            super.onReadBytes(reader);
        }
    }
}
