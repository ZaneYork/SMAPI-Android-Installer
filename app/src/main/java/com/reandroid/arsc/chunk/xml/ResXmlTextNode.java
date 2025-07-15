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
package com.reandroid.arsc.chunk.xml;

import com.reandroid.arsc.coder.XmlSanitizer;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.json.JSONObject;
import com.reandroid.xml.XMLNode;
import com.reandroid.xml.XMLText;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class ResXmlTextNode extends ResXmlNode {
    private final ResXmlText resXmlText;
    private String mIndentText;
    public ResXmlTextNode(ResXmlText resXmlText) {
        super(1);
        this.resXmlText = resXmlText;
        addChild(0, resXmlText);
    }
    public ResXmlTextNode() {
        this(new ResXmlText());
    }

    void makeIndent(int length){
        if(!isIndent()){
            throw new IllegalArgumentException("Not indent text: '" + getText() + "'");
        }
        if(length < 2){
            setText("\n");
            return;
        }
        char[] chars = new char[length];
        chars[0] = '\n';
        for(int i = 1; i < length; i++){
            chars[i] = ' ';
        }
        setText(new String(chars));
    }
    public boolean isIndent(){
        return isIndent(getText());
    }
    ResXmlText getResXmlText() {
        return resXmlText;
    }
    public int getLineNumber(){
        return getResXmlText().getLineNumber();
    }

    @Override
    int autoSetLineNumber(int start){
        String text = getText();
        int lineNumber = start;
        if(isIndent(text) && isNextElement()){
            lineNumber ++;
        }else {
            char[] chars = text.toCharArray();
            for(char ch : chars){
                if(ch == '\n'){
                    start ++;
                }
            }
        }
        setLineNumber(lineNumber);
        return start;
    }
    private boolean isNextElement(){
        ResXmlElement parent = getParentResXmlElement();
        if(parent != null){
            return parent.get(getIndex() + 1) instanceof ResXmlElement;
        }
        return false;
    }
    public String getComment() {
        return getResXmlText().getComment();
    }
    @Override
    public int getDepth(){
        ResXmlElement parent = getParentResXmlElement();
        if(parent!=null){
            return parent.getDepth() + 1;
        }
        return 0;
    }
    @Override
    void addEvents(ParserEventList parserEventList){
        String comment = getComment();
        if(comment!=null){
            parserEventList.add(
                    new ParserEvent(ParserEvent.COMMENT, this, comment, false));
        }
        parserEventList.add(new ParserEvent(ParserEvent.TEXT, this));
    }
    public ResXmlElement getParentResXmlElement(){
        return getResXmlText().getParentResXmlElement();
    }

    public void setLineNumber(int lineNumber){
        getResXmlText().setLineNumber(lineNumber);
    }
    public String getText(){
        return getResXmlText().getText();
    }
    public void setText(String text){
        getResXmlText().setText(text);
        mIndentText = null;
    }
    public void append(String text){
        String exist = getText();
        if(exist == null || exist.length() == 0){
            exist = mIndentText;
        }
        if(exist == null && isIndent(text)){
            mIndentText = text;
            return;
        }
        if(exist != null){
            text = exist + text;
        }
        setText(text);
    }

    @Override
    public boolean isNull() {
        return getResXmlText().isNull();
    }

    @Override
    void onRemoved(){
        getResXmlText().onRemoved();
    }
    @Override
    void linkStringReferences(){
        getResXmlText().linkStringReferences();
    }
    @Override
    public void serialize(XmlSerializer serializer, boolean decode) throws IOException {
        if (!isNull()) {
            serializer.text(getText());
        }
    }

    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        setLineNumber(parser.getLineNumber());
        String text;
        int event = parser.getEventType();
        if(event == XmlPullParser.ENTITY_REF){
            text = decodeEntityRef(parser.getText());
        }else if(event == XmlPullParser.TEXT){
            text = parser.getText();
            text = XmlSanitizer.unEscapeUnQuote(text);
        }else {
            throw new XmlPullParserException("Invalid text event: "
                    + event + ", " + parser.getPositionDescription());
        }
        append(text);
    }

    @Override
    public XMLNode toXml(boolean decode) {
        return new XMLText(getText());
    }

    public void mergeWithName(ResourceMergeOption mergeOption, ResXmlTextNode textNode){
        setText(textNode.getText());
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(NAME_node_type, NAME_text);
        jsonObject.put(NAME_text, getText());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setText(json.optString(NAME_text, null));
        setLineNumber(getParentResXmlElement().getStartLineNumber());
    }
    public XMLText decodeToXml() {
        XMLText xmlText=new XMLText(XmlSanitizer.escapeSpecialCharacter(getText()));
        xmlText.setLineNumber(getLineNumber());
        return xmlText;
    }
    @Override
    public String toString(){
        return "line = " + getLineNumber() + ", \"" + getText() + "\"";
    }

    private static String decodeEntityRef(String entityRef) {
        if(entityRef == null){
            return "";
        }
        String decode;
        if(entityRef.equals("lt")){
            decode = "<";
        }else if(entityRef.equals("gt")){
            decode = ">";
        }else if(entityRef.equals("amp")){
            decode = "&";
        }else if(entityRef.equals("quote")){
            decode = "\"";
        }else {
            decode = "&" + entityRef + ";";
        }
        return decode;
    }
    static boolean isTextEvent(int event){
        return event == XmlPullParser.TEXT
                || event == XmlPullParser.ENTITY_REF;
    }
    private static boolean isIndent(String text){
        if(android.text.TextUtils.isEmpty(text)){
            return true;
        }
        char[] chars = text.toCharArray();
        if(chars[0] != '\n'){
            return false;
        }
        for(int i = 1; i < chars.length; i++){
            if(chars[i] != ' '){
                return false;
            }
        }
        return true;
    }

    public static final String NAME_text="text";
}
