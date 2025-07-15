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
package com.reandroid.dex.model;

import com.reandroid.archive.ByteInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.utils.io.FileUtil;

import java.io.*;

public interface DexSource<T> extends Comparable<DexSource<?>>, Closeable{

    String getName();
    InputStream openStream() throws IOException;
    void write(byte[] bytes) throws IOException;
    boolean delete();
    T get();
    void set(T item);
    @Override
    void close() throws IOException;
    boolean isClosed();

    default int getDexFileNumber(){
        return DexFile.getDexFileNumber(getName());
    }
    default String getSimpleName() {
        String name = getName();
        int i = name.lastIndexOf('/');
        if(i < 0){
            i = name.lastIndexOf('\\');
        }
        if(i >= 0){
            name = name.substring(i + 1);
        }
        return name;
    }
    default DexSource<T> createNext(){
        throw new RuntimeException("Method not implemented");
    }
    default DexSource<T> initializeNew(){
        throw new RuntimeException("Method not implemented");
    }
    @Override
    default int compareTo(DexSource<?> dexSource){
        return Integer.compare(getDexFileNumber(), dexSource.getDexFileNumber());
    }

    static<T> DexSource<T> create(ZipEntryMap zipEntryMap, String name){
        return new ZipDexSource<>(zipEntryMap, name);
    }
    static<T> DexSource<T> create(ZipEntryMap zipEntryMap, String name, T item){
        return new ZipDexSource<>(zipEntryMap, name, item);
    }
    static<T> DexSource<T> create(File file){
        return new FileDexSource<>(file);
    }
    abstract class DexSourceImpl<T> implements DexSource<T> {

        private T item;
        private boolean closed;

        @Override
        public T get() {
            return item;
        }
        @Override
        public void set(T item) {
            if(isClosed()){
                return;
            }
            this.item = item;
        }
        @Override
        public void close() throws IOException{
            this.closed = true;
            T item = this.item;
            this.item = null;
            if(item instanceof Closeable){
                ((Closeable) item).close();
            }
        }
        public boolean isClosed() {
            return closed;
        }

        @Override
        public boolean delete() {
            set(null);
            return onDelete();
        }
        abstract boolean onDelete();

        @Override
        public boolean equals(Object obj) {
            if(this == obj) {
                return true;
            }
            if(!(obj instanceof DexSource)) {
                return false;
            }
            DexSource<?> dexSource = (DexSource<?>) obj;
            return getDexFileNumber() == dexSource.getDexFileNumber();
        }
        @Override
        public int hashCode() {
            return getDexFileNumber();
        }
        @Override
        public String toString() {
            return getSimpleName();
        }
    }
    class FileDexSource<T> extends DexSourceImpl<T> {

        private final File file;

        public FileDexSource(File file) {
            super();
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        @Override
        public String getSimpleName() {
            return getFile().getName();
        }

        @Override
        public String getName() {
            return getFile().getAbsolutePath();
        }

        @Override
        boolean onDelete() {
            if(isClosed()){
                return false;
            }
            File file = getFile();
            if(file.isFile()){
                return file.delete();
            }
            return true;
        }

        @Override
        public InputStream openStream() throws IOException {
            if(isClosed()){
                throw new IOException("Closed: " + getName());
            }
            return new FileInputStream(getFile());
        }

        @Override
        public void write(byte[] bytes) throws IOException {
            if(isClosed()){
                throw new IOException("Closed: " + getName());
            }
            OutputStream outputStream = FileUtil.outputStream(getFile());
            outputStream.write(bytes, 0, bytes.length);
            outputStream.close();
        }

        @Override
        public DexSource<T> initializeNew() {
            return null;
        }

        @Override
        public FileDexSource<T> createNext(){
            if(isClosed()){
                return null;
            }
            int index = getDexFileNumber() + 1;
            File file = getFile(index);
            while (file.isFile()){
                index ++;
                file = getFile(index);
            }
            return new FileDexSource<>(file);
        }
        private File getFile(int dexIndex){
            String name = DexFile.getDexName(dexIndex);
            File dir = getFile().getParentFile();
            File file;
            if(dir == null){
                file = new File(name);
            }else {
                file = new File(dir, name);
            }
            return file;
        }
    }
    class ZipDexSource<T> extends DexSourceImpl<T> {

        private final ZipEntryMap zipEntryMap;
        private final String name;

        public ZipDexSource(ZipEntryMap zipEntryMap, String name) {
            super();
            this.zipEntryMap = zipEntryMap;
            this.name = name;
        }
        public ZipDexSource(ZipEntryMap zipEntryMap, String name, T item) {
            this(zipEntryMap, name);
            set(item);
        }

        @Override
        public String getName() {
            return name;
        }
        @Override
        boolean onDelete() {
            if(isClosed()){
                return false;
            }
            this.zipEntryMap.remove(getName());
            return true;
        }
        @Override
        public InputStream openStream() throws IOException {
            if(isClosed()){
                throw new IOException("Closed: " + getName());
            }
            InputSource inputSource = zipEntryMap.getInputSource(getName());
            if(inputSource == null){
                throw new IOException("Zip input source not found: " + getName());
            }
            return inputSource.openStream();
        }
        @Override
        public void write(byte[] bytes) throws IOException {
            if(isClosed()){
                throw new IOException("Closed: " + getName());
            }
            ByteInputSource inputSource = new ByteInputSource(bytes, getName());
            zipEntryMap.add(inputSource);
        }

        @Override
        public ZipDexSource<T> createNext(){
            if(isClosed()){
                return null;
            }
            int index = getDexFileNumber() + 1;
            String name = getPath(index);
            ZipEntryMap zipEntryMap = this.zipEntryMap;
            while (zipEntryMap.contains(name)){
                index ++;
                name = getPath(index);
            }
            return new ZipDexSource<>(zipEntryMap, name);
        }
        private String getPath(int index){
            return FileUtil.combineUnixPath(FileUtil.getParent(getName()),
                    DexFile.getDexName(index));
        }
        @Override
        public String toString() {
            return zipEntryMap.getModuleName() + ":/" + getSimpleName();
        }
    }
}
