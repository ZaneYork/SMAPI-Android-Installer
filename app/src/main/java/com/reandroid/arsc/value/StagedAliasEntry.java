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
package com.reandroid.arsc.value;

import com.reandroid.arsc.item.ByteArray;
import com.reandroid.utils.HexUtil;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

public class StagedAliasEntry extends ByteArray implements JSONConvert<JSONObject> {
    public StagedAliasEntry(){
        super(8);
    }
    public boolean isEqual(StagedAliasEntry other){
        if(other==null){
            return false;
        }
        if(other==this){
            return true;
        }
        return getStagedResId()==other.getStagedResId()
                && getFinalizedResId()==other.getFinalizedResId();
    }
    public int getStagedResId(){
        return getInteger(0);
    }
    public void setStagedResId(int id){
        putInteger(0, id);
    }
    public int getFinalizedResId(){
        return getInteger(4);
    }
    public void setFinalizedResId(int id){
        putInteger(4, id);
    }
    @Override
    public String toString(){
        return "stagedResId=" + HexUtil.toHex8(getStagedResId())
                +", finalizedResId=" + HexUtil.toHex8(getFinalizedResId());
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(NAME_staged_resource_id, getStagedResId());
        jsonObject.put(NAME_finalized_resource_id, getFinalizedResId());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setStagedResId(json.getInt(NAME_staged_resource_id));
        setFinalizedResId(json.getInt(NAME_finalized_resource_id));
    }
    public static final String NAME_staged_resource_id = "staged_resource_id";
    public static final String NAME_finalized_resource_id = "finalized_resource_id";
}
