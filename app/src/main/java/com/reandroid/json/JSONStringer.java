/*
 * Copyright (c) 2002 JSON.org (now "Public Domain")
 * This is NOT property of REAndroid
 * This package is renamed from org.json.* to avoid class conflict when used on android platforms
*/
package com.reandroid.json;

import java.io.StringWriter;

public class JSONStringer extends JSONWriter {

    public JSONStringer() {
        super(new StringWriter());
    }

    @Override
    public String toString() {
        return this.mode == 'd' ? this.writer.toString() : null;
    }
}
