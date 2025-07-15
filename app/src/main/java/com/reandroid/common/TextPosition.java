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
package com.reandroid.common;

public class TextPosition {

    private int lineNumber;
    private int columnNumber;
    private String description;

    public TextPosition(int lineNumber, int columnNumber) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
    public TextPosition() {
        this(-1, -1);
    }

    public int getLineNumber() {
        return lineNumber;
    }
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    public int getColumnNumber() {
        return columnNumber;
    }
    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        int i = getLineNumber();
        if(i == -1) {
            builder.append("--");
        }else {
            builder.append(" line=");
            builder.append(i);
        }
        i = getColumnNumber();
        if(i == -1) {
            builder.append("--");
        }else {
            builder.append(" column=");
            builder.append(i);
        }
        builder.append(']');
        String description = getDescription();
        if(description != null) {
            builder.append(' ');
            builder.append(description);
        }
        return builder.toString();
    }
}
