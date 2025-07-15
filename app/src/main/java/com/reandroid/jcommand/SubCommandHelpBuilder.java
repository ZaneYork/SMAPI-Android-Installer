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

import android.text.TextUtils;

import com.reandroid.jcommand.annotations.ChoiceArg;
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.LastArgs;
import com.reandroid.jcommand.annotations.OptionArg;
import com.reandroid.jcommand.utils.CommandUtil;
import com.reandroid.jcommand.utils.ReflectionUtil;
import com.reandroid.jcommand.utils.SpreadSheet;
import com.reandroid.jcommand.utils.TwoColumnTable;

import java.util.*;

public class SubCommandHelpBuilder extends HelpBuilder {

    private final CommandOptions commandOptions;
    private final List<OptionArg> optionArgList;
    private final List<ChoiceArg> choiceArgList;
    private final LastArgs lastArgs;

    public SubCommandHelpBuilder(CommandStringResource stringResource, Class<?> clazz) {
        super(stringResource);
        this.commandOptions = clazz.getAnnotation(CommandOptions.class);
        this.optionArgList = ReflectionUtil.listOptionArgs(clazz);
        this.choiceArgList = ReflectionUtil.listChoiceArgs(clazz);
        this.lastArgs = ReflectionUtil.getLastArgs(clazz);
    }
    public SubCommandHelpBuilder(Class<?> clazz) {
        this(null, clazz);
    }


    @Override
    public SpreadSheet buildTable() {

        reset();

        TwoColumnTable twoColumnTable = getTable();

        appendDescription();
        appendUsage();
        appendOptionArgs(getOptionArgList(), CommandStrings.title_options);
        appendChoiceArgs();
        appendOptionArgs(getFlagList(), CommandStrings.title_flags);
        appendExamples();
        twoColumnTable.addSeparator();
        return twoColumnTable.buildTable();
    }
    private void appendOptionArgs(List<OptionArg> optionArgList, String title) {
        if(optionArgList.isEmpty()) {
            return;
        }
        sortOptionArgList(optionArgList);

        CommandStringResource stringResource = getStringResource();
        TwoColumnTable twoColumnTable = getTable();
        twoColumnTable.addSeparator();

        twoColumnTable.addMergedRow(stringResource.getString(title));
        int length = optionArgList.size();
        for(int i = 0; i < length; i++) {
            OptionArg optionArg = optionArgList.get(i);
            StringBuilder name = new StringBuilder();
            name.append(optionArg.name());
            for(String l : optionArg.alternates()) {
                name.append(" | ");
                name.append(l);
            }
            twoColumnTable.addRow(name.toString(), stringResource.getString(optionArg.description()));
        }
    }
    private void appendChoiceArgs() {
        List<ChoiceArg> choiceArgList = this.choiceArgList;
        if(choiceArgList.isEmpty()) {
            return;
        }

        CommandStringResource stringResource = getStringResource();
        TwoColumnTable twoColumnTable = getTable();

        if(optionArgList.isEmpty()) {
            twoColumnTable.addMergedRow(stringResource.getString(CommandStrings.title_options));
        }
        int length = choiceArgList.size();
        for(int i = 0; i < length; i++) {
            ChoiceArg choiceArg = choiceArgList.get(i);
            StringBuilder name = new StringBuilder();
            name.append(choiceArg.name());
            for(String l : choiceArg.alternates()) {
                name.append(" | ");
                name.append(l);
            }
            String description = stringResource.getString(choiceArg.description()) + "\n"
                    + CommandUtil.asString(choiceArg.values());
            twoColumnTable.addRow(name.toString(), description);
        }
    }
    private void appendExamples() {
        CommandOptions commandOptions = this.commandOptions;
        if(commandOptions == null) {
            return;
        }
        String[] examples = commandOptions.examples();
        int length = examples.length;
        if(length == 0) {
            return;
        }
        CommandStringResource stringResource = getStringResource();
        TwoColumnTable twoColumnTable = getTable();

        twoColumnTable.addSeparator();
        twoColumnTable.addMergedRow(stringResource.getString(CommandStrings.title_example));
        for(int i = 0; i < length; i++) {
            String col1 = (i + 1) + ")  " + stringResource.getString(examples[i]);
            twoColumnTable.addMergedRowTabbed(col1);
        }
    }
    private void appendUsage() {
        CommandOptions options = this.commandOptions;
        if(options == null) {
            return;
        }
        String usage = options.usage();
        if(TextUtils.isEmpty(usage)) {
            return;
        }

        CommandStringResource stringResource = getStringResource();
        TwoColumnTable twoColumnTable = getTable();
        twoColumnTable.addSeparator();

        twoColumnTable.addMergedRow(stringResource.getString(CommandStrings.title_usage));
        twoColumnTable.addMergedRowTabbed(stringResource.getString(options.usage()));
        twoColumnTable.addSeparator();
    }
    private void appendDescription() {
        CommandStringResource stringResource = getStringResource();
        TwoColumnTable twoColumnTable = getTable();

        twoColumnTable.addSeparator();

        CommandOptions options = this.commandOptions;
        if(options == null) {
            return;
        }
        twoColumnTable.addMergedRow(stringResource.getString(options.description()));
        twoColumnTable.addSeparator();
    }

    private List<OptionArg> getOptionArgList() {
        List<OptionArg> results = new ArrayList<>();
        for(OptionArg optionArg : this.optionArgList) {
            if(!optionArg.flag()) {
                results.add(optionArg);
            }
        }
        return results;
    }
    private List<OptionArg> getFlagList() {
        List<OptionArg> results = new ArrayList<>();
        for(OptionArg optionArg : this.optionArgList) {
            if(optionArg.flag()) {
                results.add(optionArg);
            }
        }
        return results;
    }
    private static void sortOptionArgList(List<OptionArg> optionArgList) {
        optionArgList.sort(new Comparator<OptionArg>() {
            @Override
            public int compare(OptionArg arg1, OptionArg arg2) {
                return arg1.name().compareTo(arg2.name());
            }
        });
    }
}
