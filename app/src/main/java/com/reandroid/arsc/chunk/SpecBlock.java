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

import com.reandroid.arsc.array.TypeBlockArray;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.header.SpecHeader;
import com.reandroid.arsc.item.*;
import com.reandroid.utils.HexUtil;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.util.List;

public class SpecBlock extends Chunk<SpecHeader> implements JSONConvert<JSONObject> {
    private final SpecFlagsArray specFlagsArray;
    public SpecBlock() {
        super(new SpecHeader(), 1);
        SpecHeader header = getHeaderBlock();
        this.specFlagsArray = new SpecFlagsArray(header.getEntryCount());
        addChild(specFlagsArray);
    }
    public void destroy(){
        setParent(null);
        getSpecFlagsArray().clear();
    }
    public SpecFlag getSpecFlag(int id){
        return getSpecFlagsArray().getFlag(id);
    }
    public SpecFlagsArray getSpecFlagsArray(){
        return specFlagsArray;
    }
    public List<Integer> listSpecFlags(){
        return specFlagsArray.toList();
    }
    public byte getTypeId(){
        return getHeaderBlock().getId().getByte();
    }
    public int getId(){
        return getHeaderBlock().getId().get();
    }
    public void setId(int id){
        setTypeId((byte) (0xff & id));
    }
    public void setTypeId(byte id){
        getHeaderBlock().getId().set(id);
        getTypeBlockArray().setTypeId(id);
    }
    public TypeBlockArray getTypeBlockArray(){
        SpecTypePair specTypePair=getSpecTypePair();
        if(specTypePair!=null){
            return specTypePair.getTypeBlockArray();
        }
        return null;
    }
    SpecTypePair getSpecTypePair(){
        return getParent(SpecTypePair.class);
    }
    public int getEntryCount() {
        return specFlagsArray.size();
    }
    public void setEntryCount(int count){
        specFlagsArray.setSize(count);
        specFlagsArray.refresh();
    }
    @Override
    protected void onChunkRefreshed() {
        specFlagsArray.refresh();
    }

    public void merge(SpecBlock specBlock){
        if(specBlock == null || specBlock==this){
            return;
        }
        this.getSpecFlagsArray().merge(specBlock.getSpecFlagsArray());
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(super.toString());
        TypeBlockArray typeBlockArray=getTypeBlockArray();
        if(typeBlockArray!=null){
            builder.append(", typesCount=");
            builder.append(typeBlockArray.size());
        }
        return builder.toString();
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(TypeBlock.NAME_id, getId());
        jsonObject.put(NAME_spec_flags, getSpecFlagsArray().toJson());
        return jsonObject;
    }

    @Override
    public void fromJson(JSONObject json) {
        setId(json.getInt(TypeBlock.NAME_id));
        getSpecFlagsArray().fromJson(json.optJSONArray(NAME_spec_flags));
    }

    public enum Flag{
        SPEC_PUBLIC((byte) 0x40),
        SPEC_STAGED_API((byte) 0x20);

        private final byte flag;
        Flag(byte flag) {
            this.flag = flag;
        }
        public byte getFlag() {
            return flag;
        }
        public static boolean isPublic(byte flag){
            return (SPEC_PUBLIC.flag & flag) == SPEC_PUBLIC.flag;
        }
        public static boolean isStagedApi(byte flag){
            return (SPEC_STAGED_API.flag & flag) == SPEC_STAGED_API.flag;
        }
        public static String toString(byte flagValue){
            StringBuilder builder = new StringBuilder();
            boolean appendOnce = false;
            int sum = 0;
            int flagValueInt = flagValue & 0xff;
            for(Flag flag:values()){
                int flagInt = flag.flag & 0xff;
                if((flagInt & flagValueInt) != flagInt){
                    continue;
                }
                if(appendOnce){
                    builder.append('|');
                }
                builder.append(flag);
                appendOnce = true;
                sum = sum | flagInt;
            }
            if(sum != flagValueInt){
                if(appendOnce){
                    builder.append('|');
                }
                builder.append(HexUtil.toHex2((byte) flagValueInt));
            }
            return builder.toString();
        }
    }

    public static final String NAME_spec = "spec";
    public static final String NAME_spec_flags = "spec_flags";
    public static final String NAME_flag = "flag";
}
