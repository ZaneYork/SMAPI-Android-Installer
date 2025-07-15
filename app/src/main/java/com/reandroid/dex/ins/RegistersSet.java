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
package com.reandroid.dex.ins;


import com.reandroid.dex.common.RegisterFormat;

public interface RegistersSet {
    int getRegistersCount();
    void setRegistersCount(int count);

    int getRegister(int index);
    void setRegister(int index, int value);

    int getRegisterLimit(int index);
    default int getRegister() {
        return getRegister(0);
    }
    default void setRegister(int register){
        setRegister(0, register);
    }
    default RegisterFormat getRegisterFormat(){
        return null;
    }
    default boolean removeRegisterAt(int index){
        if(index < 0) {
            return false;
        }
        int count = getRegistersCount();
        if(index >= count) {
            return false;
        }
        int last = count - 1;
        if(getRegisterFormat().isRange()) {
            if(index == 0) {
                setRegister(getRegister() + 1);
                return true;
            }
            if(index == last) {
                setRegistersCount(last);
                return true;
            }
            // Can not remove from middle of registers range
            return false;
        }
        for(int i = index; i < last; i ++) {
            setRegister(i, getRegister(i + 1));
        }
        setRegister(last, 0);
        setRegistersCount(last);
        return true;
    }
    default boolean isWideRegisterAt(int index){
        return false;
    }
}
