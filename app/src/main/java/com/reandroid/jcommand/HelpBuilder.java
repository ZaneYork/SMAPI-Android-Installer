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

import com.reandroid.jcommand.utils.SpreadSheet;
import com.reandroid.jcommand.utils.TwoColumnTable;

import java.util.HashMap;
import java.util.Map;

public abstract class HelpBuilder {

    private final CommandStringResource stringResource;
    private final TwoColumnTable twoColumnTable;
    private String[] footers;

    public HelpBuilder(CommandStringResource stringResource) {
        if(stringResource == null) {
            stringResource = defaultStringResource();
        }
        this.stringResource = stringResource;
        this.twoColumnTable = new TwoColumnTable();
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

    public String[] getFooters() {
        return footers;
    }
    public void setFooters(String ... footers) {
        this.footers = footers;
    }

    public String build() {
        return buildTable().toString();
    }

    public abstract SpreadSheet buildTable();

    public void appendLines(boolean tabbed, String[] headings) {
        if(headings == null || headings.length == 0) {
            return;
        }
        CommandStringResource stringResource = getStringResource();
        TwoColumnTable twoColumnTable = getTable();
        twoColumnTable.addSeparator();
        for(String heading : headings) {
            heading = stringResource.getString(heading);
            if (tabbed) {
                twoColumnTable.addMergedRowTabbed(heading);
            } else {
                twoColumnTable.addMergedRow(heading);
            }
        }
        twoColumnTable.addSeparator();
    }
    protected void appendFooters() {
        String[] footers = getFooters();
        if(footers == null || footers.length == 0) {
            return;
        }
        CommandStringResource stringResource = getStringResource();
        TwoColumnTable twoColumnTable = getTable();
        twoColumnTable.addSeparator();
        for(String footer : footers) {
            twoColumnTable.addMergedRow(stringResource.getString(footer))
                    .setHorizontalIndent(SpreadSheet.INDENT_LEFT);
        }
    }
    public TwoColumnTable getTable() {
        return twoColumnTable;
    }

    public CommandStringResource getStringResource() {
        return stringResource;
    }

    public void reset() {
        getTable().clear();
    }

    public static CommandStringResource defaultStringResource() {
        Map<String, String> map = new HashMap<>();
        map.put(CommandStrings.title_commands, "Commands:");
        map.put(CommandStrings.title_options, "Options:");
        map.put(CommandStrings.title_flags, "Flags:");
        map.put(CommandStrings.title_usage, "Usage:");
        map.put(CommandStrings.title_example, "Examples:");
        return resourceName -> {
            String str = map.get(resourceName);
            if(str == null) {
                str = resourceName;
            }
            return str;
        };
    }
}
