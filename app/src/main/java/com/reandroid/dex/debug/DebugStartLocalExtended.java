package com.reandroid.dex.debug;

import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.reference.Base1Ule128IdItemReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliDebugElement;
import com.reandroid.dex.smali.model.SmaliDebugLocal;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class DebugStartLocalExtended extends DebugStartLocal {

    private final Base1Ule128IdItemReference<StringId> mSignature;

    public DebugStartLocalExtended(){
        super(1, DebugElementType.START_LOCAL_EXTENDED);
        this.mSignature = new Base1Ule128IdItemReference<>(SectionType.STRING_ID);

        addChild(4, mSignature);
    }

    @Override
    public boolean isValid(){
        return !isRemoved() && mSignature.getItem() != null;
    }
    public String getSignature(){
        StringId stringId = mSignature.getItem();
        if(stringId != null){
            return stringId.getString();
        }
        return null;
    }
    public StringKey getSignatureKey(){
        return (StringKey) mSignature.getKey();
    }
    public void setSignature(String signature){
        this.mSignature.setItem(StringKey.create(signature));
    }
    public void setSignature(StringKey key){
        this.mSignature.setItem(key);
    }

    @Override
    public void appendExtra(SmaliWriter writer) throws IOException {
        if(isValid()) {
            super.appendExtra(writer);
            writer.append(", ");
            mSignature.append(writer);
        }
    }
    @Override
    public DebugElementType<DebugStartLocalExtended> getElementType() {
        return DebugElementType.START_LOCAL_EXTENDED;
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return CombiningIterator.two(super.usedIds(),
                SingleIterator.of(mSignature.getItem()));
    }
    @Override
    public void merge(DebugElement element){
        super.merge(element);
        DebugStartLocalExtended coming = (DebugStartLocalExtended) element;
        this.mSignature.setItem(coming.mSignature.getKey());
    }

    @Override
    public void fromSmali(SmaliDebugElement smaliDebugElement) throws IOException {
        super.fromSmali(smaliDebugElement);
        setSignature(((SmaliDebugLocal)smaliDebugElement).getSignature());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DebugStartLocalExtended debug = (DebugStartLocalExtended) obj;
        return getFlag() == debug.getFlag() &&
                Objects.equals(getName(), debug.getName()) &&
                Objects.equals(getType(), debug.getType())&&
                Objects.equals(getSignature(), debug.getSignature());
    }
    @Override
    public int hashCode() {
        int hash = getFlag();
        hash = hash * 31;
        String text = getName();
        if(text != null){
            hash = hash + text.hashCode();
        }
        hash = hash * 31;
        text = getType();
        if(text != null){
            hash = hash + text.hashCode();
        }
        hash = hash * 31;
        text = getSignature();
        if(text != null){
            hash = hash + text.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + mSignature;
    }
}
