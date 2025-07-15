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
package com.reandroid.apk;

import com.reandroid.utils.CRCDigest;

import java.io.IOException;
import java.io.OutputStream;

@Deprecated
public class CrcOutputStream extends OutputStream {
    private final CRCDigest crc;
    private long length;
    private long mCheckSum;
    public CrcOutputStream() {
        super();
        this.crc = new CRCDigest();
    }
    public long getLength(){
        return length;
    }
    public long getCrcValue(){
        if(mCheckSum==0){
            mCheckSum=crc.getValue();
        }
        return mCheckSum;
    }
    @Override
    public void write(int b) throws IOException {
        this.crc.update(b);
        length=length+1;
    }
    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.crc.update(b, off, len);
        length=length+len;
    }
}
