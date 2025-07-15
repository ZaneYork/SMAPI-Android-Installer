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
package com.reandroid.dex.base;

public class DexException extends RuntimeException {

    public DexException(String message){
        super(message);
    }
    public DexException(String message, Throwable cause){
        super(message, cause);
    }
    public DexException(Throwable cause){
        super(cause);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        append(this, builder);
        return builder.toString();
    }

    private static void append(Throwable throwable, StringBuilder builder){
        if(throwable == null){
            return;
        }
        builder.append(throwable.getClass().getName());
        builder.append(": ");
        builder.append(throwable.getMessage());
        StackTraceElement[] traceElements = throwable.getStackTrace();
        int length = traceElements.length;
        if(length > MAX_TRACE){
            length = MAX_TRACE;
        }
        for(int i = 0; i < length; i++){
            append(traceElements[i], builder);
        }
        Throwable cause = throwable.getCause();
        if(cause == null || cause == throwable){
            return;
        }
        builder.append("Caused by: ");
        append(cause, builder);
    }
    private static void append(StackTraceElement element, StringBuilder builder){
        if(element == null){
            return;
        }
        builder.append("\n\tat ");
        builder.append(element.getClassName());
        builder.append('.');
        builder.append(element.getMethodName());
        builder.append('(');
        builder.append(element.getFileName());
        builder.append(':');
        builder.append(element.getLineNumber());
        builder.append(')');
    }
    private static final int MAX_TRACE = 15;
}
