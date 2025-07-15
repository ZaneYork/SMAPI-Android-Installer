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
package com.reandroid.arsc.coder;

import com.reandroid.arsc.coder.xml.AaptXmlStringDecoder;
import com.reandroid.arsc.coder.xml.RawXmlStringDecoder;
import com.reandroid.arsc.coder.xml.XmlCoderLogger;
import com.reandroid.arsc.coder.xml.XmlStringDecoder;

public class CoderSetting {

    private XmlStringDecoder stringDecoder;
    private XmlCoderLogger logger;

    public CoderSetting() {
        this.stringDecoder = new RawXmlStringDecoder();
    }

    public XmlStringDecoder getStringDecoder() {
        return stringDecoder;
    }
    public void setStringDecoder(XmlStringDecoder stringDecoder) {
        if(stringDecoder == null) {
            throw new NullPointerException("Null XmlStringDecoder");
        }
        this.stringDecoder = stringDecoder;
    }
    public boolean isAapt() {
        return stringDecoder instanceof AaptXmlStringDecoder;
    }

    public XmlCoderLogger getLogger() {
        return logger;
    }
    public void setLogger(XmlCoderLogger logger) {
        this.logger = logger;
    }
}
