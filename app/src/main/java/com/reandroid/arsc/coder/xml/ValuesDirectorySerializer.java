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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.utils.io.FileUtil;
import com.reandroid.utils.io.IOUtil;
import com.reandroid.xml.XMLFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ValuesDirectorySerializer implements ValuesSerializerFactory{

    private final File resourcesDir;
    private final Map<XmlSerializer, File> serializerFileMap;
    public ValuesDirectorySerializer(File resourcesDir){
        this.resourcesDir = resourcesDir;
        this.serializerFileMap = new HashMap<>();
    }
    @Override
    public void onFinish(XmlSerializer serializer, int writtenEntries) throws IOException {
        XmlDecodeUtil.rootIndent(serializer);
        serializer.endTag(null, PackageBlock.TAG_resources);
        serializer.endDocument();
        serializer.flush();
        IOUtil.close(serializer);
        File file = serializerFileMap.remove(serializer);
        if (writtenEntries == 0 && file != null && file.isFile()){
            file.delete();
            File dir = file.getParentFile();
            FileUtil.deleteEmptyDirectory(dir);
        }
    }
    @Override
    public XmlSerializer createSerializer(TypeBlock typeBlock) throws IOException {
        File dir = new File(resourcesDir,
                typeBlock.getPackageBlock().buildDecodeDirectoryName());
        dir = new File(dir,"res");
        dir = new File(dir,
                "values" + typeBlock.getResConfig().getQualifiers());
        String name = typeBlock.getTypeName();
        if(!name.endsWith("s")){
            name = name + "s";
        }
        name = name + ".xml";
        File file = new File(dir, name);
        XmlSerializer serializer = XMLFactory.newSerializer(file);
        serializerFileMap.put(serializer, file);
        serializer.startDocument("utf-8", null);
        XmlDecodeUtil.rootIndent(serializer);
        serializer.startTag(null, PackageBlock.TAG_resources);
        return serializer;
    }
}
