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
package com.reandroid.utils.io;

import java.io.*;
import java.util.Iterator;

public class StringLineStream implements Iterator<String>, Closeable {

    private final InputStream inputStream;
    private BufferedReader mReader;
    private String mCurrent;
    private IOException mError;
    private boolean mFinished;
    private int mLineNumber;

    public StringLineStream(InputStream inputStream){
        this.inputStream = inputStream;
    }

    public int getLineNumber() {
        return mLineNumber;
    }
    public IOException getError() {
        return mError;
    }
    @Override
    public boolean hasNext() {
        return getCurrent() != null;
    }
    @Override
    public String next() {
        String current = getCurrent();
        mCurrent = null;
        return current;
    }
    @Override
    public void close() throws IOException {
        mFinished = true;
        inputStream.close();
        BufferedReader reader = mReader;
        if(reader == null){
            return;
        }
        try{
            reader.close();
        }catch (Exception ignored){
        }
    }
    private String getCurrent(){
        String current = mCurrent;
        if(current == null){
            current = readNext();
            mCurrent = current;
        }
        return current;
    }
    private String readNext(){
        if(mFinished){
            return null;
        }
        try {
            String line = getReader().readLine();
            if(line == null){
                mFinished = true;
                close();
            }else {
                mLineNumber ++;
            }
            return line;
        } catch (IOException exception) {
            mError = exception;
            mFinished = true;
            try {
                close();
            } catch (Exception ignored) {
            }
            return null;
        }
    }
    private BufferedReader getReader(){
        if(mReader == null){
            mReader = new BufferedReader(new InputStreamReader(inputStream));
        }
        return mReader;
    }

    @Override
    public String toString() {
        String current = mCurrent;
        StringBuilder builder = new StringBuilder();
        builder.append("line=");
        builder.append(mLineNumber);
        if(mFinished){
            builder.append(", FINISHED");
        }
        if(current != null){
            builder.append(", ");
            builder.append(current);
        }
        return builder.toString();
    }
}
