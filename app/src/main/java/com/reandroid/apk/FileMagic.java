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

import com.reandroid.archive.InputSource;

import java.io.IOException;
import java.io.InputStream;

public class FileMagic {

    public static String getExtensionFromMagic(InputSource inputSource) throws IOException {
        byte[] magic=readFileMagic(inputSource);
        if(magic==null){
            return null;
        }
        if(isPng(magic)){
            return ".png";
        }
        if(isJpeg(magic)){
            return ".jpg";
        }
        if(isWebp(magic)){
            return ".webp";
        }
        if(isTtf(magic)){
            return ".ttf";
        }
        return null;
    }

    private static boolean isJpeg(byte[] magic){
        return compareMagic(MAGIC_JPG, magic);
    }
    private static boolean isPng(byte[] magic){
        return compareMagic(MAGIC_PNG, magic);
    }
    private static boolean isWebp(byte[] magic){
        return compareMagic(MAGIC_WEBP, magic);
    }
    private static boolean isTtf(byte[] magic){
        return compareMagic(MAGIC_TTF, magic);
    }
    private static boolean compareMagic(byte[] magic, byte[] readMagic){
        if(magic==null || readMagic==null){
            return false;
        }
        int max=magic.length;
        if(max>readMagic.length){
            max=readMagic.length;
        }
        if(max==0){
            return false;
        }
        for(int i=0;i<max;i++){
            int m=magic[i];
            if(m==-1){
                continue;
            }
            if(m != readMagic[i]){
                return false;
            }
        }
        return true;
    }
    private static byte[] readFileMagic(InputSource inputSource) throws IOException {
        InputStream inputStream=inputSource.openStream();
        byte[] magic=new byte[MAGIC_MAX_LENGTH];
        int count=inputStream.read(magic, 0, magic.length);
        inputStream.close();
        if(count<magic.length){
            return null;
        }
        return magic;
    }

    private static final int MAGIC_MAX_LENGTH=16;
    private static final byte[] MAGIC_PNG=new byte[]{(byte) 137, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
    private static final byte[] MAGIC_JPG=new byte[]{-0x01, (byte) 0xd8, -0x01, (byte) 224, 0x00, 0x10, 0x4a, 0x46};
    private static final byte[] MAGIC_WEBP=new byte[]{0x52, 0x49, 0x46, 0x46, -0x01, -0x01, -0x01, 0x00, 0x57, 0x45, 0x42, 0x50, 0x56, 0x50, 0x38};
    private static final byte[] MAGIC_TTF=new byte[]{0x00, 0x01, 0x00, 0x00, 0x00, -0x01, -0x01, -0x01};

}
