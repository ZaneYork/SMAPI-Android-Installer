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
package com.reandroid.archive;

import com.reandroid.archive.writer.ApkFileWriter;
import com.reandroid.archive.writer.ZipAligner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ZipAlign {

    public static void alignApk(File apkFile) throws IOException {
        if(!apkFile.isFile()){
            throw new FileNotFoundException("No such file: " + apkFile);
        }
        File tmp = toTmpFile(apkFile);
        try{
            alignApk(apkFile, tmp);
        }catch (IOException ex){
            tmp.delete();
            throw ex;
        }
        apkFile.delete();
        tmp.renameTo(apkFile);
    }

    public static void alignApk(File apkFile, File outFile) throws IOException {
        align(apkFile, outFile, ZipAligner.apkAligner());
    }
    public static void align(File zipFile, File outFile, int alignment) throws IOException {
        ZipAligner zipAligner = new ZipAligner();
        zipAligner.setDefaultAlignment(alignment);
        align(zipFile, outFile, zipAligner);
    }
    public static void align(File zipFile, File outFile, ZipAligner zipAligner) throws IOException {
        if(zipFile.equals(outFile)){
            throw new IOException("Input and output are equal: " + zipFile);
        }
        ArchiveFile archiveFile = new ArchiveFile(zipFile);
        ApkFileWriter writer = new ApkFileWriter(outFile, archiveFile.getInputSources());
        writer.setZipAligner(zipAligner);
        writer.write();
    }

    private static File toTmpFile(File file){
        String name = file.getName() + ".align.tmp";
        File dir = file.getParentFile();
        if(dir == null){
            return new File(name);
        }
        return new File(dir, name);
    }
}
