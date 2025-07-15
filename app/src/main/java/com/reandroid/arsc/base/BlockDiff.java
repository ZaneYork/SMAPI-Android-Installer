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

import com.reandroid.utils.collection.ArrayCollection;

import java.util.List;
import java.util.Objects;

public class BlockDiff {
    private final Block block_a;
    private final Block block_b;
    public BlockDiff(Block block_a, Block block_b){
        this.block_a = block_a;
        this.block_b = block_b;
    }
    public DiffResult[] find(){
        return find(MAX_RESULTS);
    }
    public DiffResult[] find(int limit){
        byte[] bytes_a = block_a.getBytes();
        byte[] bytes_b = block_b.getBytes();
        List<DiffResult> results = new ArrayCollection<>();
        int start = 0;
        int position = findByteDifferencePosition(start, bytes_a, bytes_b);
        while (limit < 0 || results.size() < limit){
            if(position == NO_DIFFERENCE){
                break;
            }
            int location = position;
            if(position == LENGTH_DIFFERENCE){
                location = start;
            }
            BlockLocator.Result result_a = block_a.locateBlock(location);
            BlockLocator.Result result_b = block_b.locateBlock(location);

            if(Objects.equals(result_a, result_b)){
                break;
            }
            DiffResult diffResult = new DiffResult(result_a, result_b);
            results.add(diffResult);
            if(result_a == null || result_b == null){
                break;
            }
            start = location + result_a.block.countBytes() + 1;
            position = findByteDifferencePosition(start, bytes_a, bytes_b);
        }
        return results.toArray(new DiffResult[0]);
    }

    private int findByteDifferencePosition(int start, byte[] bytes1, byte[] bytes2){
        int length = bytes1.length;
        if(length > bytes2.length){
            length = bytes2.length;
        }
        for(int i = start; i < length; i++){
            if(bytes1[i] != bytes2[i]){
                return i;
            }
        }
        if(bytes1.length != bytes2.length){
            return LENGTH_DIFFERENCE;
        }
        return NO_DIFFERENCE;
    }
    public static String toString(DiffResult[] results){
        if(results == null || results.length == 0){
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < results.length; i++){
            if(i != 0){
                builder.append("\n");
            }
            builder.append(results[i]);
        }
        return builder.toString();
    }
    public static class DiffResult {
        public final BlockLocator.Result BLOCK_A;
        public final BlockLocator.Result BLOCK_B;

        DiffResult(BlockLocator.Result block_a, BlockLocator.Result block_b){
            BLOCK_A = block_a;
            BLOCK_B = block_b;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("BLOCK_A {");
            if(BLOCK_A != null){
                builder.append(BLOCK_A.getMessage());
            }else {
                builder.append("null");
            }
            builder.append("}, BLOCK_B {");
            if(BLOCK_B != null){
                builder.append(BLOCK_B.getMessage());
            }else {
                builder.append("null");
            }
            builder.append("}");
            return builder.toString();
        }
    }
    private static final int NO_DIFFERENCE = -1;
    private static final int LENGTH_DIFFERENCE = -2;

    private static final int MAX_RESULTS = 10;
}
