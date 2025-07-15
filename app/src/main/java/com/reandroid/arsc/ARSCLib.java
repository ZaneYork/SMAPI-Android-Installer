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
package com.reandroid.arsc;

import java.io.InputStream;
import java.util.Properties;

public class ARSCLib {
    private static Properties sProperties;

    public static String getName(){
        Properties properties=getProperties();
        return properties.getProperty("lib.name", "ARSCLib");
    }
    public static String getVersion(){
        Properties properties=getProperties();
        return properties.getProperty("lib.version", "");
    }
    public static String getRepo(){
        Properties properties=getProperties();
        return properties.getProperty("lib.repo", "https://github.com/REAndroid");
    }
    public static String getDescription(){
        Properties properties=getProperties();
        return properties.getProperty("lib.description", "Failed to load properties");
    }
    
    private static Properties getProperties(){
        if(sProperties==null){
            sProperties=loadProperties();
        }
        return sProperties;
    }
    private static Properties loadProperties(){
        InputStream inputStream= ARSCLib.class.getResourceAsStream("/arsclib.properties");
        Properties properties=new Properties();
        try{
            properties.load(inputStream);
        }catch (Exception ignored){
        }
        return properties;
    }

    public static final String NAME_arsc_lib_version = "arsc_lib_version";
}
