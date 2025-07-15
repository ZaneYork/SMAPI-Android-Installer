/*
 * Copyright (c) 2002 JSON.org (now "Public Domain")
 * This is NOT property of REAndroid
 * This package is renamed from org.json.* to avoid class conflict when used on android platforms
*/
package com.reandroid.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
@SuppressWarnings({""})
public class XMLParserConfiguration {
    /** Original Configuration of the XML Parser. */
    public static final XMLParserConfiguration ORIGINAL
        = new XMLParserConfiguration();
    /** Original configuration of the XML Parser except that values are kept as strings. */
    public static final XMLParserConfiguration KEEP_STRINGS
        = new XMLParserConfiguration().withKeepStrings(true);

    private boolean keepStrings;
    

    private String cDataTagName;
    

    private boolean convertNilAttributeToNull;

    private Map<String, XMLXsiTypeConverter<?>> xsiTypeMap;

    public XMLParserConfiguration() {
        this.keepStrings = false;
        this.cDataTagName = "content";
        this.convertNilAttributeToNull = false;
        this.xsiTypeMap = Collections.emptyMap();
    }

    @Deprecated
    public XMLParserConfiguration(final boolean keepStrings) {
        this(keepStrings, "content", false);
    }

    @Deprecated
    public XMLParserConfiguration(final String cDataTagName) {
        this(false, cDataTagName, false);
    }

    @Deprecated
    public XMLParserConfiguration(final boolean keepStrings, final String cDataTagName) {
        this.keepStrings = keepStrings;
        this.cDataTagName = cDataTagName;
        this.convertNilAttributeToNull = false;
    }

    @Deprecated
    public XMLParserConfiguration(final boolean keepStrings, final String cDataTagName, final boolean convertNilAttributeToNull) {
        this.keepStrings = keepStrings;
        this.cDataTagName = cDataTagName;
        this.convertNilAttributeToNull = convertNilAttributeToNull;
    }

    private XMLParserConfiguration(final boolean keepStrings, final String cDataTagName,
                                   final boolean convertNilAttributeToNull, final Map<String, XMLXsiTypeConverter<?>> xsiTypeMap ) {
        this.keepStrings = keepStrings;
        this.cDataTagName = cDataTagName;
        this.convertNilAttributeToNull = convertNilAttributeToNull;
        this.xsiTypeMap = Collections.unmodifiableMap(xsiTypeMap);
    }

    @Override
    protected XMLParserConfiguration clone() {
        // future modifications to this method should always ensure a "deep"
        // clone in the case of collections. i.e. if a Map is added as a configuration
        // item, a new map instance should be created and if possible each value in the
        // map should be cloned as well. If the values of the map are known to also
        // be immutable, then a shallow clone of the map is acceptable.
        return new XMLParserConfiguration(
                this.keepStrings,
                this.cDataTagName,
                this.convertNilAttributeToNull,
                this.xsiTypeMap
        );
    }
    

    public boolean isKeepStrings() {
        return this.keepStrings;
    }

    public XMLParserConfiguration withKeepStrings(final boolean newVal) {
        XMLParserConfiguration newConfig = this.clone();
        newConfig.keepStrings = newVal;
        return newConfig;
    }

    public String getcDataTagName() {
        return this.cDataTagName;
    }

    public XMLParserConfiguration withcDataTagName(final String newVal) {
        XMLParserConfiguration newConfig = this.clone();
        newConfig.cDataTagName = newVal;
        return newConfig;
    }

    public boolean isConvertNilAttributeToNull() {
        return this.convertNilAttributeToNull;
    }

    public XMLParserConfiguration withConvertNilAttributeToNull(final boolean newVal) {
        XMLParserConfiguration newConfig = this.clone();
        newConfig.convertNilAttributeToNull = newVal;
        return newConfig;
    }

    public Map<String, XMLXsiTypeConverter<?>> getXsiTypeMap() {
        return this.xsiTypeMap;
    }

    public XMLParserConfiguration withXsiTypeMap(final Map<String, XMLXsiTypeConverter<?>> xsiTypeMap) {
        XMLParserConfiguration newConfig = this.clone();
        Map<String, XMLXsiTypeConverter<?>> cloneXsiTypeMap = new HashMap<String, XMLXsiTypeConverter<?>>(xsiTypeMap);
        newConfig.xsiTypeMap = Collections.unmodifiableMap(cloneXsiTypeMap);
        return newConfig;
    }
}
