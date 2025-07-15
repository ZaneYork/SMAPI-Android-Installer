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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.model.ResourceLibrary;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.FixedLengthString;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.utils.HexUtil;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class LibraryInfo extends Block implements JSONConvert<JSONObject>, ResourceLibrary {
    private final IntegerItem mPackageId;
    private final FixedLengthString mPackageName;

    public LibraryInfo(){
        super();
        this.mPackageId=new IntegerItem();
        this.mPackageName = new FixedLengthString(256);
        mPackageId.setIndex(0);
        mPackageId.setParent(this);
        mPackageName.setIndex(1);
        mPackageName.setParent(this);
    }

    @Override
    public int getId(){
        return mPackageId.get();
    }
    public void setId(int id){
        mPackageId.set(id);
    }
    @Override
    public String getName(){
        return mPackageName.get();
    }
    public void setName(String packageName){
        mPackageName.set(packageName);
    }
    @Override
    public String getPrefix(){
        return ResourceLibrary.toPrefix(getName());
    }
    @Override
    public String getUri(){
        if(getId() == 0x01 && ResourceLibrary.PREFIX_ANDROID.equals(getName())){
            return ResourceLibrary.URI_ANDROID;
        }
        return ResourceLibrary.URI_RES_AUTO;
    }
    @Override
    public boolean packageNameMatches(String packageName) {
        return ResourceLibrary.packageNameMatches(this, packageName);
    }

    @Override
    public byte[] getBytes() {
        if(isNull()){
            return null;
        }
        return addBytes(mPackageId.getBytes(), mPackageName.getBytes());
    }
    @Override
    public int countBytes() {
        if(isNull()){
            return 0;
        }
        return mPackageId.countBytes()+mPackageName.countBytes();
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        counter.setCurrent(this);
        if(counter.END==this){
            counter.FOUND=true;
            return;
        }
        mPackageId.onCountUpTo(counter);
        mPackageName.onCountUpTo(counter);
    }
    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        int result=mPackageId.writeBytes(stream);
        result+=mPackageName.writeBytes(stream);
        return result;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        mPackageId.readBytes(reader);
        mPackageName.readBytes(reader);
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("id", getId());
        jsonObject.put("name", getName());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setId(json.getInt("id"));
        setName(json.getString("name"));
    }
    public void merge(LibraryInfo info){
        if(info==null||info==this){
            return;
        }
        if(getId()!=info.getId()){
            throw new IllegalArgumentException("Can not add different id libraries: "
                    + getId()+"!="+info.getId());
        }
        setName(info.getName());
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append("LIBRARY{");
        builder.append(HexUtil.toHex2((byte) getId()));
        builder.append(':');
        String name= getName();
        if(name==null){
            name="NULL";
        }
        builder.append(name);
        builder.append('}');
        return builder.toString();
    }
}
