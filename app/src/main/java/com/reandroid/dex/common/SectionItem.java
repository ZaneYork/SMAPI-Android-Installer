package com.reandroid.dex.common;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.item.BlockItem;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;

public class SectionItem extends BlockItem implements EditableItem, SectionTool, UsageMarker {

    private int mUsageType;
    private Key mLastKey;
    private SectionItem mReplace;

    public SectionItem(int bytesLength) {
        super(bytesLength);
    }

    public Key getKey(){
        return null;
    }
    public int getIdx(){
        throw new RuntimeException("Not applicable for: " + getClass());
    }

    public SectionType<? extends SectionItem> getSectionType(){
        throw new RuntimeException("Not implemented for " + getClass());
    }
    @SuppressWarnings("unchecked")
    public<T1 extends SectionItem> T1 getReplace() {
        if(mReplace == null){
            if(isRemovedInternal()){
                return null;
            }
            return (T1) this;
        }
        return mReplace.getReplace();
    }
    boolean isRemovedInternal(){
        return getParent() == null;
    }
    public void setReplace(SectionItem replace) {
        if(replace == this){
            return;
        }
        if(replace == null){
            this.mReplace = null;
            return;
        }
        if(getClass() != replace.getClass()){
            throw new IllegalArgumentException("Incompatible replace: "
                    + getClass() + ", " + replace.getClass());
        }else if(replace.getParent() == null){
            replace = null;
        }else if(replace.mReplace == this){
            throw new IllegalStateException("Cyclic replace set: " + getKey());
        }else {
            replace.addUsageType(getUsageType());
        }
        this.mReplace = replace;
    }
    public boolean isRemoved(){
        return getParent() == null;
    }
    @SuppressWarnings("unchecked")
    protected <T1 extends Key> T1 checkKey(T1 newKey){
        Key lastKey = this.mLastKey;
        if(lastKey == null || !lastKey.equals(newKey)){
            this.mLastKey = newKey;
            keyChanged(lastKey);
            lastKey = newKey;
        }
        return (T1) lastKey;
    }
    protected void keyChanged(Key oldKey){
        if(oldKey == null){
            return;
        }
        SectionList sectionList = getSectionList();
        if(sectionList != null){
            sectionList.keyChangedInternal(this, getSectionType(), oldKey);
        }
    }
    public boolean isSameContext(SectionItem sectionItem){
        return getSectionList() == sectionItem.getSectionList();
    }
    public boolean isSameContext(SectionList sectionList){
        return getSectionList() == sectionList;
    }
    @Override
    public int getUsageType() {
        return mUsageType;
    }
    @Override
    public void addUsageType(int usage){
        this.mUsageType |= usage;
        SectionItem replace = this.getReplace();
        if(replace != null && replace != this){
            replace.mUsageType |= usage;
        }
    }
    @Override
    public boolean containsUsage(int usage){
        if(usage == 0){
            return this.mUsageType == 0;
        }
        return (this.mUsageType & usage) == usage;
    }
    @Override
    public void clearUsageType(){
        this.mUsageType = UsageMarker.USAGE_NONE;
    }

    @Override
    public void editInternal(Block user) {

    }
    public void removeSelf(){
        throw new RuntimeException("Not implemented");
    }
    public boolean equalsKey(SectionItem sectionItem){
        if(sectionItem == this){
            return true;
        }
        if(sectionItem == null || getSectionType() != sectionItem.getSectionType()){
            return false;
        }
        Key key = getKey();
        if(key == null){
            return false;
        }
        return key.equals(sectionItem.getKey());
    }
    public boolean equalsKey(Key key){
        if(key == null){
            return false;
        }
        Key myKey = getKey();
        if(myKey == null){
            return false;
        }
        return myKey.equals(key);
    }
}
