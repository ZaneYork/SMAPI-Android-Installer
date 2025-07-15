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
package com.reandroid.arsc.coder.xml;

import android.text.TextUtils;

import com.reandroid.arsc.item.StringItem;
import com.reandroid.xml.StyleDocument;
import com.reandroid.xml.StyleText;
import com.reandroid.xml.XMLUtil;
import com.reandroid.xml.base.Text;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;

public class AaptXmlStringDecoder implements XmlStringDecoder {

    public AaptXmlStringDecoder() {
    }

    @Override
    public void serializeText(StringItem stringItem, XmlSerializer serializer) throws IOException {
        StyleDocument styleDocument = stringItem.getStyleDocument();
        if (styleDocument != null) {
            escapeStyleDocument(styleDocument);
            styleDocument.serialize(serializer);
        } else {
            serializer.text(decodePlainToAaptString(stringItem.getXml()));
        }
    }

    @Override
    public String decodeAttributeValue(StringItem stringItem) {
        return XMLUtil.escapeXmlChars(stringItem.getXml());
    }
    public static void escapeStyleDocument(StyleDocument styleDocument) {
        Iterator<StyleText> iterator = styleDocument.getStyleTexts();
        while (iterator.hasNext()) {
            StyleText styleText = iterator.next();
            styleText.setText(escapeXmlValue(styleText.getText(false)));
        }
    }
    private String decodePlainToAaptString(String text) {
        return escapeXmlValue(text);
    }

    // Copied from github.com/iBotPeaches/Apktool
    public static String escapeXmlValue(String str) {
        if (TextUtils.isEmpty(str)) return str;

        char[] chars = str.toCharArray();
        StringBuilder out = new StringBuilder(str.length() + 10);

        switch (chars[0]) {
            case '#':
            case '@':
            case '?':
                out.append('\\');
        }

        boolean isInStyleTag = false;
        int startPos = 0;
        boolean enclose = false;
        boolean wasSpace = true;
        for (char c : chars) {
            if (isInStyleTag) {
                if (c == '>') {
                    isInStyleTag = false;
                    startPos = out.length() + 1;
                    enclose = false;
                }
            } else if (c == ' ') {
                if (wasSpace) {
                    enclose = true;
                }
                wasSpace = true;
            } else {
                wasSpace = false;
                switch (c) {
                    case '\\':
                    case '"':
                        out.append('\\');
                        break;
                    case '\'':
                    case '\n':
                        enclose = true;
                        break;
                    case '<':
                        isInStyleTag = true;
                        if (enclose) {
                            out.insert(startPos, '"').append('"');
                        }
                        break;
                    default:
                }
            }
            out.append(c);
        }

        if (enclose || wasSpace) {
            out.insert(startPos, '"').append('"');
        }
        return out.toString();
    }
}
