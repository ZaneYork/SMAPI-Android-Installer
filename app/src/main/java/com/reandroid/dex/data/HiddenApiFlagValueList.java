package com.reandroid.dex.data;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class HiddenApiFlagValueList extends BlockList<HiddenApiFlagValue> {

    private DefArray<?> defArray;

    HiddenApiFlagValueList(Creator<? extends HiddenApiFlagValue> creator){
        super(creator);
    }
    public HiddenApiFlagValueList(){
        this(CREATOR);
    }

    HiddenApiFlagValueList newCopy(){
        return new Copy(this);
    }

    public boolean isEmptyValueList(){
        return size() != 0;
    }
    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        ensureDefArraySize();
        sort();
    }

    private void ensureDefArraySize(){
        int size;
        if(defArray == null){
            size = 0;
        }else {
            size = defArray.size();
        }
        setSize(size);
    }
    public boolean sort() {
        if(defArray == null){
            return false;
        }
        return super.sort(CompareUtil.getComparableComparator());
    }

    void linkDefArray(DefArray<?> defArray) {
        this.defArray = defArray;
        if(defArray == null){
            setSize(0);
            return;
        }
        int size = defArray.size();
        setSize(size);
        for(int i = 0; i < size; i++){
            get(i).linkDef(defArray.get(i));
        }
    }

    @Override
    protected void onReadBytes(BlockReader reader) throws IOException {
        readChildes(reader);
    }

    static class Copy extends HiddenApiFlagValueList{

        private final HiddenApiFlagValueList source;

        Copy(HiddenApiFlagValueList source){
            super(new CopyCreator(source));
            this.source = source;
        }
        @Override
        HiddenApiFlagValueList newCopy() {
            return new Copy(source);
        }

        @Override
        public boolean isEmptyValueList() {
            return source.isEmptyValueList();
        }
        @Override
        protected void onPreRefresh() {
        }
        @Override
        public int countBytes() {
            return 0;
        }
        @Override
        protected void onReadBytes(BlockReader reader) throws IOException {
        }
        @Override
        public void readChildes(BlockReader reader) throws IOException {
        }
        @Override
        protected int onWriteBytes(OutputStream stream) throws IOException {
            return 0;
        }
    }


    static class CopyCreator implements Creator<HiddenApiFlagValue>{

        private final HiddenApiFlagValueList source;

        CopyCreator(HiddenApiFlagValueList source){
            this.source = source;
        }

        @Override
        public HiddenApiFlagValue[] newArrayInstance(int length) {
            return new HiddenApiFlagValue[length];
        }
        @Override
        public HiddenApiFlagValue newInstance() {
            throw new RuntimeException("Call newInstanceAt()");
        }
        @Override
        public HiddenApiFlagValue newInstanceAt(int index) {
            return source.get(index).newCopy();
        }
    }

    private static final Creator<HiddenApiFlagValue> CREATOR = new Creator<HiddenApiFlagValue>() {
        @Override
        public HiddenApiFlagValue[] newArrayInstance(int length) {
            return new HiddenApiFlagValue[length];
        }
        @Override
        public HiddenApiFlagValue newInstance() {
            return new HiddenApiFlagValue();
        }
    };
}
