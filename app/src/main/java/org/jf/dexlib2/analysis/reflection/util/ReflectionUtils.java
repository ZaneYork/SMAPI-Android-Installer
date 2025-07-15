/*
 * Copyright 2013, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.dexlib2.analysis.reflection.util;


public class ReflectionUtils {

    private static String getPrimitive(String java){
        if(java == null){
            return null;
        }
        if(java.equals("boolean")){
            return "Z";
        }
        if(java.equals("int")){
            return "I";
        }
        if(java.equals("long")){
            return "J";
        }
        if(java.equals("double")){
            return "D";
        }
        if(java.equals("void")){
            return "V";
        }
        if(java.equals("float")){
            return "F";
        }
        if(java.equals("char")){
            return "C";
        }
        if(java.equals("short")){
            return "S";
        }
        if(java.equals("byte")){
            return "B";
        }
        return null;
    }
    private static String getJava(String primitive){
        if(primitive == null){
            return null;
        }
        if(primitive.equals("Z")){
            return "boolean";
        }
        if(primitive.equals("I")){
            return "int";
        }
        if(primitive.equals("J")){
            return "long";
        }
        if(primitive.equals("D")){
            return "double";
        }
        if(primitive.equals("V")){
            return "void";
        }
        if(primitive.equals("F")){
            return "float";
        }
        if(primitive.equals("C")){
            return "char";
        }
        if(primitive.equals("S")){
            return "short";
        }
        if(primitive.equals("B")){
            return "byte";
        }
        return null;
    }


    public static String javaToDexName(String javaName) {
        if (javaName.charAt(0) == '[') {
            return javaName.replace('.', '/');
        }

        String primitive = getPrimitive(javaName);
        if (primitive != null) {
            return primitive;
        }

        return 'L' + javaName.replace('.', '/') + ';';
    }

    public static String dexToJavaName(String dexName) {
        if (dexName.charAt(0) == '[') {
            return dexName.replace('/', '.');
        }
        String java = getJava(dexName);

        if (java != null) {
            return java;
        }

        return dexName.replace('/', '.').substring(1, dexName.length()-1);
    }
}
