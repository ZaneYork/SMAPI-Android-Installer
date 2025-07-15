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
package com.reandroid.graphics;

import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class AndroidColor {

    private int alpha;
    private int red;
    private int green;
    private int blue;

    private Type type;

    private boolean upperCase;

    public AndroidColor(int alpha, int red, int green, int blue){
        this.alpha = alpha;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public AndroidColor(){
        this(0, 0, 0, 0);
    }

    public void setUpperCase(boolean upperCase) {
        this.upperCase = upperCase;
    }
    public AndroidColor copy(){
        AndroidColor color = new AndroidColor(this.alpha, this.red, this.green, this.blue);
        color.setType(getType());
        color.setUpperCase(upperCase);
        return color;
    }
    public AndroidColor inverse(){
        AndroidColor color = copy();
        int mask = getType().mask();
        color.red(~color.red() & mask);
        color.green(~color.green() & mask);
        color.blue(~color.blue() & mask);
        return color;
    }
    public AndroidColor argb(){
        AndroidColor color = new AndroidColor(0, this.red, this.green, this.blue);
        Type type = getType();
        if(type.isEightBit()){
            type = Type.ARGB8;
        }else {
            type = Type.ARGB4;
        }
        color.setType(type);
        color.setUpperCase(upperCase);
        color.alpha(this.alpha());
        return color;
    }
    public AndroidColor rgb(){
        AndroidColor color = new AndroidColor(0, this.red, this.green, this.blue);
        Type type = getType();
        if(type.isEightBit()){
            type = Type.RGB8;
        }else {
            type = Type.RGB4;
        }
        color.setType(type);
        color.setUpperCase(upperCase);
        return color;
    }
    public AndroidColor toEightBit(){
        if(isEightBit()){
            return this;
        }
        AndroidColor color = copy();
        color.setEightBit(true);
        return color;
    }
    public AndroidColor toFourBit(){
        if(!isEightBit()){
            return this;
        }
        AndroidColor color = copy();
        color.setEightBit(false);
        return color;
    }
    public boolean hasAlpha(){
        return getType().hasAlpha();
    }
    public void setHasAlpha(boolean hasAlpha){
        Type type = getType();
        if(hasAlpha != type.hasAlpha()){
            if(type == Type.ARGB4){
                type = Type.RGB4;
            }else if(type == Type.RGB4){
                type = Type.ARGB4;
            }else if(type == Type.ARGB8){
                type = Type.RGB8;
            }else if(type == Type.RGB8){
                type = Type.ARGB8;
            }
            int alpha = 0;
            if(hasAlpha){
                alpha = type.fit(alpha());
            }
            this.setType(type);
            alpha(alpha);
        }
    }

    public int alpha() {
        return this.alpha;
    }
    public void alpha(int alpha) {
        if(this.alpha != alpha || alpha == 0){
            this.alpha = getType().fit(alpha);
        }
    }
    public int red() {
        return this.red;
    }
    public void red(int red) {
        this.red = getType().fit(red);
    }
    public int green() {
        return this.green;
    }
    public void green(int green) {
        this.green = getType().fit(green);
    }
    public int blue() {
        return this.blue;
    }
    public void blue(int blue) {
        this.blue = getType().fit(blue);
    }

    public AndroidColor addRgb(int amount){
        if(amount == 0){
            return this;
        }
        AndroidColor color = copy();
        int max = 0xf;
        if(color.isEightBit()){
            max = 0xff;
        }
        int i = color.red + amount;
        if(i < 0 || i > max){
            return this;
        }
        color.red = i;
        i = color.green + amount;
        if(i < 0 || i > max){
            return this;
        }
        color.green = i;
        i = color.blue + amount;
        if(i < 0 || i > max){
            return this;
        }
        color.blue = i;
        return color;
    }
    public boolean isEightBit(){
        return getType().isEightBit();
    }
    public void setEightBit(boolean eightBit){
        Type type = this.getType();
        if(eightBit == type.isEightBit()){
            return;
        }
        int max;
        int scale;
        if(eightBit){
            max = 0xff;
            scale = 0xf;
            type = type.toEightBit();
        }else {
            max = 0xf;
            scale = 0xff;
            type = type.toFourBit();
        }
        this.alpha = (this.alpha * max) / scale;
        this.red = (this.red * max) / scale;
        this.green = (this.green * max) / scale;
        this.blue = (this.blue * max) / scale;
        this.type = type;
    }
    public Type getType() {
        Type type = this.type;
        if(type == null){
            type = Type.getFor(alpha(), red(), green(), blue());
        }
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }

    public String toHexString(){
        Type type = getType();
        StringBuilder builder = new StringBuilder();
        builder.append('#');
        int width = 1;
        if(type.isEightBit()){
            width = 2;
        }
        if(type.hasAlpha()){
            builder.append(HexUtil.toHex(null, alpha, width));
        }
        builder.append(HexUtil.toHex(null, red, width));
        builder.append(HexUtil.toHex(null, green, width));
        builder.append(HexUtil.toHex(null, blue, width));
        String hex = builder.toString();
        if(upperCase) {
            hex = StringsUtil.toUpperCase(hex);
        }
        return hex;
    }

    public int intValue() {
        int value = alpha << 24;
        value |= red << 16;
        value |= green << 8;
        value |= blue;
        return value;
    }

    public int compareTo(AndroidColor reference, AndroidColor color) {
        return Double.compare(reference.distance(this), reference.distance(color));
    }

    public double distance(AndroidColor other) {
        if(other == this){
            return 0;
        }
        AndroidColor color = this;
        if(color.isEightBit() != other.isEightBit()){
            if(color.isEightBit()){
                other = other.toEightBit();
            }else {
                color = color.toEightBit();
            }
        }
        double d = deltaSquare(other.red(), color.red());
        d += deltaSquare(other.green(), color.green());
        d += deltaSquare(other.blue(), color.blue());
        d = Math.sqrt(d);
        d = d * 100.0;
        long l = Math.round(d);
        d = l / 100.0;
        return d;
    }
    public boolean equalsRgb(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AndroidColor)) {
            return false;
        }
        AndroidColor other = (AndroidColor) obj;
        AndroidColor color = this;
        if(color.isEightBit() != other.isEightBit()){
            if(color.isEightBit()){
                other = other.toEightBit();
            }else {
                color = color.toEightBit();
            }
        }
        return color.red == other.red &&
                color.green == other.green &&
                color.blue == other.blue;
    }

    public boolean equals(AndroidColor color, float percent) {
        if (color == null) {
            return false;
        }
        if (color == this){
            return true;
        }
        return distance(color) <= toDistance(percent);
    }
    public boolean equals(AndroidColor color, double distanceTolerance) {
        if (color == null) {
            return false;
        }
        if (color == this){
            return true;
        }
        return distance(color) <= distanceTolerance;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AndroidColor)) {
            return false;
        }
        AndroidColor other = (AndroidColor) obj;
        AndroidColor color = this;
        if(color.isEightBit() != other.isEightBit()){
            if(color.isEightBit()){
                other = other.toEightBit();
            }else {
                color = color.toEightBit();
            }
        }
        return color.alpha == other.alpha &&
                color.red == other.red &&
                color.green == other.green &&
                color.blue == other.blue;
    }

    @Override
    public int hashCode() {
        return intValue();
    }
    @Override
    public String toString() {
        return toHexString();
    }
    public static ColorIterator decodeAll(String text) {
        return new ColorIterator(text);
    }
    public static AndroidColor decode(String text) {
        if(text == null){
            return null;
        }
        int length = text.length();
        if(length < 4){
            return null;
        }
        if(text.charAt(0) != '#'){
            return null;
        }
        StringBuilder builder = new StringBuilder(9);
        builder.append('#');
        for(int i = 1; i < length; i++){
            char ch = text.charAt(i);
            if(!HexUtil.isHexChar(ch)){
                break;
            }
            if(i > 8){
                return null;
            }
            builder.append(ch);
        }
        return parseHex(builder.toString(), false);
    }
    public static AndroidColor parseHex(String hexColor) {
        return parseHex(hexColor, true);
    }
    private static AndroidColor parseHex(String hexColor, boolean fail) {
        if(hexColor == null ||
                hexColor.length() == 0 ||
                hexColor.charAt(0) != '#'){
            if(fail){
                throw new NumberFormatException("Invalid hex color: " + hexColor);
            }
            return null;
        }
        Type type = Type.valueOf(hexColor.length());
        if(type == null){
            if(fail){
                throw new NumberFormatException("Invalid hex color string length: " + hexColor);
            }
            return null;
        }
        String current = hexColor;
        AndroidColor color = new AndroidColor();
        color.type = type;
        current = current.substring(1);
        color.setUpperCase(StringsUtil.containsUpperAZ(current));
        int width = 1;
        if(type.isEightBit()){
            width = 2;
        }
        int i;
        int error = -1;
        if(type.hasAlpha()){
            i = HexUtil.decodeHex(current.substring(0, width), error);
            if(i == error){
                if(fail){
                    throw new NumberFormatException("Invalid hex: " + hexColor);
                }
                return null;
            }
            color.alpha = i;
            current = current.substring(width);
        }
        i = HexUtil.decodeHex(current.substring(0, width), error);
        if(i == error){
            if(fail){
                throw new NumberFormatException("Invalid hex: " + hexColor);
            }
            return null;
        }
        color.red = i;
        current = current.substring(width);
        i = HexUtil.decodeHex(current.substring(0, width), error);
        if(i == error){
            if(fail){
                throw new NumberFormatException("Invalid hex: " + hexColor);
            }
            return null;
        }
        color.green = i;
        current = current.substring(width);
        i = HexUtil.decodeHex(current.substring(0, width), error);
        if(i == error){
            if(fail){
                throw new NumberFormatException("Invalid hex: " + current);
            }
            return null;
        }
        color.blue = i;
        return color;
    }

    private static int deltaSquare(int i1, int i2) {
        int i = i1 - i2;
        return i * i;
    }
    public static float toPercent(double distance){
        return (float) ((distance * 100.0) / LENGTH);
    }
    public static double toDistance(float percent){
        return (percent * LENGTH) / 100.0;
    }

    public static class Type {

        public static final Type RGB4 = new Type("RGB4", false, false);
        public static final Type ARGB4 = new Type("ARGB4", true, false);
        public static final Type RGB8 = new Type("RGB8", false, true);
        public static final Type ARGB8 = new Type("ARGB8", true, true);

        private final String name;
        private final boolean hasAlpha;
        private final boolean eightBit;

        private Type(String name, boolean hasAlpha, boolean eightBit){
            this.name = name;
            this.hasAlpha = hasAlpha;
            this.eightBit = eightBit;
        }

        public boolean fits(int alpha, int red, int green, int blue){
            if(alpha != 0 && !hasAlpha){
                return false;
            }
            int mask = mask();
            return (alpha & mask) == alpha &&
                    (red & mask) == red &&
                    (red & mask) == green &&
                    (red & mask) == blue;
        }
        public int fit(int value){
            int mask = mask();
            if(value > mask) {
                return mask;
            }
            if(value < 0){
                return 0;
            }
            return value;
        }
        public int mask(){
            if(eightBit){
                return 0xff;
            }
            return 0xf;
        }
        public boolean hasAlpha() {
            return hasAlpha;
        }
        public boolean isEightBit() {
            return eightBit;
        }
        public Type toEightBit(){
            if(isEightBit()){
                return this;
            }
            if(this == RGB4){
                return RGB8;
            }
            return ARGB8;
        }
        public Type toFourBit(){
            if(!isEightBit()){
                return this;
            }
            if(this == RGB8){
                return RGB4;
            }
            return ARGB4;
        }

        @Override
        public boolean equals(Object o) {
            return o == this;
        }
        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }

        public static Type getFor(int alpha, int red, int green, int blue){
            if(RGB4.fits(alpha, red, green, blue)){
                return RGB4;
            }
            if(ARGB4.fits(alpha, red, green, blue)){
                return ARGB4;
            }
            if(RGB8.fits(alpha, red, green, blue)){
                return RGB8;
            }
            return ARGB8;
        }
        public static Type valueOf(int hexStringLength){
            if(hexStringLength == 4){
                return RGB4;
            }
            if(hexStringLength == 5){
                return ARGB4;
            }
            if(hexStringLength == 7){
                return RGB8;
            }
            if(hexStringLength == 9){
                return ARGB8;
            }
            return null;
        }
    }

    public static class ColorIterator implements Iterator<AndroidColor> {

        private String text;
        private AndroidColor current;
        private String lastColor;
        private int index;
        private int lastIndex;

        public ColorIterator(String text){
            this.text = text;
            this.lastIndex = -1;
        }

        public String getText() {
            return text;
        }
        public String replace(AndroidColor color) {
            String text = getText();
            if(color == null || lastColor == null){
                return text;
            }
            String left = text.substring(0, lastIndex);
            String right = text.substring(lastIndex + lastColor.length());
            lastColor = color.toHexString();
            text = left + lastColor + right;
            this.text = text;
            return text;
        }

        @Override
        public void remove() {
            String lastColor = this.lastColor;
            if(lastColor == null){
                return;
            }
            String text = getText();
            this.lastColor = null;
            String left = text.substring(0, lastIndex);
            String right = text.substring(lastIndex + lastColor.length());
            this.text = left + right;
        }

        @Override
        public boolean hasNext() {
            return getNext() != null;
        }

        @Override
        public AndroidColor next() {
            AndroidColor color = getNext();
            if(color == null){
                throw new NoSuchElementException();
            }
            lastColor = color.toHexString();
            current = null;
            return color;
        }
        private int nextIndex(){
            if(text == null){
                return -1;
            }
            int length = text.length();
            while (index < length){
                if(text.charAt(index) == '#'){
                    return index;
                }
                index ++;
            }
            return -1;
        }
        private AndroidColor getNext(){
            while (current == null && nextIndex() != -1){
                current = decode(text.substring(index));
                if(current != null){
                    lastIndex = index;
                }
                index ++;
            }
            return current;
        }
    }

    // the distance between "#000000" and "#ffffff";
    public static final double LENGTH = ObjectsUtil.of(441.67);
}
