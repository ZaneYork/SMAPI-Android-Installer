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

public class EntryHeader extends ValueHeader {
    public EntryHeader(){
        super(HEADER_SIZE_SCALAR);
    }

    @Override
    public String toString(){
        if(isNull()){
            return "null";
        }
        StringBuilder builder=new StringBuilder();
        int byte_size = getSize();
        int read_size = readSize();
        if(byte_size!=8){
            builder.append("size=").append(byte_size);
        }
        if(byte_size!=read_size){
            builder.append(" readSize=").append(read_size);
        }
        if(isComplex()){
            builder.append(" complex");
        }
        if(isPublic()){
            builder.append(" public");
        }
        if(isWeak()){
            builder.append(" weak");
        }
        if(isCompact()){
            builder.append(" compact");
        }
        String name = getName();
        if(name!=null){
            builder.append(" name=").append(name);
        }else {
            builder.append(" key=").append(getKey());
        }
        return builder.toString();
    }

    private static final short HEADER_SIZE_SCALAR = 8;
}
