package com.zane.smapiinstaller.utils;

import com.zane.smapiinstaller.logic.ManifestTagVisitor;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.AxmlWriter;
import pxb.android.axml.NodeVisitor;

public class ManifestUtil {

    /**
     * 修改AndroidManifest.xml文件
     *
     * @param bytes            AndroidManifest.xml文件字符数组
     * @param attrProcessLogic 处理逻辑
     * @return 修改后的AndroidManifest.xml文件字符数组
     * @throws IOException 异常
     */
    public static byte[] modifyManifest(byte[] bytes, Function<ManifestTagVisitor.AttrArgs, List<ManifestTagVisitor.AttrArgs>> attrProcessLogic, Function<ManifestTagVisitor.ChildArgs, List<ManifestTagVisitor.ChildArgs>> childProcessLogic) throws IOException {
        AxmlReader reader = new AxmlReader(bytes);
        AxmlWriter writer = new AxmlWriter();
        reader.accept(new AxmlVisitor(writer) {
            @Override
            public NodeVisitor child(String ns, String name) {
                NodeVisitor child = super.child(ns, name);
                return new ManifestTagVisitor(child, attrProcessLogic, childProcessLogic);
            }
        });
        return writer.toByteArray();
    }
}
