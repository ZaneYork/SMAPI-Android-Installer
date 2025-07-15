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

import java.io.File;

public class XmlEncodeUtil {


    public static String getQualifiersFromValuesXml(File valuesXmlFile){
        String dirName = valuesXmlFile.getParentFile().getName();
        int i = dirName.indexOf('-');
        if(i > 0){
            return dirName.substring(i);
        }
        return "";
    }
    public static String getTypeFromValuesXml(File valuesXmlFile){
        String name = valuesXmlFile.getName();
        name = name.substring(0, name.length() - 4);
        if(!name.equals("plurals") && name.endsWith("s")){
            name = name.substring(0, name.length()-1);
        }
        return name;
    }
}
