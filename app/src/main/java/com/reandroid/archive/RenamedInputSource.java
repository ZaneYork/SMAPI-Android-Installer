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
package com.reandroid.archive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RenamedInputSource<T extends InputSource> extends InputSource {
    private final T inputSource;
    public RenamedInputSource(String name, T input){
        super(name);
        this.inputSource=input;
        super.setMethod(input.getMethod());
        super.setSort(input.getSort());
    }

    @SuppressWarnings("unchecked")
    public<T1 extends InputSource> T1 getParentInputSource(Class<T1> instance){
        InputSource inputSource = getInputSource();
        if(instance.isInstance(inputSource)){
            return (T1) inputSource;
        }
        if(inputSource instanceof RenamedInputSource){
            RenamedInputSource<?> parent = (RenamedInputSource<?>) inputSource;
            return parent.getParentInputSource(instance);
        }
        return null;
    }
    public T getInputSource() {
        return inputSource;
    }
    @Override
    public void close(InputStream inputStream) throws IOException {
        getInputSource().close(inputStream);
    }
    @Override
    public long getLength() throws IOException {
        return getInputSource().getLength();
    }
    @Override
    public long getCrc() throws IOException {
        return getInputSource().getCrc();
    }
    @Override
    public void write(File file) throws IOException {
        getInputSource().write(file);
    }
    @Override
    public long write(OutputStream outputStream) throws IOException {
        return getInputSource().write(outputStream);
    }
    @Override
    public InputStream openStream() throws IOException {
        return getInputSource().openStream();
    }
}
