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

import com.reandroid.archive.*;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.model.FrameworkTable;
import com.reandroid.arsc.value.ValueType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/*
 * Produces compressed framework apk by removing irrelevant files and entries,
 * basically it keeps only resources.arsc and AndroidManifest.xml
 */
public class FrameworkApk extends ApkModule{
    private final Object mLock = new Object();
    private int versionCode;
    private String versionName;
    private String packageName;
    private boolean mOptimizing;
    private boolean mDestroyed;
    public FrameworkApk(String moduleName, ZipEntryMap zipEntryMap) {
        super(moduleName, zipEntryMap);
        super.setLoadDefaultFramework(false);
    }
    public FrameworkApk(ZipEntryMap zipEntryMap) {
        this("framework", zipEntryMap);
    }

    @Override
    public void destroy(){
        synchronized (mLock){
            this.versionCode = -1;
            this.versionName = "-1";
            this.packageName = "destroyed";
            super.destroy();
            this.mDestroyed = true;
        }
    }
    public boolean isDestroyed() {
        synchronized (mLock){
            if(!mDestroyed){
                return false;
            }
            if(hasTableBlock()){
                this.versionCode = 0;
                this.versionName = null;
                this.packageName = null;
                mDestroyed = false;
                return false;
            }
            return true;
        }
    }

