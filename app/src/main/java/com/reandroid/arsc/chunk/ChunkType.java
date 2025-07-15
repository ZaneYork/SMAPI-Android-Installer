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
package com.reandroid.arsc.chunk;

import com.reandroid.utils.HexUtil;

public enum ChunkType {
    NULL((short)0x0000),

    STRING((short)0x0001),
    TABLE((short)0x0002),
    XML((short)0x0003),

    XML_START_NAMESPACE((short)0x0100),
    XML_END_NAMESPACE((short)0x0101),
    XML_START_ELEMENT((short)0x0102),
    XML_END_ELEMENT((short)0x0103),
    XML_CDATA((short)0x0104),
    XML_LAST_CHUNK((short)0x017f),
    XML_RESOURCE_MAP((short)0x0180),

    PACKAGE((short)0x0200),
    TYPE((short)0x0201),
    SPEC((short)0x0202),
    LIBRARY((short)0x0203),
    OVERLAYABLE((short)0x0204),
    OVERLAYABLE_POLICY((short)0x0205),
    STAGED_ALIAS((short)0x0206);

    public final short ID;
    ChunkType(short id) {
        this.ID = id;
    }

    @Override
    public String toString(){
        return name() + "(" + HexUtil.toHex4(ID) + ")";
    }

    public static ChunkType get(short id){
        ChunkType[] all=values();
        for(ChunkType t:all){
            if(t.ID ==id){
                return t;
            }
        }
        return null;
    }

    public static ChunkType getTable(short id){
        for(ChunkType t:table_chunk_types){
            if(t.ID ==id){
                return t;
            }
        }
        return null;
    }
    public static ChunkType getXml(short id){
        for(ChunkType t:xml_chunk_types){
            if(t.ID ==id){
                return t;
            }
        }
        return null;
    }

    private static final ChunkType[] table_chunk_types=new ChunkType[]{
            PACKAGE,
            TYPE,
            SPEC,
            LIBRARY
    };
    private static final ChunkType[] xml_chunk_types=new ChunkType[]{
            XML_START_NAMESPACE,
            XML_END_NAMESPACE,
            XML_START_ELEMENT,
            XML_END_ELEMENT,
            XML_CDATA,
            XML_LAST_CHUNK,
            XML_RESOURCE_MAP
    };
}
