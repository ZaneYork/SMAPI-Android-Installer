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
package com.reandroid.xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class XmlIndentingSerializer extends XmlSerializerWrapper {

    public XmlIndentingSerializer(XmlSerializer baseSerializer) {
        super(baseSerializer);
    }

    @Override
    public void startDocument(String encoding, Boolean standalone) throws IOException, IllegalArgumentException, IllegalStateException {
        super.startDocument(encoding, standalone);
        setIndentFeature();
    }

    @Override
    public XmlSerializer startTag(String namespace, String name) throws IOException, IllegalArgumentException, IllegalStateException {
        setIndentFeature();
        return super.startTag(namespace, name);
    }
    private void setIndentFeature() {
        XMLUtil.setFeatureSafe(this, XMLUtil.FEATURE_INDENT_OUTPUT, true);
    }

    public static XmlSerializer create(XmlSerializer serializer) {
        if(containsIndenting(serializer)) {
            return serializer;
        }
        return new XmlIndentingSerializer(serializer);
    }
    private static boolean containsIndenting(XmlSerializer serializer) {
        while (!(serializer instanceof XmlIndentingSerializer) &&
                (serializer instanceof XmlSerializerWrapper)) {
            serializer = ((XmlSerializerWrapper) serializer).getBaseSerializer();
        }
        return serializer instanceof XmlIndentingSerializer;
    }
}
