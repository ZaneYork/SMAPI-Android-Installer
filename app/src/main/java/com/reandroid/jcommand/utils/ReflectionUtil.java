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
package com.reandroid.jcommand.utils;

import com.reandroid.jcommand.annotations.ChoiceArg;
import com.reandroid.jcommand.annotations.LastArgs;
import com.reandroid.jcommand.annotations.OptionArg;
import com.reandroid.jcommand.annotations.OtherOption;
import com.reandroid.jcommand.exceptions.CommandFormatException;

import java.io.File;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ReflectionUtil {

    @SuppressWarnings("unchecked")
    public static void setCollection(Object obj, Field field, String value) {
        try {
            Collection<Object> collection = (Collection<Object>) field.get(obj);
            if(collection == null) {
                Class<?> type = field.getType();
                if (isInstanceClass(type, List.class)) {
                    collection = new ArrayList<>();
                } else {
                    collection = new HashSet<>();
                }
                field.set(obj, collection);
            }
            Object converted = createObject(getCollectionEntryType(field), value);
            collection.add(converted);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static Object createObject(Class<?> type, String value) {
        if(type == null || String.class.equals(type)) {
            return value;
        }
        if(type.isEnum()) {
            Object[] enumConstants = type.getEnumConstants();
            for (Object o : enumConstants) {
                Enum<?> e = (Enum<?>) o;
                if(value.equalsIgnoreCase(e.name())) {
                    return e;
                }
            }
            throw new CommandFormatException(type, value);
        }
        if(File.class.equals(type)) {
            return new File(value);
        }
        if(Integer.class.equals(type)) {
            try {
                return Integer.decode(value);
            } catch (NumberFormatException e1) {
                throw new CommandFormatException(Integer.class, value);
            }
        }
        if(Long.class.equals(type)) {
            try {
                return Long.decode(value);
            } catch (NumberFormatException e1) {
                throw new CommandFormatException(Long.class, value);
            }
        }
        if(Double.class.equals(type)) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e1) {
                throw new CommandFormatException(Long.class, value);
            }
        }
        if(Boolean.class.equals(type)) {
            boolean b;
            if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)) {
                b = true;
            } else if ("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value)) {
                b = false;
            } else {
                throw new CommandFormatException(Boolean.class, value);
            }
            return b;
        }
        throw new RuntimeException("Unsupported collection entry type: " + type);
    }
    public static Class<?> getCollectionEntryType(Field field) {
        return null;
//        Type type = field.getAnnotatedType().getType();
//        if(!(type instanceof ParameterizedType)) {
//            return null;
//        }
//        ParameterizedType parameterizedType = (ParameterizedType) type;
//        Type[] args = parameterizedType.getActualTypeArguments();
//        if(args == null || args.length != 1) {
//            return null;
//        }
//        try {
//            return Class.forName(args[0].getTypeName());
//        } catch (Exception ignored) {
//            return null;
//        }
    }
    public static void setFile(Object obj, Field field, String value) {
        try {
            File file = new File(value);
            field.set(obj, file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void setObject(Object obj, Field field, Object value) {
        try {
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void setEnum(Object obj, Field field, String value) {
        Class<?> type = field.getType();
        Object[] enumConstants = type.getEnumConstants();
        for (Object o : enumConstants) {
            Enum<?> e = (Enum<?>) o;
            if(value.equalsIgnoreCase(e.name())) {
                setObject(obj, field, o);
                return;
            }
        }
        throw new CommandFormatException(type, value);
    }

    public static void setDoubleObject(Object obj, Field field, String value) {
        try {
            Double d = Double.parseDouble(value);
            field.set(obj, d);
        } catch (NumberFormatException e1) {
            throw new CommandFormatException(Double.class, value);
        }catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }
    public static void setDouble(Object obj, Field field, String value) {
        try {
            double d = Double.parseDouble(value);
            field.setDouble(obj, d);
        } catch (NumberFormatException e1) {
            throw new CommandFormatException(Double.class, value);
        }catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }
    public static void setLongObject(Object obj, Field field, String value) {
        try {
            Long l = Long.decode(value);
            field.set(obj, l);
        } catch (NumberFormatException e1) {
            throw new CommandFormatException(Long.class, value);
        }catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }
    public static void setLong(Object obj, Field field, String value) {
        try {
            long l = Long.decode(value);
            field.setLong(obj, l);
        } catch (NumberFormatException e1) {
            throw new CommandFormatException(Long.class, value);
        }catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }
    public static void setIntObject(Object obj, Field field, String value) {
        try {
            Integer i = Integer.decode(value);
            field.set(obj, i);
        } catch (NumberFormatException e1) {
            throw new CommandFormatException(Integer.class, value);
        }catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }
    public static void setInt(Object obj, Field field, String value) {
        try {
            int i = Integer.decode(value);
            field.setInt(obj, i);
        } catch (NumberFormatException e1) {
            throw new CommandFormatException(Integer.class, value);
        }catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }
    public static void setBooleanObject(Object obj, Field field, String value) {
        Boolean b = null;
        if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)) {
            b = true;
        } else if ("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value)) {
            b = false;
        } else {
            throw new CommandFormatException(Boolean.class, value);
        }
        try {
            field.set(obj, b);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void setBoolean(Object obj, Field field, String value) {
        boolean b;
        if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)) {
            b = true;
        } else if ("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value)) {
            b = false;
        } else {
            throw new CommandFormatException(Boolean.class, value);
        }
        setBoolean(obj, field, b);
    }
    public static void setBoolean(Object obj, Field field, boolean value) {
        try {
            Class<?> type = field.getType();
            if(type.equals(boolean.class)) {
                field.setBoolean(obj, value);
            } else {
                field.set(obj, value);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static List<String> listNames(Field field) {
        List<String> results = new ArrayList<>();
        OptionArg optionArg = field.getAnnotation(OptionArg.class);
        if(optionArg != null) {
            results.add(optionArg.name());
            for(String name : optionArg.alternates()) {
                if(name != null) {
                    results.add(name);
                }
            }
        }
        ChoiceArg choiceArg = field.getAnnotation(ChoiceArg.class);
        if(choiceArg != null) {
            results.add(choiceArg.name());
            for(String name : choiceArg.alternates()) {
                if(name != null) {
                    results.add(name);
                }
            }
        }
        return results;
    }
    public static String getArgName(Field field) {
        OptionArg optionArg = field.getAnnotation(OptionArg.class);
        if(optionArg != null) {
            return optionArg.name();
        }
        ChoiceArg choiceArg = field.getAnnotation(ChoiceArg.class);
        if(choiceArg != null) {
            return choiceArg.name();
        }
        return null;
    }
    public static boolean isFlagArg(Field field) {
        OptionArg optionArg = field.getAnnotation(OptionArg.class);
        if(optionArg != null) {
            return optionArg.flag();
        }
        return false;
    }
    public static List<OptionArg> listOptionArgs(Class<?> clazz) {
        List<Field> fieldList = listInstanceFields(clazz);
        List<OptionArg> optionArgList = new ArrayList<>();
        for(Field field : fieldList) {
            OptionArg optionArg = field.getAnnotation(OptionArg.class);
            if (optionArg != null) {
                optionArgList.add(optionArg);
            }
        }
        return optionArgList;
    }
    public static List<OtherOption> listOtherOptions(Class<?> clazz) {
        List<Method> methodList = listMethods(clazz);
        List<OtherOption> results = new ArrayList<>();
        for(Method method : methodList) {
            OtherOption option = method.getAnnotation(OtherOption.class);
            if (option != null) {
                results.add(option);
            }
        }
        return results;
    }
    public static List<ChoiceArg> listChoiceArgs(Class<?> clazz) {
        List<Field> fieldList = listInstanceFields(clazz);
        List<ChoiceArg> choiceArgs = new ArrayList<>();
        for(Field field : fieldList) {
            ChoiceArg choiceArg = field.getAnnotation(ChoiceArg.class);
            if (choiceArg != null) {
                choiceArgs.add(choiceArg);
            }
        }
        return choiceArgs;
    }
    public static LastArgs getLastArgs(Class<?> clazz) {
        List<Field> fieldList = listInstanceFields(clazz);
        for(Field field : fieldList) {
            LastArgs lastArgs = field.getAnnotation(LastArgs.class);
            if (lastArgs != null) {
                return lastArgs;
            }
        }
        return null;
    }
    public static List<Field> listInstanceFields(Class<?> clazz) {
        List<Field> results = new ArrayList<>();
        listInstanceFields(clazz, results);
        return results;
    }
    private static void listInstanceFields(Class<?> clazz, List<Field> results) {
        if(clazz == null || Object.class.equals(clazz)) {
            return;
        }
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields) {
            int mod = field.getModifiers();
            if((mod & Modifier.STATIC) != Modifier.STATIC) {
                results.add(field);
            }
        }
        listInstanceFields(clazz.getSuperclass(), results);
    }
    public static List<Method> listMethods(Class<?> clazz) {
        List<Method> results = new ArrayList<>();
        listMethods(clazz, results);
        return results;
    }
    private static void listMethods(Class<?> clazz, List<Method> results) {
        if(clazz == null || Object.class.equals(clazz)) {
            return;
        }
        Method[] methods = clazz.getDeclaredMethods();
        for(Method method : methods) {
            if(method != null) {
                results.add(method);
            }
        }
        listMethods(clazz.getSuperclass(), results);
    }

    public static<T> T createNew(Class<T> type) {
        try {
            Constructor<T> constructor = type.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean isInstanceClass(Class<?> type, Class<?> superType) {
        if(type == null) {
            return false;
        }
        if(type.equals(superType)) {
            return true;
        }
        if(type.isPrimitive() || type.equals(Object.class)) {
            return false;
        }
        Class<?>[] interfaces = type.getInterfaces();
        for(Class<?> clazz : interfaces) {
            if(isInstanceClass(clazz, superType)) {
                return true;
            }
        }
        return isInstanceClass(type.getSuperclass(), superType);
    }

}
