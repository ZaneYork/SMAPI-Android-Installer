/*
 * Copyright (c) 2002 JSON.org (now "Public Domain")
 * This is NOT property of REAndroid
 * This package is renamed from org.json.* to avoid class conflict when used on android platforms
*/
package com.reandroid.json;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class JSONWriter {
    protected final Appendable writer;
    protected char mode;
    private boolean comma;
    private final JSONObject[] stack;
    private int top;
    private int indentFactor;
    public JSONWriter(Appendable w) {
        this.comma = false;
        this.mode = 'i';
        this.stack = new JSONObject[MAX_DEPTH];
        this.top = 0;
        this.writer = w;
        this.indentFactor = DEFAULT_INDENT_FACTOR;
    }

    /**
     * indentFactor == 0, only new line
     * indentFactor < 0, off
     * indentFactor > 0, length of tab (left spaces) is indentFactor + depth
     * default = INDENT_FACTOR
     * */
    public void setIndentFactor(int indentFactor) {
        this.indentFactor = indentFactor;
    }

    private JSONWriter append(String string) throws JSONException {
        if (string == null) {
            throw new JSONException("Null pointer");
        }
        if (this.mode == 'o' || this.mode == 'a') {
            try {
                if (this.comma && this.mode == 'a') {
                    this.writer.append(',');
                    writeIndent();
                }
                this.writer.append(string);
            } catch (IOException e) {
                throw new JSONException(e);
            }
            if (this.mode == 'o') {
                this.mode = 'k';
            }
            this.comma = true;
            return this;
        }
        throw new JSONException("Value out of sequence.");
    }

    public JSONWriter array() throws JSONException {
        writeIndent();
        if (this.mode == 'i' || this.mode == 'o' || this.mode == 'a') {
            this.push(null);
            this.append("[");
            this.comma = false;
            writeIndent();
            return this;
        }
        throw new JSONException("Misplaced array.");
    }

    private JSONWriter end(char m, char c) throws JSONException {
        if (this.mode != m) {
            throw new JSONException(m == 'a'
                ? "Misplaced endArray."
                : "Misplaced endObject.");
        }
        this.pop(m);
        try {
            writeIndent();
            this.writer.append(c);
        } catch (IOException e) {
            throw new JSONException(e);
        }
        this.comma = true;
        return this;
    }

    public JSONWriter endArray() throws JSONException {
        return this.end('a', ']');
    }

    public JSONWriter endObject() throws JSONException {
        return this.end('k', '}');
    }

    public JSONWriter key(String string) throws JSONException {
        if (string == null) {
            throw new JSONException("Null key.");
        }
        if (this.mode == 'k') {
            try {
                JSONObject topObject = this.stack[this.top - 1];
                // don't use the built in putOnce method to maintain Android support
				if(topObject.has(string)) {
					throw new JSONException("Duplicate key \"" + string + "\"");
				}
                topObject.put(string, true);
                if (this.comma) {
                    this.writer.append(',');
                    writeIndent();
                }
                this.writer.append(JSONObject.quote(string));
                this.writer.append(':');
                this.comma = false;
                this.mode = 'o';
                return this;
            } catch (IOException e) {
                throw new JSONException(e);
            }
        }
        throw new JSONException("Misplaced key.");
    }
    public JSONWriter object() throws JSONException {
        if (this.mode == 'i') {
            this.mode = 'o';
        }
        if (this.mode == 'o' || this.mode == 'a') {
            this.append("{");
            this.push(new JSONObject());
            this.comma = false;
            writeIndent();
            return this;
        }
        throw new JSONException("Misplaced object.");

    }
    private void pop(char c) throws JSONException {
        if (this.top <= 0) {
            throw new JSONException("Nesting error.");
        }
        char m = this.stack[this.top - 1] == null ? 'a' : 'k';
        if (m != c) {
            throw new JSONException("Nesting error.");
        }
        this.top -= 1;
        this.mode = this.top == 0
            ? 'd'
            : this.stack[this.top - 1] == null
            ? 'a'
            : 'k';
    }

    private void push(JSONObject jo) throws JSONException {
        if (this.top >= MAX_DEPTH) {
            throw new JSONException("Nesting too deep.");
        }
        this.stack[this.top] = jo;
        this.mode = jo == null ? 'a' : 'k';
        this.top += 1;
    }
    public JSONWriter value(boolean b) throws JSONException {
        return this.append(b ? "true" : "false");
    }

    public JSONWriter value(double d) throws JSONException {
        return this.value(Double.valueOf(d));
    }

    public JSONWriter value(long l) throws JSONException {
        return this.append(Long.toString(l));
    }
    public JSONWriter value(Object object) throws JSONException {
        if(object instanceof JSONArray){
            return value((JSONArray) object);
        }
        if(object instanceof JSONObject){
            return value((JSONObject) object);
        }
        return this.append(valueToString(object));
    }
    public JSONWriter value(JSONArray jsonArray) throws JSONException {
        JSONWriter writer = array();
        int length = jsonArray.length();
        for(int i = 0; i < length; i++){
            writer.value(jsonArray.get(i));
        }
        writer.endArray();
        return this;
    }
    public JSONWriter value(JSONObject jsonObject) throws JSONException {
        JSONWriter writer = object();
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()){
            String key = iterator.next();
            writer.key(key).value(jsonObject.get(key));
        }
        writer.endObject();
        return this;
    }
    private void writeIndent() throws JSONException {
        if(this.indentFactor < 0 || this.mode == 'i'){
            return;
        }
        try{
            Appendable appendable = this.writer;
            appendable.append('\n');
            int level = this.top * this.indentFactor;
            for(int i = 0; i < level ; i++){
                appendable.append(' ');
            }
        }catch (IOException ex){
            throw new JSONException(ex);
        }
    }

    public static String valueToString(Object value) throws JSONException {
        if (value == null || value.equals(null)) {
            return "null";
        }
        if (value instanceof JSONString) {
            String object;
            try {
                object = ((JSONString) value).toJSONString();
            } catch (Exception e) {
                throw new JSONException(e);
            }
            if (object != null) {
                return object;
            }
            throw new JSONException("Bad value from toJSONString: " + object);
        }
        if (value instanceof Number) {
            return JSONItem.numberToString((Number) value);
        }
        if (value instanceof Boolean || value instanceof JSONObject
                || value instanceof JSONArray) {
            return value.toString();
        }
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            return new JSONObject(map).toString();
        }
        if (value instanceof Collection) {
            Collection<?> coll = (Collection<?>) value;
            return new JSONArray(coll).toString();
        }
        if (value.getClass().isArray()) {
            return new JSONArray(value).toString();
        }
        if(value instanceof Enum<?>){
            return JSONObject.quote(((Enum<?>)value).name());
        }
        return JSONObject.quote(value.toString());
    }

    private static final int MAX_DEPTH = 200;
    private static final int DEFAULT_INDENT_FACTOR = 1;
}
