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

import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.*;

import java.io.IOException;

public class SmaliAnnotationItem extends SmaliSet<SmaliAnnotationElement> implements SmaliRegion {

    private final SmaliDirective smaliDirective;
    private AnnotationVisibility visibility;
    private TypeKey type;

    public SmaliAnnotationItem(boolean subAnnotation){
        super();
        this.smaliDirective = subAnnotation ? SmaliDirective.SUB_ANNOTATION :
                SmaliDirective.ANNOTATION;
        if(!subAnnotation) {
            visibility = AnnotationVisibility.BUILD;
        }
    }
    public SmaliAnnotationItem(){
        this(false);
    }

    public AnnotationVisibility getVisibility() {
        return visibility;
    }
    public void setVisibility(AnnotationVisibility visibility) {
        SmaliDirective smaliDirective = getSmaliDirective();
        if(smaliDirective == SmaliDirective.ANNOTATION && visibility == null) {
            throw new NullPointerException("Null annotation visibility");
        }
        if(smaliDirective == SmaliDirective.SUB_ANNOTATION && visibility != null) {
            throw new IllegalArgumentException("Can not set annotation visibility for: " + smaliDirective);
        }
        this.visibility = visibility;
    }

    public TypeKey getType() {
        return type;
    }
    public void setType(TypeKey type) {
        this.type = type;
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return smaliDirective;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        writer.appendOptional(getVisibility());
        writer.appendOptional(getType());
        writer.appendAllWithIndent(iterator());
        getSmaliDirective().appendEnd(writer);
    }

    @Override
    public void parse(SmaliReader reader) throws IOException{
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = getSmaliDirective();
        SmaliParseException.expect(reader, directive);
        if(directive == SmaliDirective.ANNOTATION) {
            AnnotationVisibility visibility = AnnotationVisibility.parse(reader);
            if(visibility == null) {
                throw new SmaliParseException("Unrecognized annotation visibility", reader);
            }
            setVisibility(visibility);
        }
        setType(TypeKey.read(reader));
        while (parseNext(reader) != null){
            reader.skipWhitespacesOrComment();
        }
        SmaliParseException.expect(reader, getSmaliDirective(), true);
    }
    @Override
    SmaliAnnotationElement createNext(SmaliReader reader) {
        reader.skipWhitespacesOrComment();
        if(reader.finished()) {
            return null;
        }
        if(getSmaliDirective().isEnd(reader)){
            return null;
        }
        return new SmaliAnnotationElement();
    }
}
