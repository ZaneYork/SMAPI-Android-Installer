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
package com.reandroid.arsc.value;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;

class ResConfigBase extends FixedBlockContainer
        implements BlockLoad{

    private final IntegerItem configSize;
    private final ResConfigValueContainer mValuesContainer;

    
    public ResConfigBase(int size) {
        super(2);
        this.configSize = new IntegerItem(size);
        this.mValuesContainer = new ResConfigValueContainer(size - 4,
                configSize);
        addChild(0, configSize);
        addChild(1, mValuesContainer);
        this.configSize.setBlockLoad(this);
    }

    public int getConfigSize(){
        return this.configSize.get();
    }
    public boolean trimToSize(int size){
        int current = getConfigSize();
        if(current == size){
            return true;
        }
        if(!isValidSize(size)){
            return false;
        }
        if(current<size){
            setConfigSize(size);
            return true;
        }
        int offset = size - 4;
        int len = current - 4 - offset;
        byte[] bts = mValuesContainer.getByteArray(offset, len);
        if(!isNullBytes(bts)){
            return false;
        }
        setConfigSize(size);
        return true;
    }
    public void trimToMinimumSize(){
        int size = ByteArray.trimTrailZeros(mValuesContainer.getBytes()).length + 4;
        size = nearestSize(size);
        trimToSize(size);
    }
    public void setConfigSize(int size){
        if(!isValidSize(size)){
            throw new IllegalArgumentException("Invalid config size = " + size);
        }
        if(size % 4 != 0){
            size = size + (4 - size % 4);
        }
        this.configSize.set(size);
        size = size-4;
        mValuesContainer.setSize(size);
    }

    void resetValueBytes(){
        mValuesContainer.fill((byte) 0);
    }
    void setValueBytes(byte[] bytes){
        setConfigSize(bytes.length + 4);
        mValuesContainer.putByteArray(0, bytes);
    }
    byte[] getValueBytes(){
        return mValuesContainer.getBytes();
    }
    /////////////////////////////////////////

    public void setMcc(int  value){
        mValuesContainer.setShortValue(OFFSET_mcc, value);
    }
    public int getMcc(){
        return mValuesContainer.getShortValue(OFFSET_mcc);
    }
    public void setMnc(int value){
        mValuesContainer.setShortValue(OFFSET_mnc, value);
    }
    public int getMnc(){
        return mValuesContainer.getShortValue(OFFSET_mnc);
    }

    public byte[] getLanguageBytes(){
        if(getConfigSize() < SIZE_16){
            return new byte[2];
        }
        return mValuesContainer.getByteArrayValue(OFFSET_language, 2);
    }
    public void setLanguageBytes(byte[] bytes){
        mValuesContainer.setByteArrayValue(OFFSET_language, bytes, 2);
    }
    public byte[] getRegionBytes(){
        return mValuesContainer.getByteArrayValue(OFFSET_region, 2);
    }
    public void setRegionBytes(byte[] bytes){
        mValuesContainer.setByteArrayValue(OFFSET_region, bytes, 2);
    }
    public void setOrientation(int orientation){
        mValuesContainer.setByteValue(OFFSET_orientation, orientation);
    }
    public int getOrientationValue(){
        return mValuesContainer.getByteValue(OFFSET_orientation);
    }
    public void setTouchscreen(int touchscreen){
        mValuesContainer.setByteValue(OFFSET_touchscreen, touchscreen);
    }
    public int getTouchscreenValue(){
        return mValuesContainer.getByteValue(OFFSET_touchscreen);
    }
    public void setDensity(int density){
        mValuesContainer.setShortValue(OFFSET_density, density);
    }
    public int getDensityValue(){
        return mValuesContainer.getShortValue(OFFSET_density);
    }
    public void setKeyboard(int keyboard){
        mValuesContainer.setByteValue(OFFSET_keyboard, keyboard);
    }
    public int getKeyboardValue(){
        return mValuesContainer.getByteValue(OFFSET_keyboard);
    }
    public void setNavigation(int navigation){
        mValuesContainer.setByteValue(OFFSET_navigation, navigation);
    }
    public int getNavigationValue(){
        return mValuesContainer.getByteValue(OFFSET_navigation);
    }
    public void setInputFlags(int inputFlags){
        mValuesContainer.setByteValue(OFFSET_inputFlags, inputFlags);
    }
    public int getInputFlagsValue(){
        return mValuesContainer.getByteValue(OFFSET_inputFlags);
    }
    public void setGenderValue(int value){
        mValuesContainer.setByteValue(OFFSET_gender, value);
    }
    public int getGenderValue(){
        return mValuesContainer.getByteValue(OFFSET_gender);
    }
    public void setScreenWidth(int value){
        mValuesContainer.setShortValue(OFFSET_screenWidth, value);
    }
    public int getScreenWidth(){
        return mValuesContainer.getShortValue(OFFSET_screenWidth);
    }
    public void setScreenHeight(int value){
        mValuesContainer.setShortValue(OFFSET_screenHeight, value);
    }
    public int getScreenHeight(){
        return mValuesContainer.getShortValue(OFFSET_screenHeight);
    }
    public void setSdkVersion(int value){
        mValuesContainer.setShortValue(OFFSET_sdkVersion, value);
    }
    public int getSdkVersion(){
        return mValuesContainer.getShortValue(OFFSET_sdkVersion);
    }
    public void setMinorVersion(int value){
        mValuesContainer.setShortValue(OFFSET_minorVersion, value);
    }
    public int getMinorVersion(){
        return mValuesContainer.getShortValue(OFFSET_minorVersion);
    }
    public void setScreenLayout(int value){
        mValuesContainer.setByteValue(OFFSET_screenLayout, value);
    }
    public int getScreenLayout(){
        return mValuesContainer.getByteValue(OFFSET_screenLayout);
    }
    public void setUiMode(int mode){
        mValuesContainer.setByteValue(OFFSET_uiMode, mode);
    }
    public int getUiMode(){
        return mValuesContainer.getByteValue(OFFSET_uiMode);
    }
    public void setSmallestScreenWidthDp(int value){
        mValuesContainer.setShortValue(OFFSET_smallestScreenWidthDp, value);
    }
    public int getSmallestScreenWidthDp(){
        return mValuesContainer.getShortValue(OFFSET_smallestScreenWidthDp);
    }
    public void setScreenWidthDp(int value){
        mValuesContainer.setShortValue(OFFSET_screenWidthDp, value);
    }
    public int getScreenWidthDp(){
        return mValuesContainer.getShortValue(OFFSET_screenWidthDp);
    }
    public void setScreenHeightDp(int value){
        mValuesContainer.setShortValue(OFFSET_screenHeightDp, value);
    }
    public int getScreenHeightDp(){
        return mValuesContainer.getShortValue(OFFSET_screenHeightDp);
    }
    public void setLocaleScript(byte[] bts){
        mValuesContainer.setByteArrayValue(OFFSET_localeScript, bts, LEN_localeScript);
    }
    public byte[] getLocaleScriptBytes(){
        return mValuesContainer.getByteArrayValue(OFFSET_localeScript, LEN_localeScript);
    }
    public void setLocaleVariant(byte[] bts){
        mValuesContainer.setByteArrayValue(OFFSET_localeVariant, bts, LEN_localeVariant);
    }
    public byte[] getLocaleVariantBytes(){
        return mValuesContainer.getByteArrayValue(OFFSET_localeVariant, LEN_localeVariant);
    }
    public void setScreenLayout2(int screenLayout2){
        mValuesContainer.setByteValue(OFFSET_screenLayout2, screenLayout2);
    }
    public int getScreenLayout2(){
        return mValuesContainer.getByteValue(OFFSET_screenLayout2);
    }
    public void setColorMode(int colorMode){
        mValuesContainer.setByteValue(OFFSET_colorMode, colorMode);
    }
    public int getColorMode(){
        return mValuesContainer.getByteValue(OFFSET_colorMode);
    }
    public void setReservedColorModePadding(int value){
        mValuesContainer.setByteValue(OFFSET_reservedColorModePadding,
                value);
    }
    public int getReservedColorModePadding(){
        return mValuesContainer.getByteValue(OFFSET_reservedColorModePadding);
    }
    public void setUnknownBytes(byte[] bytes){
        int length = getConfigSize() - 4 - OFFSET_unknown;
        if(bytes.length > length){
            length = bytes.length;
        }
        mValuesContainer.setByteArrayValue(OFFSET_unknown, bytes,
                length);
    }
    public byte[] getUnknownBytes(){
        return mValuesContainer.getByteArrayValue(OFFSET_unknown,
                getConfigSize() - 4 - OFFSET_unknown);
    }




    @Override
    protected void onPreRefresh(){
        int count = countBytes();
        configSize.set(count);
    }
    @Override
    protected void onRefreshed() {
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender == configSize){
            mValuesContainer.setSize(configSize.get() - 4);
        }
    }
    int compareLocale(ResConfigBase config) {
        int offset = OFFSET_region;
        int i = CompareUtil.compare(
                mValuesContainer.getShortValue(offset),
                config.mValuesContainer.getShortValue(offset));
        if(i == 0) {
            offset = OFFSET_language;
            i = CompareUtil.compare(
                    mValuesContainer.getShortValue(offset),
                    config.mValuesContainer.getShortValue(offset));
        }
        if(i == 0) {
            offset = OFFSET_localeScript;
            i = CompareUtil.compare(
                    mValuesContainer.getIntValue(offset),
                    config.mValuesContainer.getIntValue(offset));
        }
        if(i == 0) {
            offset = OFFSET_localeVariant;
            i = CompareUtil.compare(
                    mValuesContainer.getLongValue(offset),
                    config.mValuesContainer.getLongValue(offset));
        }
        return i;
    }
    public static int nearestSize(int size){
        if(size <= SIZE_16){
            return SIZE_16;
        }
        if(size <= SIZE_28){
            return SIZE_28;
        }
        if(size <= SIZE_32){
            return SIZE_32;
        }
        if(size <= SIZE_36){
            return SIZE_36;
        }
        if(size <= SIZE_48){
            return SIZE_48;
        }
        if(size <= SIZE_52){
            return SIZE_52;
        }
        if(size <= SIZE_56){
            return SIZE_56;
        }
        if(size <= SIZE_64){
            return SIZE_64;
        }
        return size +  ((4 - (size % 4)) % size);
    }
    public static boolean isValidSize(int size){
        switch (size){
            case SIZE_16:
            case SIZE_28:
            case SIZE_32:
            case SIZE_36:
            case SIZE_48:
            case SIZE_52:
            case SIZE_56:
            case SIZE_64:
                return true;
            default:
                return size > SIZE_64;
        }
    }


    static byte[] toByteArray(char[] chs, int len){
        byte[] bts = new byte[len];
        if(chs == null){
            return bts;
        }
        int sz = chs.length;
        for(int i = 0; i < sz; i++){
            bts[i]= (byte) chs[i];
        }
        return bts;
    }
    static char[] toCharArray(byte[] bts){
        if(isNullBytes(bts)){
            return null;
        }
        int sz = bts.length;
        char[] chs = new char[sz];
        for(int i = 0; i < sz; i++){
            int val = 0xff & bts[i];
            chs[i]= (char) val;
        }
        return chs;
    }
    static char[] trimEndingZero(char[] chars){
        if(chars == null){
            return null;
        }
        int lastNonZero = -1;
        for(int i = 0; i < chars.length; i++){
            if(chars[i]!= 0){
                lastNonZero = i;
            }
        }
        if(lastNonZero==-1){
            return null;
        }
        lastNonZero = lastNonZero+1;
        if(lastNonZero== chars.length){
            return chars;
        }
        char[] result = new char[lastNonZero];
        System.arraycopy(chars, 0, result, 0, lastNonZero);
        return result;
    }
    static byte[] trimEndingZero(byte[] bytes){
        if(bytes == null){
            return null;
        }
        int lastNonZero = -1;
        for(int i = 0; i < bytes.length; i++){
            if(bytes[i]!= 0){
                lastNonZero = i;
            }
        }
        if(lastNonZero==-1){
            return null;
        }
        lastNonZero = lastNonZero+1;
        if(lastNonZero== bytes.length){
            return bytes;
        }
        byte[] result = new byte[lastNonZero];
        System.arraycopy(bytes, 0, result, 0, lastNonZero);
        return result;
    }
    static boolean isNullChars(char[] chars){
        if(chars == null){
            return true;
        }
        for(int i = 0; i < chars.length; i++){
            if(chars[i] != 0){
                return false;
            }
        }
        return true;
    }
    static boolean isNullBytes(byte[] bytes){
        if(bytes == null){
            return true;
        }
        for(int i = 0; i < bytes.length; i++){
            if(bytes[i] != 0){
                return false;
            }
        }
        return true;
    }
    static String ensureLength(String str, int min, char postfix){
        int length = str.length();
        if(length >= min){
            return str;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(str);
        int remain = min - length;
        for(int i = 0; i < remain; i++){
            builder.append(postfix);
        }
        return builder.toString();
    }
    static String trimPostfix(String str, char postfix){
        if(str == null){
            return null;
        }
        int length = str.length();
        int index = length-1;
        while (length>0 && str.charAt(index) == postfix){
            str = str.substring(0, index);
            length = str.length();
            index = length - 1;
        }
        return str;
    }

    public static final int SIZE_16 = 16;
    public static final int SIZE_28 = 28;
    public static final int SIZE_32 = 32;
    public static final int SIZE_36 = 36;
    public static final int SIZE_48 = 48;
    public static final int SIZE_52 = 52;
    public static final int SIZE_56 = 56;
    public static final int SIZE_64 = 64;

    private static final int OFFSET_mcc = 0;
    private static final int OFFSET_mnc = 2;
    private static final int OFFSET_language = 4;
    private static final int OFFSET_region = 6;
    private static final int OFFSET_orientation = 8;
    private static final int OFFSET_touchscreen = 9;
    private static final int OFFSET_density = 10;
    //SIZE=16
    private static final int OFFSET_keyboard = 12;
    private static final int OFFSET_navigation = 13;
    private static final int OFFSET_inputFlags = 14;
    private static final int OFFSET_gender = 15;
    private static final int OFFSET_screenWidth = 16;
    private static final int OFFSET_screenHeight = 18;
    private static final int OFFSET_sdkVersion = 20;
    private static final int OFFSET_minorVersion = 22;
    //SIZE=28
    private static final int OFFSET_screenLayout = 24;
    private static final int OFFSET_uiMode = 25;
    private static final int OFFSET_smallestScreenWidthDp = 26;
    //SIZE=32
    private static final int OFFSET_screenWidthDp = 28;
    private static final int OFFSET_screenHeightDp = 30;
    //SIZE=36
    private static final int OFFSET_localeScript = 32;
    private static final int OFFSET_localeVariant = 36;
    //SIZE=48
    private static final int OFFSET_screenLayout2 = 44;
    private static final int OFFSET_colorMode = 45;
    private static final int OFFSET_reservedColorModePadding = 46;
    //SIZE=52
    private static final int OFFSET_unknown = 48;
    //SIZE=60

    static final int LEN_localeScript = 4;
    static final int LEN_localeVariant = 8;

    public static final String NAME_mcc = "mcc";
    public static final String NAME_mnc = "mnc";
    public static final String NAME_language = "language";
    public static final String NAME_region = "region";
    public static final String NAME_orientation = "orientation";
    public static final String NAME_touchscreen = "touchscreen";
    public static final String NAME_density = "density";
    //SIZE=16
    public static final String NAME_keyboard = "keyboard";
    public static final String NAME_navigation = "navigation";
    public static final String NAME_input_flags_keys_hidden = "input_flags_keys_hidden";
    public static final String NAME_input_flags_nav_hidden = "input_flags_nav_hidden";
    public static final String NAME_gender = "gender";
    public static final String NAME_screenWidth = "screenWidth";
    public static final String NAME_screenHeight = "screenHeight";
    public static final String NAME_sdkVersion = "sdkVersion";
    public static final String NAME_minorVersion = "minorVersion";
    //SIZE=28
    public static final String NAME_screen_layout_size = "screen_layout_size";
    public static final String NAME_screen_layout_long = "screen_layout_long";
    public static final String NAME_screen_layout_dir = "screen_layout_dir";
    public static final String NAME_ui_mode_type = "ui_mode_type";
    public static final String NAME_ui_mode_night = "ui_mode_night";
    public static final String NAME_smallestScreenWidthDp = "smallestScreenWidthDp";
    //SIZE=32 = "";
    public static final String NAME_screenWidthDp = "screenWidthDp";
    public static final String NAME_screenHeightDp = "screenHeightDp";
    //SIZE=36
    public static final String NAME_localeScript = "localeScript";
    public static final String NAME_localeVariant = "localeVariant";
    public static final String NAME_screen_layout_round = "screen_layout_round";
    public static final String NAME_color_mode_wide = "color_mode_wide";
    public static final String NAME_color_mode_hdr = "color_mode_hdr";

    public static final String NAME_unknownBytes = "unknown_bytes";

    public static final String UNKNOWN_BYTES = "unknown_bytes";

    public static final String NAME_config_size = "config_size";
}
