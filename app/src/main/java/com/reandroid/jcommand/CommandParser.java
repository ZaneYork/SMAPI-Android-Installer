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

import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.MainCommand;
import com.reandroid.jcommand.annotations.OnOptionSelected;
import com.reandroid.jcommand.annotations.OtherOption;
import com.reandroid.jcommand.exceptions.CommandException;
import com.reandroid.jcommand.utils.ReflectionUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class CommandParser {

    private final Class<?> mainCommandClass;

    public CommandParser(Class<?> mainCommandClass) {
        this.mainCommandClass = mainCommandClass;
    }

    public void parse(Object callback, String ... args) {
        if(args == null || args.length == 0) {
            throw new CommandException(CommandStrings.empty_command_args_exception);
        }
        String command = args[0];
        Method otherOptionMethod = getOtherOptionMethod(command, callback == null);
        if(otherOptionMethod != null) {
            try {
                otherOptionMethod.setAccessible(true);
                otherOptionMethod.invoke(callback);
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Class<?> optionClass = getOptionClass(command);
        if(optionClass != null) {
            int length = args.length;
            String[] subArgs = new String[length - 1];
            for(int i = 1; i < length; i++) {
                subArgs[i - 1] = args[i];
            }
            parseSubCommand(callback, optionClass, subArgs);
        } else {
            throw new CommandException(CommandStrings.unknown_command_exception, command);
        }
    }
    private void parseSubCommand(Object callback, Class<?> optionClass, String[] args) {
        Object obj = SubCommandParser.parse(optionClass, args);
        boolean empty = args.length == 0;
        Method onOptionSelectedMethod = getOnOptionSelectedMethod(callback == null);
        try {
            onOptionSelectedMethod.setAccessible(true);
            onOptionSelectedMethod.invoke(callback, obj, empty);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private Class<?> getOptionClass(String command) {
        MainCommand mainCommand = mainCommandClass.getAnnotation(MainCommand.class);
        Class<?>[] classes = mainCommand.options();
        for(Class<?> clazz : classes) {
            CommandOptions commandOptions = clazz.getAnnotation(CommandOptions.class);
            if(commandOptions.name().equals(command)) {
                return clazz;
            }
            String[] names = commandOptions.alternates();
            for(String name : names) {
                if(name.equals(command)) {
                    return clazz;
                }
            }
        }
        return null;
    }

    private Method getOtherOptionMethod(String command, boolean is_static) {
        List<Method> methodList = ReflectionUtil.listMethods(mainCommandClass);
        for(Method method : methodList) {
            boolean staticMethod = (method.getModifiers() & Modifier.STATIC) == Modifier.STATIC;
            if(staticMethod == is_static) {
                OtherOption option = method.getAnnotation(OtherOption.class);
                if(option != null) {
                    String[] names = option.names();
                    for(String name : names) {
                        if(name.equals(command)) {
                            return method;
                        }
                    }
                }
            }
        }
        return null;
    }
    private Method getOnOptionSelectedMethod(boolean is_static) {
        List<Method> methodList = ReflectionUtil.listMethods(mainCommandClass);
        for(Method method : methodList) {
            boolean staticMethod = (method.getModifiers() & Modifier.STATIC) == Modifier.STATIC;
            if(staticMethod == is_static) {
                OnOptionSelected option = method.getAnnotation(OnOptionSelected.class);
                if(option != null) {
                    return method;
                }
            }
        }
        if(is_static) {
            throw new RuntimeException("No static method annotated with OnOptionSelected in class: '" + mainCommandClass + "'");
        }
        throw new RuntimeException("No instance method annotated with OnOptionSelected in class: '" + mainCommandClass + "'");
    }

}
