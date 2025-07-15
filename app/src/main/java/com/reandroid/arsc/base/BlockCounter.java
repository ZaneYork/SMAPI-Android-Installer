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
package com.reandroid.arsc.base;

public class BlockCounter {
    public final Block END;
    public boolean FOUND;
    private int COUNT;
    public BlockCounter(Block end){
        this.END=end;
    }
    public void addCount(int val){
        if(FOUND || val == 0){
            return;
        }
        COUNT += val;
        onCountAdded(COUNT);
    }
    void onCountAdded(int count){
    }
    public int getCountValue() {
        return COUNT;
    }

    public void setCurrent(Block current){
    }
    @Override
    public String toString(){
        if(FOUND){
            return "FOUND="+COUNT;
        }
        return String.valueOf(COUNT);
    }
}
