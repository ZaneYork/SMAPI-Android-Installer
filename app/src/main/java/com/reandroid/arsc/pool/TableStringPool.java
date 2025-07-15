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
package com.reandroid.arsc.pool;

import com.reandroid.arsc.array.OffsetArray;
import com.reandroid.arsc.array.StringArray;
import com.reandroid.arsc.array.TableStringArray;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.UnknownChunk;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.header.TableHeader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.TableString;
import com.reandroid.xml.StyleDocument;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class TableStringPool extends StringPool<TableString> {
    public TableStringPool(boolean is_utf8) {
        super(is_utf8);
    }

    public TableString getOrCreateStyled(String xmlString) throws IOException, XmlPullParserException {
        return getOrCreate(StyleDocument.parseStyledString(xmlString));
    }
    public TableString getOrCreate(XmlPullParser parser) throws IOException, XmlPullParserException {
        return getOrCreate(StyleDocument.parseNext(parser));
    }
    @Override
    void linkStrings(){
        super.linkStrings();
        TableBlock tableBlock = getParentInstance(TableBlock.class);
        if(tableBlock != null){
            tableBlock.linkTableStringsInternal(this);
        }
    }
    @Override
    StringArray<TableString> newInstance(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        return new TableStringArray(offsets, itemCount, itemStart, is_utf8);
    }

    /**
     * Loads string pool only from table block (resources.arsc) without
     * loading other chunks
     */
    public static TableStringPool readFromTable(InputStream inputStream) throws IOException {
        TableHeader tableHeader = new TableHeader();
        tableHeader.readBytes(inputStream);
        if(tableHeader.getChunkType()!=ChunkType.TABLE){
            throw new IOException("Not TableBlock: "+tableHeader);
        }
        UnknownChunk poolChunk = new UnknownChunk();
        poolChunk.readBytes(inputStream);
        HeaderBlock poolHeader = poolChunk.getHeaderBlock();
        if(poolHeader.getChunkType()!=ChunkType.STRING){
            throw new IOException("Not StringPool chunk: " + poolChunk);
        }
        BlockReader blockReader = new BlockReader(poolChunk.getBytes());
        TableStringPool stringPool = new TableStringPool(true);
        stringPool.readBytes(blockReader);
        blockReader.close();
        return stringPool;
    }
}
