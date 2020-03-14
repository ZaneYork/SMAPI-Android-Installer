package com.zane.smapiinstaller.logic;

import com.google.common.base.Predicate;

import pxb.android.axml.NodeVisitor;

/**
 * AndroidManifest文件节点访问器
 */
class ManifestTagVisitor extends NodeVisitor {

    private final Predicate<AttrArgs> attrProcessLogic;

    public ManifestTagVisitor(NodeVisitor nv, Predicate<AttrArgs> attrProcessLogic) {
        super(nv);
        this.attrProcessLogic = attrProcessLogic;
    }

    @Override
    public void attr(String ns, String name, int resourceId, int type, Object obj) {
        AttrArgs attrArgs = new AttrArgs(ns, name, resourceId, type, obj);
        attrProcessLogic.apply(attrArgs);
        super.attr(attrArgs.ns, attrArgs.name, attrArgs.resourceId, attrArgs.type, attrArgs.obj);
    }

    @Override
    public NodeVisitor child(String ns, String name) {
        return new ManifestTagVisitor(super.child(ns, name), attrProcessLogic);
    }

    public static class AttrArgs {
        String ns;
        String name;
        int resourceId;
        int type;
        Object obj;

        AttrArgs(String ns, String name, int resourceId, int type, Object obj) {
            this.ns = ns;
            this.name = name;
            this.resourceId = resourceId;
            this.type = type;
            this.obj = obj;
        }
    }
}
