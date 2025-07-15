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
package com.reandroid.dex.key;

import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.ArrayIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class ArrayKey implements Key, Iterable<Key> {

    private final Key[] values;

    public ArrayKey(Key[] values) {
        this.values = values;
    }

    public Key get(int i) {
        return values[i];
    }
    public int length() {
        Key[] values = this.values;
        if (values != null) {
            return values.length;
        }
        return 0;
    }
    public boolean isEmpty() {
        return length() == 0;
    }
    @Override
    public Iterator<Key> iterator() {
        return ArrayIterator.of(values);
    }

    @Override
    public Iterator<? extends Key> mentionedKeys() {
        return iterator();
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        append(writer, ", ");
    }

    public void append(SmaliWriter writer, String separator) throws IOException {
        int length = this.length();
        for (int i = 0; i < length; i++) {
            if (i != 0) {
                writer.append(separator);
            }
            Key key = get(i);
            key.append(writer);
        }
    }
    @Override
    public String toString() {
        return toString(", ");
    }
    public String toString(String separator) {
        StringBuilder builder = new StringBuilder();
        int length = this.length();
        for (int i = 0; i < length; i++) {
            if (i != 0) {
                builder.append(separator);
            }
            Key key = get(i);
            builder.append(key);
        }
        return builder.toString();
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof ArrayKey)) {
            return 0;
        }
        ArrayKey arrayKey = (ArrayKey) obj;
        Iterator<Key> iterator1 = this.iterator();
        Iterator<Key> iterator2 = arrayKey.iterator();
        while (iterator1.hasNext() && iterator2.hasNext()) {
            int i = CompareUtil.compare(iterator1.next(), iterator2.next());
            if (i != 0) {
                return i;
            }
        }
        return CompareUtil.compare(length(), arrayKey.length());
    }

    @Override
    public int hashCode() {
        int hash = 0;
        Key[] values = this.values;
        if (values != null) {
            for (Key key : values) {
                hash = hash + hash * ObjectsUtil.hash(key);
            }
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ArrayKey)) {
            return false;
        }
        ArrayKey arrayKey = (ArrayKey) obj;
        if (this.length() != arrayKey.length()) {
            return false;
        }
        Iterator<Key> iterator1 = this.iterator();
        Iterator<Key> iterator2 = arrayKey.iterator();
        while (iterator1.hasNext() && iterator2.hasNext()) {
            if (!ObjectsUtil.equals(iterator1.next(), iterator2.next())) {
                return false;
            }
        }
        return true;
    }

    public static ArrayKey read(SmaliReader reader, char end) throws IOException {
        reader.skipWhitespacesOrComment();
        if (reader.getASCII(reader.position()) == end) {
            reader.readASCII();
            return new ArrayKey(new Key[0]);
        }
        List<Key> results = new ArrayCollection<>();
        while (true) {
            Key key = readNext(reader);
            results.add(key);
            reader.skipWhitespacesOrComment();
            if (reader.getASCII(reader.position()) == end) {
                break;
            }
            SmaliParseException.expect(reader, ',');
        }
        SmaliParseException.expect(reader, end);
        return new ArrayKey(results.toArray(new Key[results.size()]));
    }
    private static Key readNext(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        char c = reader.getASCII(reader.position());
        if (c == '"') {
            return StringKey.read(reader);
        }
        Key key = MethodHandleKey.read(reader);
        if (key != null) {
            return key;
        }
        key = TypeKey.primitiveType(c);
        if (key != null) {
            reader.readASCII();
            return key;
        }
        int lineEnd = reader.indexOfBeforeLineEnd(',');
        if (lineEnd < 0) {
            lineEnd = reader.indexOfLineEnd();
        }
        if (c == 'L' || c == '[') {
            int i = reader.indexOfBeforeLineEnd('>');
            if (i < 0 || i > lineEnd) {
                return TypeKey.read(reader);
            }
            c = reader.getASCII(i + 1);
            if (c == '(') {
                return MethodKey.read(reader);
            }
            if (c != ':') {
                throw new SmaliParseException("Expecting ':'", reader);
            }
            return FieldKey.read(reader);
        }
        return StringKey.read(reader);
    }
}
