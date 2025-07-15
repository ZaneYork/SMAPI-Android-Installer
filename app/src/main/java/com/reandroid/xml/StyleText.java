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
package com.reandroid.xml;

import java.io.IOException;

public class StyleText extends XMLText implements StyleNode{
    public StyleText(String text){
        super(text);
    }
    public StyleText(){
        this("");
    }
    void writeStyledText(Appendable appendable) throws IOException {
        appendable.append(getText(false));
    }
    @Override
    public int getLength(){
        return getTextLength();
    }
    @Override
    public int getTextLength(){
        String text = getText();
        if(text != null){
            return text.length();
        }
        return 0;
    }
    @Override
    public void appendChar(char ch) {
        if(ch == 0){
            return;
        }
        appendText(ch);
    }
    @Override
    public StyleNode getParentStyle() {
        return (StyleNode) getParentNode();
    }
    @Override
    public void addStyleNode(StyleNode styleNode){
        throw new IllegalArgumentException("Text can't add node");
    }
    @Override
    boolean isIndent(){
        return false;
    }
}
