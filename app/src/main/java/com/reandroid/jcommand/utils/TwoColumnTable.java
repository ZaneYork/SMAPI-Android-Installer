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

public class TwoColumnTable {

    private final SpreadSheet mSpreadSheet;
    private String tab2;
    private String mColumnSeparator;
    private int maxWidth;
    private boolean drawBorder;

    public TwoColumnTable() {
        this.mSpreadSheet = new SpreadSheet();
        this.tab2 = "  ";
        this.mColumnSeparator = "   ";
        this.maxWidth = 80;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }
    public void setTab2(String tab2) {
        this.tab2 = tab2;
    }
    public void setColumnSeparator(String columnSeparator) {
        this.mColumnSeparator = columnSeparator;
    }
    public void setDrawBorder(boolean drawBorder) {
        this.drawBorder = drawBorder;
    }

    public SpreadSheet getSpreadSheet() {
        return mSpreadSheet;
    }
    public void clear() {
        getSpreadSheet().clear();
    }

    public String build() {
        return buildTable().toString();
    }
    public SpreadSheet buildTable() {
        SpreadSheet spreadSheet = this.getSpreadSheet();
        spreadSheet.fixColumnCount();
        spreadSheet.fitColumnWidth(0);
        int firstWidth = spreadSheet.getColumnPrintWidth(0);
        int maxWidth = this.maxWidth - firstWidth;
        if (maxWidth <= 0) {
            maxWidth = 1;
        }
        int mergedCellsWidth = spreadSheet.getMergedCellsWidth() - firstWidth;
        int width = spreadSheet.fitColumnWidth(1);
        int diff = spreadSheet.getColumnPrintWidth(1) - width;
        maxWidth = maxWidth - diff;
        if(mergedCellsWidth > width) {
            width = mergedCellsWidth;
        }

        if(width > maxWidth) {
            width = maxWidth;
        }
        spreadSheet.setColumnWidth(1, width);
        return spreadSheet.format();
    }

    public SpreadSheet.Row addRow(Object item1, Object item2) {
        SpreadSheet.Row row = new SpreadSheet.Row();
        SpreadSheet.Cell cell1 = new SpreadSheet.Cell(item1);
        SpreadSheet.Cell cell2 = new SpreadSheet.Cell(item2);
        row.add(cell1);
        row.add(cell2);
        String tab2 = this.tab2;
        if(drawBorder) {
            tab2 = "|" + tab2;
        }
        cell1.setBorderLeft(tab2);
        cell1.setBorderRight("");
        cell2.setBorderLeft(mColumnSeparator);
        if(drawBorder) {
            cell2.setBorderRight(" |");
        } else {
            cell2.setBorderRight("");
        }
        mSpreadSheet.add(row);
        return row;
    }
    public boolean hasSeparatorRow() {
        SpreadSheet.Row row = mSpreadSheet.get(mSpreadSheet.size() - 1);
        if(row != null) {
            return row.get(0) instanceof SpreadSheet.SeparatorCells;
        }
        return false;
    }
    public SpreadSheet.MergedCells addMergedRow(Object item) {
        SpreadSheet.Row row = addRow(item, "");
        SpreadSheet.MergedCells mergedCells = row.merge(0, 2);
        mergedCells.setHorizontalIndent(SpreadSheet.INDENT_LEFT);
        mergedCells.setVerticalIndent(SpreadSheet.INDENT_TOP);
        if(drawBorder) {
            mergedCells.setBorderLeft("|");
            mergedCells.setBorderRight(" |");
            mergedCells.setHorizontalIndent(SpreadSheet.INDENT_CENTER);
        }
        return mergedCells;
    }
    public SpreadSheet.MergedCells addMergedRowTabbed(Object item) {
        SpreadSheet.MergedCells mergedCells = addMergedRow(item);
        String tab2 = this.tab2;
        if(drawBorder) {
            tab2 = "|" + tab2;
        }
        mergedCells.setHorizontalIndent(SpreadSheet.INDENT_LEFT);
        mergedCells.setBorderLeft(tab2);
        return mergedCells;
    }
    public void addSeparator() {
        if(drawBorder && !hasSeparatorRow()) {

            boolean first = mSpreadSheet.size() == 0;

            SpreadSheet.Row row = addRow("", "");
            SpreadSheet.SeparatorCells mergedCells = row.asSeparator();
            mergedCells.setBorderLeft(first ? " " : "|");
            mergedCells.setBorderRight(first ? " " : "_|");
            mergedCells.setHorizontalIndent(SpreadSheet.INDENT_LEFT);
            mergedCells.setVerticalIndent(SpreadSheet.INDENT_TOP);
        }
    }
    public void addLine() {
        if(!hasSeparatorRow()) {

            boolean add_vertical = drawBorder && mSpreadSheet.size() != 0;

            SpreadSheet.Row row = addRow("", "");
            SpreadSheet.SeparatorCells mergedCells = row.asSeparator();
            mergedCells.setBorderLeft(add_vertical ? "|" : " ");
            mergedCells.setBorderRight(add_vertical ? "_|" : " ");
            mergedCells.setHorizontalIndent(SpreadSheet.INDENT_LEFT);
            mergedCells.setVerticalIndent(SpreadSheet.INDENT_TOP);
        }
    }
}
