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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;

public class SmaliParamSet extends SmaliSet<SmaliMethodParameter>{

    public SmaliParamSet(){
        super();
    }

    @Override
    SmaliMethodParameter createNext(SmaliReader reader) {
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if(directive == SmaliDirective.PARAM){
            return new SmaliMethodParameter();
        }
        return null;
    }
}
