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
package com.reandroid.dex.common;

public class RegisterFormat {

    public static final RegisterFormat NONE;
    public static final RegisterFormat READ;
    public static final RegisterFormat WRITE;
    public static final RegisterFormat READ_READ;
    public static final RegisterFormat READ_WRITE;
    public static final RegisterFormat READ_READ_READ;
    public static final RegisterFormat WRITE_READ;
    public static final RegisterFormat WRITE_READ_READ;
    public static final RegisterFormat OUT;
    public static final RegisterFormat OUT_RANGE;

    static {

        NONE = new RegisterFormat("NONE", new RegisterType[0]);
        READ = new RegisterFormat("READ", new RegisterType[]{RegisterType.READ});
        WRITE = new RegisterFormat("WRITE", new RegisterType[]{RegisterType.WRITE});
        READ_READ = new RegisterFormat("READ_READ", new RegisterType[]{RegisterType.READ, RegisterType.READ});
        READ_WRITE = new RegisterFormat("READ_WRITE", new RegisterType[]{RegisterType.READ, RegisterType.WRITE});
        READ_READ_READ = new RegisterFormat("READ_READ_READ", new RegisterType[]{RegisterType.READ, RegisterType.READ, RegisterType.READ});
        WRITE_READ = new RegisterFormat("WRITE_READ", new RegisterType[]{RegisterType.WRITE, RegisterType.READ});
        WRITE_READ_READ = new RegisterFormat("WRITE_READ_READ", new RegisterType[]{RegisterType.WRITE, RegisterType.READ, RegisterType.READ});

        OUT = new RegisterFormat("OUT", new RegisterType[0]){
            @Override
            public RegisterType get(int i) {
                return RegisterType.READ;
            }
            @Override
            public boolean isOut() {
                return true;
            }
        };
        OUT_RANGE = new RegisterFormat("OUT_RANGE", new RegisterType[]{RegisterType.READ, RegisterType.READ}){
            @Override
            public boolean isOut() {
                return true;
            }
            @Override
            public boolean isRange() {
                return true;
            }
        };
    }

    private final String name;
    private final RegisterType[] types;
    private final int hash;

    private RegisterFormat(String name, RegisterType[] types){
        this.name = name;
        this.types = types;
        int h = 1;
        if(types != null){
            h = h + 31 * types.length;
        }
        h = h + 31 * name.hashCode();
        this.hash = h;
    }

    public RegisterType get(int i){
        if(isOut()){
            return RegisterType.READ;
        }
        return types[i];
    }
    public int size(){
        RegisterType[] types = this.types;
        if(types != null){
            return types.length;
        }
        return 0;
    }
    public boolean isOut(){
        return false;
    }
    public boolean isRange(){
        return false;
    }
    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return name;
    }
}
