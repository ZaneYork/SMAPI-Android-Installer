package com.reandroid.dex.smali.model;

import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public abstract class SmaliDefSet<T extends SmaliDef> extends SmaliSet<T>
        implements SmaliRegion {

    private TypeKey defining;

    public SmaliDefSet(){
        super();
    }

    public TypeKey getDefining() {
        TypeKey typeKey = this.defining;
        if(typeKey == null) {
            SmaliClass smaliClass = getSmaliClass();
            if(smaliClass != null){
                typeKey = smaliClass.getKey();
            }
        }
        return typeKey;
    }
    public void setDefining(TypeKey defining) {
        this.defining = defining;
    }

    abstract T createNew();

    public SmaliClass getSmaliClass(){
        return getParentInstance(SmaliClass.class);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendAllWithDoubleNewLine(iterator());
    }

    @Override
    T createNext(SmaliReader reader) {
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if(directive != getSmaliDirective()){
            return null;
        }
        return createNew();
    }
}
