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

import com.reandroid.xml.kxml2.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class CloseableParser extends KXmlParser implements Closeable {
    private InputStream inputStream;
    private Reader reader;

    public CloseableParser(){
        super();
    }

    @Override
    public int next() throws XmlPullParserException, IOException {
        int event = super.next();
        if(event == XmlPullParser.END_DOCUMENT){
            close();
        }
        return event;
    }
    @Override
    public int nextToken() throws XmlPullParserException, IOException {
        int event = super.nextToken();
        if(event == XmlPullParser.END_DOCUMENT){
            close();
        }
        return event;
    }
    @Override
    public void setInput(Reader reader) throws XmlPullParserException{
        super.setInput(reader);
        this.reader = reader;
    }
    @Override
    public void setInput(InputStream is, String charset) throws XmlPullParserException{
        super.setInput(is, charset);
        this.inputStream = is;
    }
    @Override
    public void close() throws IOException {
        if(reader != null){
            reader.close();
            reader = null;
        }
        if(inputStream != null){
            inputStream.close();
            inputStream = null;
        }
    }
}
