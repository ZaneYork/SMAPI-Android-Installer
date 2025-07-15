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

import com.reandroid.arsc.item.ByteArray;

import java.util.Objects;

public class BlockLocator extends BlockCounter{
    private final int bytePosition;
    private Block current;
    private Block previous;

    public BlockLocator(int bytePosition) {
        super(null);
        this.bytePosition = bytePosition;
    }
    @Override
    void onCountAdded(int count){
        if(count > bytePosition){
            FOUND = true;
        }
    }
    @Override
    public void setCurrent(Block current){
        if(current == null || FOUND){
            return;
        }
        this.previous = this.current;
        this.current = current;
        if(getCountValue() > bytePosition){
            FOUND = true;
            return;
        }
    }

    public int getBytePosition() {
        return bytePosition;
    }
    public Result getResult(){
        if(!FOUND || current == null){
            return null;
        }
        return new Result(getBytePosition(),
                current,
                buildParentTraces(current, previous));
    }
    @Override
    public String toString(){
        Result result = getResult();
        if(result == null){
            return getCountValue() + "/" + bytePosition;
        }
        return result.toString();
    }
    private static Block[] buildParentTraces(Block block, Block previous){
        Block[] tmp = new Block[PARENT_TRACE_LENGTH];
        Block parent = block.getParent();
        if(parent == null){
            parent = previous;
        }
        int count = 0;
        while (parent != null && count < tmp.length){
            tmp[count] = parent;
            parent = parent.getParent();
            count++;
        }
        Block[] results = new Block[count];
        for(int i = 0; i < count; i++){
            results[i] = tmp[i];
        }
        return results;
    }
    public static class Result{

        public final int position;
        public final Block block;
        public final Block[] parentTraces;

        Result(int position, Block block, Block[] parentTraces){
            this.position = position;
            this.block = block;
            this.parentTraces = parentTraces;
        }
        public String getMessage() {
            return "Position=" + position + ", " + toResultName(block);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (block == null || obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Result result = (Result) obj;
            if(result.block == null){
                return false;
            }
            return ByteArray.equals(block.getBytes(), result.block.getBytes());
        }

        @Override
        public int hashCode() {
            return Objects.hash(block);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(getMessage());
            for(Block parent : parentTraces){
                builder.append("\n  ");
                builder.append(toResultName(parent));
            }
            return builder.toString();
        }
    }
    static String toResultName(Block block){
        if(block == null){
            return "null";
        }
        String name = block.getClass().getSimpleName();
        String text = block.toString();
        if(text == null){
            text = "null";
        }
        if(!text.startsWith(name)){
            text = name + " " + text;
        }
        return text;
    }

    private static final int PARENT_TRACE_LENGTH = 10;
}
