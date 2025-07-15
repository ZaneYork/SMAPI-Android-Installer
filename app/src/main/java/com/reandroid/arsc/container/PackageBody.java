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
package com.reandroid.arsc.container;

import com.reandroid.arsc.chunk.*;
import com.reandroid.arsc.array.SpecTypePairArray;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.header.SpecHeader;
import com.reandroid.arsc.header.TypeHeader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.list.OverlayableList;
import com.reandroid.arsc.list.StagedAliasList;

import java.io.IOException;

public class PackageBody extends FixedBlockContainer {

    private final SpecTypePairArray mSpecTypePairArray;
    private final LibraryBlock mLibraryBlock;
    private final StagedAliasList mStagedAliasList;
    private final OverlayableList mOverlayableList;
    private final BlockList<OverlayablePolicy> mOverlayablePolicyList;
    private final BlockList<UnknownChunk> mUnknownChunkList;

    public PackageBody(){
        super(6);
        this.mSpecTypePairArray = new SpecTypePairArray();
        this.mLibraryBlock = new LibraryBlock();
        this.mStagedAliasList = new StagedAliasList();
        this.mOverlayableList = new OverlayableList();
        this.mOverlayablePolicyList = new BlockList<>();
        this.mUnknownChunkList = new BlockList<>();

        addChild(0, mSpecTypePairArray);
        addChild(1, mLibraryBlock);
        addChild(2, mStagedAliasList);
        addChild(3, mOverlayableList);
        addChild(4, mOverlayablePolicyList);
        addChild(5, mUnknownChunkList);
    }

    public boolean isEmpty(){
        return  getSpecTypePairArray().isEmpty() &&
                getLibraryBlock().isEmpty() &&
                getStagedAliasList().size() == 0 &&
                getOverlayableList().size() == 0 &&
                getUnknownChunkList().size() == 0;
    }

    public void destroy(){
        getSpecTypePairArray().clear();
        getLibraryBlock().getLibraryInfoArray().clear();
        getStagedAliasList().clearChildes();
        getOverlayableList().clearChildes();
        getUnknownChunkList().clearChildes();
    }
    public OverlayableList getOverlayableList() {
        return mOverlayableList;
    }
    public StagedAliasList getStagedAliasList() {
        return mStagedAliasList;
    }
    public LibraryBlock getLibraryBlock(){
        return mLibraryBlock;
    }
    public SpecTypePairArray getSpecTypePairArray() {
        return mSpecTypePairArray;
    }
    public BlockList<UnknownChunk> getUnknownChunkList(){
        return mUnknownChunkList;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        boolean readOk=true;
        while (readOk){
            readOk=readNextBlock(reader);
        }
    }
    private boolean readNextBlock(BlockReader reader) throws IOException {
        HeaderBlock headerBlock = reader.readHeaderBlock();
        if(headerBlock==null){
            return false;
        }
        int pos=reader.getPosition();
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType==ChunkType.SPEC){
            readSpecBlock(reader);
        }else if(chunkType==ChunkType.TYPE){
            readTypeBlock(reader);
        }else if(chunkType==ChunkType.LIBRARY){
            readLibraryBlock(reader);
        }else if(chunkType==ChunkType.OVERLAYABLE){
            readOverlayable(reader);
        }else if(chunkType==ChunkType.OVERLAYABLE_POLICY){
            readOverlayablePolicy(reader);
        }else if(chunkType==ChunkType.STAGED_ALIAS){
            readStagedAlias(reader);
        }else {
            readUnknownChunk(reader);
        }
        return pos!=reader.getPosition();
    }
    private void readSpecBlock(BlockReader reader) throws IOException{
        SpecHeader specHeader = SpecHeader.read(reader);
        SpecTypePair specTypePair = mSpecTypePairArray.getOrCreate(specHeader.getId().getByte());
        specTypePair.getSpecBlock().readBytes(reader);
    }
    private void readTypeBlock(BlockReader reader) throws IOException{
        TypeHeader typeHeader = TypeHeader.read(reader);
        SpecTypePair specTypePair = mSpecTypePairArray.getOrCreate(typeHeader.getId().getByte());
        TypeBlock typeBlock = specTypePair.getTypeBlockArray().createNext(
                typeHeader.isSparse(), typeHeader.isOffset16());
        typeBlock.readBytes(reader);
    }
    private void readLibraryBlock(BlockReader reader) throws IOException{
        LibraryBlock libraryBlock=new LibraryBlock();
        libraryBlock.readBytes(reader);
        mLibraryBlock.addLibraryInfo(libraryBlock);
    }
    private void readStagedAlias(BlockReader reader) throws IOException{
        StagedAlias stagedAlias = new StagedAlias();
        stagedAlias.readBytes(reader);
        mStagedAliasList.add(stagedAlias);
    }
    private void readOverlayable(BlockReader reader) throws IOException{
        Overlayable overlayable = getOverlayableList().createNext();
        overlayable.readBytes(reader);
    }
    private void readOverlayablePolicy(BlockReader reader) throws IOException{
        OverlayablePolicy overlayablePolicy = new OverlayablePolicy();
        mOverlayablePolicyList.add(overlayablePolicy);
        overlayablePolicy.readBytes(reader);
    }
    private void readUnknownChunk(BlockReader reader) throws IOException{
        UnknownChunk unknownChunk = new UnknownChunk();
        unknownChunk.readBytes(reader);
        mUnknownChunkList.add(unknownChunk);
    }
}
