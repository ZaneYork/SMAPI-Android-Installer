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

import com.reandroid.dex.smali.*;

import java.io.IOException;

public abstract class SmaliCodeExceptionHandler extends SmaliCode implements SmaliRegion {

    private final SmaliLabel start;
    private final SmaliLabel end;
    private final SmaliLabel catchLabel;

    public SmaliCodeExceptionHandler(){
        super();
        this.start = new SmaliLabel();
        this.end = new SmaliLabel();
        this.catchLabel = new SmaliLabel();

        this.start.setParent(this);
        this.end.setParent(this);
        this.catchLabel.setParent(this);
    }

    public int getAddress(){
        SmaliCodeTryItem tryItem = getTryItem();
        if(tryItem == null){
            return -1;
        }
        return tryItem.getAddress();
    }
    public SmaliLabel getStart() {
        return start;
    }
    public SmaliLabel getEnd() {
        return end;
    }
    public SmaliLabel getCatchLabel() {
        return catchLabel;
    }

    SmaliCodeTryItem getTryItem(){
        return getParentInstance(SmaliCodeTryItem.class);
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        appendType(writer);
        writer.append('{');
        getStart().append(writer);
        writer.append(" .. ");
        getEnd().append(writer);
        writer.append('}');
        writer.append(' ');
        getCatchLabel().append(writer);
    }
    public void appendType(SmaliWriter writer) throws IOException {

    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        SmaliParseException.expect(reader, getSmaliDirective());
        parseType(reader);
        reader.skipSpaces();
        SmaliParseException.expect(reader, '{');
        getStart().parse(reader);
        reader.skipSpaces();
        SmaliParseException.expect(reader, '.');
        SmaliParseException.expect(reader, '.');
        getEnd().parse(reader);
        reader.skipSpaces();
        SmaliParseException.expect(reader, '}');
        getCatchLabel().parse(reader);
    }
    void parseType(SmaliReader reader) throws IOException {
    }
}
