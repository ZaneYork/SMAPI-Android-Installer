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
package com.reandroid.common;

public interface DiagnosticMessage {
    Type type();
    default String getTag() {
        return null;
    }
    default Origin getSource() {
        return null;
    }

    class StringMessage implements DiagnosticMessage {

        private final Type type;
        private final Origin source;
        private final String tag;
        private final String message;

        public StringMessage(Type type, Origin source, String tag, String message) {
            this.type = type;
            this.source = source;
            this.tag = tag;
            this.message = message;
        }
        public StringMessage(Type type, String tag, String message) {
            this(type, null, tag, message);
        }
        public StringMessage(Type type, String message) {
            this(type, null, null, message);
        }
        @Override
        public Type type() {
            return type;
        }
        @Override
        public Origin getSource() {
            return source;
        }
        @Override
        public String getTag() {
            return tag;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(type().getName());
            builder.append(": ");
            String tag = getTag();
            if(tag != null) {
                builder.append(tag);
                builder.append(" : ");
            }
            Origin source = getSource();
            if(source != null) {
                builder.append(source);
                builder.append(", ");
            }
            builder.append(getMessage());
            return builder.toString();
        }
    }

    enum Type {

        INFO("I"),
        WARN("W"),
        ERROR("E"),
        VERBOSE("V"),
        DEBUG("D");

        String simpleName;

        Type(String simpleName) {
            this.simpleName = simpleName;
        }
        public String getName() {
            return simpleName;
        }
    }
}
