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
import com.reandroid.jcommand.annotations.OtherOption;
import com.reandroid.jcommand.utils.ReflectionUtil;
import com.reandroid.jcommand.utils.SpreadSheet;
import com.reandroid.jcommand.utils.TwoColumnTable;

import java.util.ArrayList;
import java.util.List;

public class CommandHelpBuilder extends HelpBuilder {

    private final Class<?> mainCommandClass;
    private MainCommand mainCommand;

    public CommandHelpBuilder(CommandStringResource stringResource, Class<?> mainCommandClass) {
        super(stringResource);
        if (mainCommandClass == null) {
            throw new NullPointerException("Null MainCommand");
        }
        this.mainCommandClass = mainCommandClass;
    }
    public CommandHelpBuilder(Class<?> mainCommandClass) {
        this(null, mainCommandClass);
    }

    @Override
    public SpreadSheet buildTable() {

        reset();
        appendLines(false, getMainCommand().headers());
        appendUsageLines();
        appendCommandOptions();
        appendOtherOptions();
        appendFooters();

        TwoColumnTable twoColumnTable = getTable();
        twoColumnTable.addSeparator();

        return twoColumnTable.buildTable();
    }

    public MainCommand getMainCommand() {
        MainCommand mainCommand = this.mainCommand;
        if(mainCommand == null) {
            mainCommand = mainCommandClass.getAnnotation(MainCommand.class);
            if(mainCommand == null) {
                throw new RuntimeException("Class not annotated with MainCommand: '"
                        + mainCommandClass + "'");
            }
            this.mainCommand = mainCommand;
        }
        return mainCommand;
    }

    private void appendCommandOptions() {
        List<CommandOptions> commandOptionsList = getCommandOptions();

        TwoColumnTable twoColumnTable = getTable();

        twoColumnTable.addMergedRow(getStringResource()
                .getString(CommandStrings.title_commands));

        for(CommandOptions options : commandOptionsList) {
            appendCommandOptions(options);
        }
        twoColumnTable.addSeparator();
    }
    private void appendCommandOptions(CommandOptions options) {
        TwoColumnTable twoColumnTable = getTable();
        StringBuilder name = new StringBuilder();
        name.append(options.name());
        String[] alternates = options.alternates();
        for(String alt : alternates) {
            name.append(" | ");
            name.append(alt);
        }
        twoColumnTable.addRow(name.toString(),
                getStringResource().getString(options.description()));
    }

    private void appendOtherOptions() {
        List<OtherOption> otherOptionList = getOtherOptions();
        if(otherOptionList.isEmpty()) {
            return;
        }
        TwoColumnTable twoColumnTable = getTable();

        twoColumnTable.addMergedRow(getStringResource()
                .getString(CommandStrings.title_other_options));

        for(OtherOption options : otherOptionList) {
            appendOtherOption(options);
        }
        twoColumnTable.addSeparator();
    }
    private void appendOtherOption(OtherOption options) {
        TwoColumnTable twoColumnTable = getTable();
        StringBuilder nameBuilder = new StringBuilder();
        String[] names = options.names();
        int length = names.length;
        for(int i = 0; i < length; i++) {
            if(i != 0) {
                nameBuilder.append(" | ");
            }
            nameBuilder.append(names[i]);
        }
        twoColumnTable.addRow(nameBuilder.toString(),
                getStringResource().getString(options.description()));
    }
    private void appendUsageLines() {
        MainCommand mainCommand = getMainCommand();
        if(mainCommand == null) {
            return;
        }
        String[] usageLines = mainCommand.usages();
        if(usageLines.length == 0) {
            return;
        }
        CommandStringResource stringResource = getStringResource();
        TwoColumnTable twoColumnTable = getTable();

        twoColumnTable.addSeparator();

        twoColumnTable.addMergedRow(stringResource.getString(CommandStrings.title_usage));

        for(String line : usageLines) {
            twoColumnTable.addMergedRowTabbed(stringResource.getString(line));
        }
        twoColumnTable.addSeparator();
    }

    public List<CommandOptions> getCommandOptions() {
        List<CommandOptions> results = new ArrayList<>();
        Class<?>[] optionClasses = getMainCommand().options();
        for(Class<?> clazz : optionClasses) {
            CommandOptions commandOptions = clazz.getAnnotation(CommandOptions.class);
            if(commandOptions == null) {
                throw new IllegalArgumentException("Invalid option class: '"
                        + clazz + "', should annotate CommandOptions");
            }
            results.add(commandOptions);
        }
        return results;
    }
    public List<OtherOption> getOtherOptions() {
        return ReflectionUtil.listOtherOptions(mainCommandClass);
    }
}
