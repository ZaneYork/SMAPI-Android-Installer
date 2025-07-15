package com.reandroid.arsc.value.bag;

import com.reandroid.arsc.value.Entry;

public interface Bag {
    Entry getEntry();
    default String getName(){
        Entry entry =getEntry();
        if(entry ==null){
            return null;
        }
        return entry.getName();
    }
    default String getTypeName(){
        Entry entry =getEntry();
        if(entry ==null){
            return null;
        }
        return entry.getTypeName();
    }
}
