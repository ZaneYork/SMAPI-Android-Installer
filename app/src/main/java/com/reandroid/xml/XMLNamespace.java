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

import com.reandroid.common.Namespace;

import java.util.Objects;

public class XMLNamespace implements Namespace, Cloneable {
    private String uri;
    private String prefix;

    public XMLNamespace(String uri, String prefix){
        this.uri = uri;
        this.prefix = prefix;
    }

    XMLNamespace newCopy(XMLNode parent){
        XMLNamespace xmlNamespace = new XMLNamespace(getUri(), getPrefix());
        if(parent instanceof XMLElement){
            ((XMLElement)parent).addNamespace(xmlNamespace);
        }
        return xmlNamespace;
    }
    @Override
    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }
    @Override
    public String getPrefix() {
        return prefix;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    public boolean isEqual(String uri, String prefix){
        return Objects.equals(uri, getUri())
                && Objects.equals(prefix, getPrefix());
    }

    static boolean looksNamespace(String name, String value){
        if(value==null || !name.startsWith("xmlns:")){
            return false;
        }
        return true;
    }
}