    public int getVersionCode() {
        if(this.versionCode == 0){
            initValues();
        }
        return this.versionCode;
    }
    public String getVersionName() {
        if(this.versionName == null){
            initValues();
        }
        return this.versionName;
    }
    @Override
    public String getPackageName() {
        if(this.packageName == null){
            initValues();
        }
        return this.packageName;
    }
    @Override
    public void setPackageName(String packageName) {
        super.setPackageName(packageName);
        this.packageName = null;
    }
    private void initValues() {
        if(hasAndroidManifest()){
            AndroidManifestBlock manifest = getAndroidManifest();
            Integer code = manifest.getVersionCode();
            if(code != null){
                this.versionCode = code;
            }
            if(this.versionName == null){
                this.versionName = manifest.getVersionName();
            }
            if(this.packageName == null){
                this.packageName = manifest.getPackageName();
            }
        }
        if(hasTableBlock()){
            FrameworkTable table = getTableBlock();
            if(table.isOptimized() && this.versionCode == 0){
                int version = table.getVersionCode();
                if(version!=0){
                    versionCode = version;
                    if(this.versionName == null){
                        this.versionName = String.valueOf(version);
                    }
                }
            }
            if(this.packageName == null){
                PackageBlock packageBlock = table.pickOne();
                if(packageBlock!=null){
                    this.packageName = packageBlock.getName();
                }
            }
        }
    }
    @Override
    public void setManifest(AndroidManifestBlock manifestBlock){
        synchronized (mLock){
            super.setManifest(manifestBlock);
            this.versionCode = 0;
            this.versionName = null;
            this.packageName = null;
        }
    }
    @Override
    public void setTableBlock(TableBlock tableBlock){
        synchronized (mLock){
            super.setTableBlock(tableBlock);
            this.versionCode = 0;
            this.versionName = null;
            this.packageName = null;
        }
    }
    @Override
    public FrameworkTable getTableBlock() {
        return (FrameworkTable) super.getTableBlock();
    }
    @Override
    public FrameworkTable getLoadedTableBlock() {
        return (FrameworkTable) super.getLoadedTableBlock();
    }
    @Override
    FrameworkTable loadTableBlock() throws IOException {
        ZipEntryMap archive= getZipEntryMap();
        InputSource inputSource = archive.getInputSource(TableBlock.FILE_NAME);
        if(inputSource==null){
            throw new IOException("Entry not found: "+TableBlock.FILE_NAME);
        }
        InputStream inputStream = inputSource.openStream();
        FrameworkTable frameworkTable=FrameworkTable.load(inputStream);
        frameworkTable.setApkFile(this);

        BlockInputSource<FrameworkTable> blockInputSource=new BlockInputSource<>(inputSource.getName(), frameworkTable);
        blockInputSource.setMethod(inputSource.getMethod());
        blockInputSource.setSort(inputSource.getSort());
        archive.add(blockInputSource);
        return frameworkTable;
    }
    public void optimize(){
        synchronized (mLock){
            if(mOptimizing){
                return;
            }
            if(!hasTableBlock()){
                mOptimizing = false;
                return;
            }
            FrameworkTable frameworkTable = getTableBlock();
            if(frameworkTable.isOptimized()){
                mOptimizing = false;
                initValues();
                return;
            }
            FrameworkOptimizer optimizer = new FrameworkOptimizer(this);
            optimizer.optimize();
            mOptimizing = false;
            initValues();
        }
    }
    public String getName(){
        if(isDestroyed()){
            return "destroyed";
        }
        String pkg = getPackageName();
        if(pkg==null){
            return "";
        }
        return pkg + "-" + getVersionCode();
    }
    @Override
    public int hashCode(){
        return Objects.hash(getClass(), getName());
    }
    @Override
    public boolean equals(Object obj){
        if(obj==this){
            return true;
        }
        if(getClass()!=obj.getClass()){
            return false;
        }
        FrameworkApk other = (FrameworkApk) obj;
        return getName().equals(other.getName());
    }
    @Override
    public String toString(){
        return getName();
    }
    public static FrameworkApk loadApkFile(File apkFile) throws IOException {
        return loadApkFile(apkFile, true);
    }
    public static FrameworkApk loadTableBlock(File apkFile) throws IOException {
        return loadApkFile(apkFile, false);
    }
    private static FrameworkApk loadApkFile(File apkFile, boolean addManifest) throws IOException {
        ArchiveFile archive = new ArchiveFile(apkFile);
        InputSource table = archive.getEntrySource(TableBlock.FILE_NAME);
        if(table == null){
            throw new IOException("Missing " + TableBlock.FILE_NAME + ", on " + apkFile);
        }
        ZipEntryMap zipEntryMap = new ZipEntryMap();
        zipEntryMap.add(table);
        if(addManifest){
            zipEntryMap.add(archive.getEntrySource(AndroidManifestBlock.FILE_NAME));
        }
        FrameworkApk frameworkApk = new FrameworkApk(zipEntryMap);
        frameworkApk.setCloseable(archive);
        return frameworkApk;
    }
    public static FrameworkApk loadApkFile(File apkFile, String moduleName) throws IOException {
        ArchiveFile archive = new ArchiveFile(apkFile);
        ZipEntryMap zipEntryMap = archive.createZipEntryMap();
        FrameworkApk frameworkApk = new FrameworkApk(moduleName, zipEntryMap);
        frameworkApk.setCloseable(archive);
        return frameworkApk;
    }
    public static boolean isFramework(ApkModule apkModule) {
        if(!apkModule.hasAndroidManifest()){
            return false;
        }
        return isFramework(apkModule.getAndroidManifest());
    }
    public static boolean isFramework(AndroidManifestBlock manifestBlock){
        ResXmlElement root = manifestBlock.getManifestElement();
        ResXmlAttribute attribute = root.searchAttributeByName(AndroidManifestBlock.NAME_coreApp);
        if(attribute==null || attribute.getValueType()!= ValueType.BOOLEAN){
            return false;
        }
        return attribute.getValueAsBoolean();
    }
    public static FrameworkApk loadApkBuffer(InputStream inputStream) throws IOException{
        return loadApkBuffer("framework", inputStream);
    }
    public static FrameworkApk loadApkBuffer(String moduleName, InputStream inputStream) throws IOException {
        ArchiveBytes archive = new ArchiveBytes(inputStream);
        ZipEntryMap zipEntryMap = archive.createZipEntryMap();
        FrameworkApk frameworkApk = new FrameworkApk(moduleName, zipEntryMap);
        frameworkApk.initValues();
        return frameworkApk;
    }
    public static void optimize(File in, File out, APKLogger apkLogger) throws IOException{
        FrameworkApk frameworkApk = FrameworkApk.loadApkFile(in);
        frameworkApk.setAPKLogger(apkLogger);
        frameworkApk.optimize();
        frameworkApk.writeApk(out);
    }
}
