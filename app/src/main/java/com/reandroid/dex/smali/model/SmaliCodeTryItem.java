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
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.InstanceIterator;

import java.io.IOException;
import java.util.Iterator;

public class SmaliCodeTryItem extends SmaliCode{

    private final SmaliSet<SmaliCodeCatch> catchSet;
    private SmaliCodeCatchAll catchAll;

    public SmaliCodeTryItem(){
        super();
        this.catchSet = new SmaliSet<>();
        this.catchSet.setParent(this);
    }


    public int getStartAddress(){
        SmaliLabel label = pickStartLabel();
        if(label != null){
            return label.getAddress();
        }
        return -1;
    }
    public int getAddress(){
        SmaliCodeSet codeSet = getCodeSet();
        if(codeSet == null){
            return -1;
        }
        Iterator<SmaliCode> iterator = codeSet.iterator(codeSet.indexOf(this) + 1);
        SmaliInstruction next = CollectionUtil.getFirst(
                InstanceIterator.of(iterator, SmaliInstruction.class));
        if(next != null){
            return next.getAddress();
        }
        return -1;
    }

    public SmaliCodeCatchAll getCatchAll() {
        return catchAll;
    }
    public void setCatchAll(SmaliCodeCatchAll catchAll) {
        this.catchAll = catchAll;
        if(catchAll != null){
            catchAll.setParent(this);
        }
    }
    public SmaliSet<SmaliCodeCatch> getCatchSet() {
        return catchSet;
    }
    private SmaliLabel pickStartLabel(){
        Iterator<SmaliCodeCatch> iterator = getCatchSet().iterator();
        if (iterator.hasNext()){
            return iterator.next().getStart();
        }
        SmaliCodeCatchAll catchAll = getCatchAll();
        if(catchAll != null){
            return catchAll.getStart();
        }
        return null;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendAll(getCatchSet().iterator());
        SmaliCodeCatchAll catchAll = getCatchAll();
        if(catchAll != null){
            writer.newLine();
            catchAll.append(writer);
        }
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        parseCatches(reader);
        parseCatchAll(reader);
    }
    private void parseCatches(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliSet<SmaliCodeCatch> catchSet = getCatchSet();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        while (directive == SmaliDirective.CATCH){
            int position = reader.position();
            SmaliCodeCatch codeCatch = new SmaliCodeCatch();
            codeCatch.parse(reader);
            if(isDifferentGroup(codeCatch)){
                reader.position(position);
                break;
            }
            catchSet.add(codeCatch);
            reader.skipWhitespacesOrComment();
            directive = SmaliDirective.parse(reader, false);
        }
    }
    private void parseCatchAll(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if(directive == SmaliDirective.CATCH_ALL){
            SmaliCodeCatchAll catchAll = new SmaliCodeCatchAll();
            int position = reader.position();
            catchAll.parse(reader);
            if(isDifferentGroup(catchAll)){
                reader.position(position);
            }else {
                setCatchAll(catchAll);
            }
        }
    }
    private boolean isDifferentGroup(SmaliCodeExceptionHandler exceptionHandler){
        SmaliLabel smaliLabel = pickStartLabel();
        return smaliLabel != null && !smaliLabel.equals(exceptionHandler.getStart());
    }
}
