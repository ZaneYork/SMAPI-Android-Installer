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
package com.reandroid.dex.sections;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.data.StringData;

import java.io.IOException;

public class StringDataArray extends DataSectionArray<StringData> {

    public StringDataArray(IntegerPair countAndOffset, Creator<StringData> creator) {
        super(countAndOffset, creator);
    }

    @Override
    public void readChildes(BlockReader reader) throws IOException {
        super.readChildes(reader);
        StringIdArray idArray = getStringIdArray();
        idArray.link(this);
    }


    private StringIdArray getStringIdArray(){
        SectionList sectionList = getParent(SectionList.class);
        if(sectionList != null){
            StringIdSection section = (StringIdSection) sectionList.getSection(SectionType.STRING_ID);
            if(section != null){
                return section.getItemArray();
            }
        }
        return null;
    }
}
