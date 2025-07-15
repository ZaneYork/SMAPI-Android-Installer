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
package com.reandroid.xml.source;

import com.reandroid.xml.XMLFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;

public class XMLFileParserSource implements XMLParserSource{
    private final String path;
    private final File file;

    public XMLFileParserSource(String path, File file){
        this.path = path;
        this.file = file;
    }

    @Override
    public XmlPullParser getParser() throws XmlPullParserException {
        return XMLFactory.newPullParser(getFile());
    }
    @Override
    public String getPath() {
        return path;
    }
    public File getFile() {
        return file;
    }
}
