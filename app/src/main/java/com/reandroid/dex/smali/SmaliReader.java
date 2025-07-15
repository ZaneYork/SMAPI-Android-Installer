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
package com.reandroid.dex.smali;

import com.reandroid.common.ByteSource;
import com.reandroid.common.Origin;
import com.reandroid.common.TextPosition;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.NumbersUtil;
import com.reandroid.utils.io.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SmaliReader {

    private final ByteSource byteSource;
    private int position;

    private Origin origin;

    public SmaliReader(ByteSource byteSource) {
        this.byteSource = byteSource;
    }
    public SmaliReader(byte[] bytes) {
        this(ByteSource.of(bytes));
    }

    public void reset() {
        this.position(0);
    }
    public int position() {
        return position;
    }
    public void position(int position) {
        this.position = position;
    }
    public int available() {
        return this.byteSource.length() - this.position;
    }
    public boolean finished() {
        return available() == 0;
    }
    public void offset(int amount){
        position(position() + amount);
    }
    public byte get(){
        return get(position());
    }
    public char getASCII(int i){
        int c = get(i) & 0xff;
        return (char) c;
    }
    public byte get(int i) {
        return byteSource.read(i);
    }
    public char readASCII() {
        int i = read() & 0xff;
        return (char) i;
    }
    public byte read() {
        int i = position;
        position ++;
        return this.byteSource.read(i);
    }
    public String getString(int length){
        return new String(getBytes(length), StandardCharsets.UTF_8);
    }
    public String readString(int length){
        return new String(readBytes(length), StandardCharsets.UTF_8);
    }
    public String readEscapedString(char stopChar) throws IOException{
        int position = position();
        boolean utf8Detected = false;
        StringBuilder builder = new StringBuilder();
        boolean skipped = false;
        while (true){
            if(finished()){
                skip(-1);
                throw new SmaliParseException("Missing character '" + stopChar + "'", this);
            }
            char ch = readASCII();
            if(ch > 0x7f) {
                utf8Detected = true;
            }
            if(skipped){
                builder.append(decodeSkipped(this, ch));
                skipped = false;
                continue;
            }
            if(ch == '\\'){
                skipped = true;
                continue;
            }
            if(ch == stopChar){
                skip(-1);
                break;
            }
            builder.append(ch);
        }
        if(utf8Detected) {
            int len = position() - position;
            position(position);
            return decodeEscapedString(readString(len));
        }
        return builder.toString();
    }
    public String readStringForNumber(){
        int pos = position();
        int end = indexOfLineEnd();
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for(int i = pos; i < end; i++){
            byte b = get(i);
            if(!isNumber(b)){
                break;
            }
            char ch = (char) (0xff & b);
            builder.append(ch);
            count ++;
        }
        skip(count);
        return builder.toString();
    }

    public int readInteger() throws IOException{
        byte signByte = get();
        boolean negative = signByte == '-';
        if(negative || signByte == '+'){
            skip(1);
        }
        int result = 0;
        int pos = position();
        while (!finished()){
            int i = base10Digit(read());
            if(i == -1 || result < 0){
                skip(-1);
                break;
            }
            result = result * 10;
            result = result + i;
        }
        if(pos == position()){
            throw new SmaliParseException("Invalid integer format", this);
        }
        if(result < 0){
            skip(-1);
            throw new SmaliParseException("Integer overflow", this);
        }
        if(negative){
            result = -result;
        }
        return result;
    }
    public byte[] readBytes(int length){
        byte[] bytes = getBytes(length);
        offset(length);
        return bytes;
    }

    public byte[] getBytes(int length){
        byte[] result = new byte[length];
        int pos = position();
        for(int i = 0; i < length; i++){
            result[i] = get(pos + i);
        }
        return result;
    }
    public boolean startsWith(byte[] bytes){
        return startsWith(bytes, position());
    }
    public boolean startsWith(byte[] bytes, int start){
        int length = available();
        if(length < bytes.length){
            return false;
        }
        length = bytes.length;
        for(int i = 0; i < length; i++){
            if(bytes[i] != get(start + i)){
                return false;
            }
        }
        return true;
    }
    public int startsWithSqueezeSpaces(byte[] bytes){
        int pos = position();
        int length = available();
        int bytesLength = bytes.length;
        if(length == 0 || bytesLength == 0){
            return -1;
        }
        int index = 0;
        boolean prevSpace = false;
        for(int i = 0; i < length; i++){
            byte b1 = get(pos + i);
            if(b1 == ' '){
                if(prevSpace){
                    continue;
                }
                prevSpace = true;
            }else {
                prevSpace = false;
            }
            if(index == bytesLength){
                return i;
            }
            byte b2 = bytes[index];
            index ++;
            if(b1 != b2){
                return -1;
            }
        }
        if(index == bytesLength) {
            return bytesLength;
        }
        return -1;
    }
    public int indexOf(char ch){
        return indexOf((byte) ch);
    }
    public int indexOfWhiteSpace(){
        int pos = position();
        int end = pos + available();
        for(int i = pos; i < end; i++){
            if(isWhiteSpace(get(i))){
                return i;
            }
        }
        return end;
    }
    public int indexOfWhiteSpaceOrComment(){
        int pos = position();
        int end = pos + available();
        for(int i = pos; i < end; i++){
            if(isWhiteSpaceOrComment(get(i))){
                return i;
            }
        }
        return end;
    }
    public int indexOfLineEnd(){
        int pos = position();
        int end = pos + available();
        for(int i = pos; i < end; i++){
            if(isLineEnd(get(i))){
                return i;
            }
        }
        return end;
    }
    public int indexOf(byte b){
        return indexOf(position(), b);
    }
    public int indexOf(int start, byte b){
        return byteSource.indexOf(start, b);
    }
    public int indexOfBeforeLineEnd(char ch){
        int pos = position();
        int end = pos + available();
        for(int i = pos; i < end; i++){
            byte b = get(i);
            if(ch == b){
                return i;
            }
            if(isLineEnd(b)){
                return -1;
            }
        }
        return -1;
    }
    public int indexOf(byte[] bytes){
        int length = bytes.length;
        if(length == 0){
            return -1;
        }
        int pos = position();
        int end = pos + available() - length;
        for(int i = pos; i <= end; i++){
            if(equalsAt(i, bytes)){
                return i;
            }
        }
        return -1;
    }
    private boolean equalsAt(int index, byte[] bytes){
        int length = bytes.length;
        if(length > available() - index) {
            return false;
        }
        for(int i = 0; i < length; i++){
            if(bytes[i] != get(i + index)){
                return false;
            }
        }
        return true;
    }
    public boolean skipWhitespacesOrComment(){
        if(finished()) {
            return false;
        }
        boolean result = false;
        if(get() == '#'){
            nextLine();
            result = true;
        }
        while (skipWhitespaces()){
            if(get() == '#'){
                nextLine();
            }
            result = true;
        }
        return result;
    }
    public boolean skipWhitespaces(){
        if(finished()) {
            return false;
        }
        int pos = position();
        int nextPosition = pos;
        int end = pos + available();
        for(int i = pos; i < end; i++){
            if(!isWhiteSpace(get(i))){
                break;
            }
            nextPosition = i + 1;
        }
        if(nextPosition != pos){
            position(nextPosition);
            return nextPosition != end;
        }
        return false;
    }
    public boolean skipSpaces(){
        int pos = position();
        int nextPosition = pos;
        int end = pos + available();
        for(int i = pos; i < end; i++){
            if(!isSpace(get(i))){
                nextPosition = i;
                break;
            }
        }
        if(nextPosition != pos){
            position(nextPosition);
            return true;
        }
        return false;
    }
    public void nextLine(){
        int i = indexOf('\n');
        if(i < 0){
            i = position() + available();
        }
        position(i);
    }
    public void skip(int amount){
        int available = available();
        if(amount > available){
            amount = available;
        }
        position(amount + position());
    }

    public Origin getCurrentOrigin() {
        return getOrigin(position());
    }
    public Origin getOrigin(int position) {
        Origin origin =  this.getOrigin();
        origin = origin.createChild(new SmaliTextPosition(byteSource, position));
        return origin;
    }
    public Origin getOrigin() {
        Origin origin =  this.origin;
        if(origin == null) {
            origin = Origin.newRoot();
            this.origin = origin;
        }
        return origin;
    }
    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    @Override
    public String toString() {
        return getCurrentOrigin().toString();
    }

    public static boolean isWhiteSpaceOrComment(byte b){
        return isWhiteSpace(b) || b == '#';
    }
    public static boolean isWhiteSpace(byte b){
        switch (b){
            case ' ':
            case '\n':
            case '\t':
            case '\r':
                return true;
            default:
                return false;
        }
    }
    public static boolean isSpace(byte b){
        switch (b){
            case ' ':
            case '\t':
                return true;
            default:
                return false;
        }
    }
    private static int base10Digit(byte b){
        int bound = '0';
        if(b >= bound && b <= '9'){
            return b - bound;
        }
        return -1;
    }
    private static boolean isNumber(byte b){
        if(b >= '0' && b <= '9'){
            return true;
        }
        if(b >= 'a' && b <= 'z'){
            return true;
        }
        if(b >= 'A' && b <= 'z'){
            return true;
        }
        switch (b){
            case '-':
            case '+':
            case '.':
                return true;
            default:
                return false;
        }
    }
    private static boolean isLineEnd(byte b){
        switch (b){
            case '\n':
            case '\r':
            case '#':
                return true;
            default:
                return false;
        }
    }
    private static char decodeSkipped(SmaliReader reader, char ch){
        if(ch == 'u') {
            return decodeFourHex(reader);
        }
        return decodeSkippedChar(ch);
    }
    private static char decodeSkippedChar(char ch){
        switch (ch){
            case 'b':
                return '\b';
            case 'f':
                return  '\f';
            case 'n':
                return '\n';
            case 'r':
                return  '\r';
            case 't':
                return '\t';
            default:
                return ch;
        }
    }
    private static char decodeFourHex(SmaliReader reader){
        int i = HexUtil.decodeHexChar(reader.read());
        i = i << 4;
        i |= HexUtil.decodeHexChar(reader.read());
        i = i << 4;
        i |= HexUtil.decodeHexChar(reader.read());
        i = i << 4;
        i |= HexUtil.decodeHexChar(reader.read());
        return (char) i;
    }
    public static String decodeEscapedString(String text){
        StringBuilder builder = new StringBuilder();
        boolean skipped = false;
        int length = text.length();
        for (int i = 0; i < length; i++){
            char ch = text.charAt(i);
            if(skipped){
                if(ch == 'u') {
                    builder.append(decodeHex(
                            text.charAt(i + 1),
                            text.charAt(i + 2),
                            text.charAt(i + 3),
                            text.charAt(i + 4)));
                    i = i + 4;
                }else {
                    builder.append(decodeSkippedChar(ch));
                }
                skipped = false;
                continue;
            }
            if(ch == '\\'){
                skipped = true;
                continue;
            }
            builder.append(ch);
        }
        return builder.toString();
    }
    private static char decodeHex(char c1, char c2, char c3, char c4){
        int i = HexUtil.decodeHexChar(c1);
        i = i << 4;
        i |= HexUtil.decodeHexChar(c2);
        i = i << 4;
        i |= HexUtil.decodeHexChar(c3);
        i = i << 4;
        i |= HexUtil.decodeHexChar(c4);
        return (char) i;
    }
    public static SmaliReader of(String text){
        SmaliReader reader = new SmaliReader(text.getBytes());
        reader.setOrigin(Origin.createNew("<text-source>"));
        return reader;
    }
    public static SmaliReader of(File file) throws IOException {
        SmaliReader reader = new SmaliReader(IOUtil.readFully(file));
        reader.setOrigin(Origin.createNew(file));
        return reader;
    }
    public static SmaliReader of(InputStream inputStream) throws IOException {
        SmaliReader reader = new SmaliReader(IOUtil.readFully(inputStream));
        reader.setOrigin(Origin.createNew("<" + inputStream.getClass().getName() + ">"));
        return reader;
    }

    static class SmaliTextPosition extends TextPosition {

        private ByteSource byteSource;
        private final int position;

        public SmaliTextPosition(ByteSource byteSource, int position) {
            this.byteSource = byteSource;
            this.position = position;
        }

        @Override
        public int getLineNumber() {
            computeValues();
            return super.getLineNumber();
        }
        @Override
        public int getColumnNumber() {
            computeValues();
            return super.getColumnNumber();
        }
        private void computeValues() {
            if(this.byteSource == null) {
                return;
            }
            ByteSource byteSource = this.byteSource;
            this.byteSource = null;
            int line = 1;
            int column = 1;
            try {
                int end = NumbersUtil.min(position, byteSource.length());
                for(int i = 0; i < end; i++) {
                    if (byteSource.read(i) == '\n') {
                        line ++;
                        column = 1;
                    } else {
                        column ++;
                    }
                }
                setDescription(computePositionDescription(byteSource));
            } catch (Throwable throwable) {
                setDescription(throwable.getMessage());
            }
            setLineNumber(line);
            setColumnNumber(column);
            this.byteSource = null;
        }
        private String computePositionDescription(ByteSource byteSource) {
            int pos = this.position;
            if(pos >= byteSource.length()){
                return "EOF";
            }
            StringBuilder builder = new StringBuilder();
            builder.append('\n');
            int lineStart = pos;
            while (byteSource.read(lineStart) != '\n'){
                if(lineStart == 0){
                    break;
                }
                lineStart --;
            }
            if(byteSource.read(lineStart) == '\n'){
                lineStart ++;
            }
            int limit = 38;
            if(pos - lineStart > limit){
                lineStart = pos - limit;
            }
            int end = -1;
            if(byteSource.length() - pos > 1) {
                end = byteSource.indexOf(lineStart, (byte) '\n');
            }
            if(end < 0){
                if(pos == 0){
                    end = lineStart;
                }else {
                    end = pos;
                }
                end = end + (byteSource.length() - pos);
            }
            if(end - pos > limit){
                end = pos + limit;
            }
            for(int i = lineStart; i < end; i++){
                builder.append((char) (byteSource.read(i) & 0xff));
            }
            builder.append('\n');
            for(int i = lineStart; i < pos; i++){
                builder.append(' ');
            }
            builder.append('^');
            return builder.toString();
        }
    }
}
