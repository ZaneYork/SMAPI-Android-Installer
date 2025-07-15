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
package com.reandroid.jcommand;

import com.reandroid.jcommand.annotations.ChoiceArg;
import com.reandroid.jcommand.annotations.LastArgs;
import com.reandroid.jcommand.annotations.OptionArg;
import com.reandroid.jcommand.exceptions.DuplicateOptionException;
import com.reandroid.jcommand.exceptions.MissingValueException;
import com.reandroid.jcommand.exceptions.UnknownOptionException;
import com.reandroid.jcommand.utils.CommandUtil;
import com.reandroid.jcommand.utils.ReflectionUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class SubCommandParser {

    private final String[] mArgs;
    private Object mObj;
    private final Map<String, Field> optionFieldsMap;
    private final Map<String, Field> choiceOptionFieldsMap;
    private Field mLastArgs;
    private final Set<Field> mParsedFields;
    private int mIndex;

    public SubCommandParser(String[] args) {
        this.mArgs = args;
        this.optionFieldsMap = new HashMap<>();
        this.choiceOptionFieldsMap = new HashMap<>();
        this.mParsedFields = new HashSet<>();
    }
    public <T> T parse(Class<T> type) {
        T obj = ReflectionUtil.createNew(type);
        parse(obj);
        return obj;
    }
    public void parse(Object obj) {
        this.mObj = obj;
        mapFields(obj.getClass());
        parseArgs();
    }
    private void parseArgs() {
        String[] args = this.mArgs;
        int length = args.length;
        for(mIndex = 0; mIndex < length; mIndex++) {
            String arg = args[mIndex];
            Field field = optionFieldsMap.get(arg);
            if(field == null) {
                field = choiceOptionFieldsMap.get(arg);
                if(field != null) {
                    parseChoiceOptionField(field);
                    continue;
                }
            }
            if(field == null) {
                if(mLastArgs == null) {
                    throw new UnknownOptionException(arg);
                }
                parseLastArgs();
                continue;
            }
            parseOptionField(field);
        }
    }
    private void addParsed(Field field, String arg) {
        if(mParsedFields.contains(field)) {
            if(!ReflectionUtil.isInstanceClass(field.getType(), Collection.class)) {
                throw new DuplicateOptionException(arg);
            }
        } else {
            mParsedFields.add(field);
        }
    }
    private void parseOptionField(Field field) {
        OptionArg optionArg = field.getAnnotation(OptionArg.class);
        addParsed(field, optionArg.name());
        field.setAccessible(true);
        if(optionArg.flag()) {
            ReflectionUtil.setBoolean(mObj, field, true);
            return;
        }
        mIndex ++;
        if(mIndex >= mArgs.length) {
            throw new MissingValueException(mArgs[mIndex - 1]);
        }
        String arg = mArgs[mIndex];
        Class<?> type = field.getType();
        Object obj = this.mObj;
        if(ReflectionUtil.isInstanceClass(type, Collection.class)) {
            ReflectionUtil.setCollection(obj, field, arg);
            return;
        }
        if (type == String.class) {
            ReflectionUtil.setObject(obj, field, arg);
        } else if (type == int.class) {
            ReflectionUtil.setInt(obj, field, arg);
        } else if (type == Integer.class) {
            ReflectionUtil.setIntObject(obj, field, arg);
        } else if (type == long.class) {
            ReflectionUtil.setLong(obj, field, arg);
        } else if (type == Long.class) {
            ReflectionUtil.setLongObject(obj, field, arg);
        } else if (type == double.class) {
            ReflectionUtil.setDouble(obj, field, arg);
        } else if (type == Double.class) {
            ReflectionUtil.setDoubleObject(obj, field, arg);
        } else if (type == boolean.class) {
            ReflectionUtil.setBoolean(obj, field, arg);
        } else if (type == Boolean.class) {
            ReflectionUtil.setBooleanObject(obj, field, arg);
        } else if (type == File.class) {
            ReflectionUtil.setFile(obj, field, arg);
        } else {
            throw new RuntimeException("Unsupported field type: " + field);
        }
    }
    private void parseChoiceOptionField(Field field) {
        ChoiceArg choiceArg = field.getAnnotation(ChoiceArg.class);
        addParsed(field, choiceArg.name());
        field.setAccessible(true);
        mIndex ++;
        if(mIndex >= mArgs.length) {
            throw new MissingValueException(mArgs[mIndex - 1]);
        }
        String arg = mArgs[mIndex];
        if (!CommandUtil.containsIgnoreCase(choiceArg.values(), arg)) {
            throw new UnknownOptionException(arg);
        }
        Class<?> type = field.getType();
        if(String.class.equals(type)) {
            ReflectionUtil.setObject(mObj, field, arg);
        } else if (type.isEnum()) {
            ReflectionUtil.setEnum(mObj, field, arg);
        } else {
            throw new RuntimeException("Unsupported choice field type: " + field);
        }
    }
    @SuppressWarnings("unchecked")
    private void parseLastArgs() {
        Field field = this.mLastArgs;
        field.setAccessible(true);
        Collection<Object> collection;
        try {
            collection = (Collection<Object>) field.get(mObj);
            if(collection == null) {
                Class<?> type = field.getType();
                if (ReflectionUtil.isInstanceClass(type, List.class)) {
                    collection = new ArrayList<>();
                } else {
                    collection = new HashSet<>();
                }
                field.set(mObj, collection);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        mIndex ++;
        while (mIndex < mArgs.length) {
            collection.add(mArgs[mIndex]);
            mIndex ++;
        }
    }
    private void mapFields(Class<?> type) {
        List<Field> fieldList = ReflectionUtil.listInstanceFields(type);
        Map<String, Field> optionFieldsMap = this.optionFieldsMap;
        Map<String, Field> choiceOptionFieldsMap = this.choiceOptionFieldsMap;
        for(Field field : fieldList) {
            List<String> nameList = ReflectionUtil.listNames(field);
            for(String name : nameList) {
                Field exist = getField(name);
                if(exist != null) {
                    throw new RuntimeException("Duplicate fields: '"
                            + exist + "', '" + field + "'");
                }
                if(field.getAnnotation(OptionArg.class) != null) {
                    optionFieldsMap.put(name, field);
                }
                if(field.getAnnotation(ChoiceArg.class) != null) {
                    choiceOptionFieldsMap.put(name, field);
                }
            }
            if(nameList.isEmpty()) {
                LastArgs lastArgs = field.getAnnotation(LastArgs.class);
                if(lastArgs != null) {
                    if(mLastArgs != null) {
                        throw new RuntimeException("Duplicate LastArgs: '"
                                + mLastArgs + "', '" + field + "'");
                    }
                    mLastArgs = field;
                }
            }
        }
    }
    private Field getField(String name) {
        Field result = optionFieldsMap.get(name);
        if(result == null) {
            result = choiceOptionFieldsMap.get(name);
        }
        return result;
    }
    public static <T> T parse(Class<T> type, String[] args) {
        SubCommandParser parser = new SubCommandParser(args);
        return parser.parse(type);
    }
    public static void parse(Object obj, String[] args) {
        SubCommandParser parser = new SubCommandParser(args);
        parser.parse(obj);
    }
}
