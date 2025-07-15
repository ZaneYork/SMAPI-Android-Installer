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
import com.reandroid.jcommand.utils.CommandUtil;
import com.reandroid.jcommand.utils.ReflectionUtil;
import com.reandroid.jcommand.utils.SpreadSheet;
import com.reandroid.jcommand.utils.TwoColumnTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OptionStringBuilder {

    private final TwoColumnTable twoColumnTable;

    private final Object optionObject;
    private final List<Field> optionArgFieldsList;
    private Field mLastArgs;

    public OptionStringBuilder(Object optionObject) {
        this.optionObject = optionObject;
        this.twoColumnTable = new TwoColumnTable();
        this.optionArgFieldsList = new ArrayList<>();
        this.twoColumnTable.setColumnSeparator(" = ");
    }


    public void setMaxWidth(int maxWidth) {
        getTable().setMaxWidth(maxWidth);
    }
    public void setTab2(String tab2) {
        getTable().setTab2(tab2);
    }
    public void setColumnSeparator(String columnSeparator) {
        getTable().setColumnSeparator(columnSeparator);
    }
    public void setDrawBorder(boolean headerSeparators) {
        getTable().setDrawBorder(headerSeparators);
    }

    public TwoColumnTable getTable() {
        return twoColumnTable;
    }

    public String buildTable() {
        TwoColumnTable twoColumnTable = getTable();
        twoColumnTable.clear();
        buildTableRows();
        twoColumnTable.getSpreadSheet().setIndent(0, SpreadSheet.INDENT_RIGHT);
        SpreadSheet spreadSheet = twoColumnTable.buildTable();
        return spreadSheet.toString();
    }
    public String buildCommandArg() {
        String[] array = buildArray(true);
        StringBuilder builder = new StringBuilder();
        int length = array.length;
        for (int i = 0; i < length; i ++) {
            if(i != 0) {
                builder.append(' ');
            }
            builder.append(array[i]);
        }
        return builder.toString();
    }
    @SuppressWarnings("unchecked")
    private void buildTableRows() {
        listFields();
        TwoColumnTable twoColumnTable = getTable();
        for(Field field : optionArgFieldsList) {
            String name = ReflectionUtil.getArgName(field);
            if(name == null) {
                continue;
            }
            Object value = getFieldValue(field);
            if(value == null) {
                continue;
            }
            if(value instanceof Collection) {
                Collection<Object> collection = (Collection<Object>) value;
                for(Object o : collection) {
                    if(o == null) {
                        continue;
                    }
                    twoColumnTable.addRow(name, CommandUtil.quoteString(o.toString()));
                }
            } else {
                if(!ReflectionUtil.isFlagArg(field) || Boolean.TRUE.equals(value)) {
                    twoColumnTable.addRow(name, CommandUtil.quoteString(value.toString()));
                }
            }
        }
        twoColumnTable.addLine();
    }

    @SuppressWarnings("unchecked")
    private String[] buildArray(boolean quote) {
        listFields();
        List<String> results = new ArrayList<>();
        for(Field field : optionArgFieldsList) {
            String name = ReflectionUtil.getArgName(field);
            if(name == null) {
                continue;
            }
            Object value = getFieldValue(field);
            if(value == null) {
                continue;
            }
            if(value instanceof Collection) {
                Collection<Object> collection = (Collection<Object>) value;
                for(Object o : collection) {
                    if(o == null) {
                        continue;
                    }
                    String s = o.toString();
                    if(quote) {
                        s = CommandUtil.quoteString(s);
                    }
                    results.add(name);
                    results.add(s);
                }
            } else {
                if(ReflectionUtil.isFlagArg(field)) {
                    if(Boolean.TRUE.equals(value)) {
                        results.add(name);
                    }
                } else {
                    results.add(name);
                    String s = value.toString();
                    if(quote) {
                        s = CommandUtil.quoteString(s);
                    }
                    results.add(s);
                }
            }
        }
        return results.toArray(new String[0]);
    }
    private Object getFieldValue(Field field) {
        try {
            field.setAccessible(true);
            return field.get(optionObject);
        }catch (Exception e) {
            return null;
        }
    }
    private void listFields() {
        if(optionArgFieldsList.isEmpty() && mLastArgs == null) {
            listFields(optionObject.getClass());
        }
    }
    private void listFields(Class<?> type) {
        List<Field> fieldList = ReflectionUtil.listInstanceFields(type);
        for(Field field : fieldList) {
            if (field.getAnnotation(OptionArg.class) != null || field.getAnnotation(ChoiceArg.class) != null) {
                optionArgFieldsList.add(field);
            } else {
                LastArgs lastArgs = field.getAnnotation(LastArgs.class);
                if(lastArgs != null) {
                    mLastArgs = field;
                }
            }
        }
    }
}
