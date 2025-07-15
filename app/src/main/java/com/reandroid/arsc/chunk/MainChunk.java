package com.reandroid.arsc.chunk;

import com.reandroid.arsc.ApkFile;
import com.reandroid.arsc.pool.StringPool;

public interface MainChunk {
    StringPool<?> getStringPool();
    ApkFile getApkFile();
    void setApkFile(ApkFile apkFile);
    TableBlock getTableBlock();
}
