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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SpreadSheet implements Iterable<SpreadSheet.Row> {

    private final List<Row> rowList;

    public SpreadSheet() {
        this.rowList = new ArrayList<>();
    }

    public SpreadSheet format() {
        SpreadSheet spreadSheet = new SpreadSheet();
        for (Row parent : this) {
            spreadSheet.addAll(parent.getSplitLines());
        }
        return spreadSheet;
    }


    public SpreadSheet add(Row row) {
        rowList.add(row);
        return this;
    }

    public SpreadSheet addAll(Collection<? extends Row> rows) {
        rowList.addAll(rows);
        return this;
    }

    public Row get(int i) {
        if(i >= 0 && i < rowList.size()) {
            return rowList.get(i);
        }
        return null;
    }

    public int size() {
        return rowList.size();
    }
    public void clear() {
        rowList.clear();
    }

    public int getPrintWidth() {
        int result = 0;
        for (Row row : this) {
            int width = row.getPrintWidth();
            if(width > result) {
                result = width;
            }
        }
        return result;
    }
    public int getMergedCellsWidth() {
        int result = 0;
        for (Row row : this) {
            int width = row.getMergedCellsWidth();
            if(width > result) {
                result = width;
            }
        }
        return result;
    }
    public void setIndent(int columnIndex, int indent) {
        for (Row row : this) {
            row.setIndent(columnIndex, indent);
        }
    }

    public int getColumnCount() {
        int result = 0;
        for(Row row : this) {
            int count = row.getColumnCount();
            if(count > result) {
                result = count;
            }
        }
        return result;
    }
    public void setColumnCount(int columnCount) {
        for (Row row : this) {
            row.ensureSize(columnCount);
        }
    }
    public void fixColumnCount() {
        setColumnCount(getColumnCount());
    }

    public void setBorderLeft(int columnIndex, String border) {
        for (Row row : this) {
            row.setBorderLeft(columnIndex, border);
        }
    }

    public void setBorderRight(int columnIndex, String border) {
        for (Row row : this) {
            row.setBorderRight(columnIndex, border);
        }
    }

    public int itemWidth(int columnIndex) {
        int result = 0;
        for (Row row : this) {
            int width = row.itemWidth(columnIndex);
            if (width > result) {
                result = width;
            }
        }
        return result;
    }
    public int getColumnPrintWidth(int columnIndex) {
        int result = 0;
        for (Row row : this) {
            int width = row.getColumnPrintWidth(columnIndex);
            if (width > result) {
                result = width;
            }
        }
        return result;
    }

    public void setColumnWidth(int columnIndex, int columnWidth) {
        for (Row row : this) {
            row.setColumnWidth(columnIndex, columnWidth);
        }
    }

    public int fitColumnWidth(int columnIndex) {
        int width = itemWidth(columnIndex);
        setColumnWidth(columnIndex, width);
        return width;
    }
    public void fitColumnWidth() {
        int columnCount = getColumnCount();
        for(int i = 0; i < columnCount; i++) {
            fitColumnWidth(i);
        }
    }

    @Override
    public Iterator<Row> iterator() {
        return rowList.iterator();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int size = size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                builder.append('\n');
            }
            builder.append(get(i));
        }
        return builder.toString();
    }

    public static class Row implements Iterable<Cell> {

        private final List<Cell> cellList;
        private String mTab;

        public Row() {
            this.cellList = new ArrayList<>();
            this.mTab = "";
        }

        public void add(Cell cell) {
            cellList.add(cell);
        }
        public void add(int i, Cell cell) {
            cellList.add(i, cell);
        }

        public String getTab() {
            return mTab;
        }
        public void setTab(String tab) {
            if(tab == null) {
                tab = "";
            }
            this.mTab = tab;
        }

        public MergedCells merge(int start, int count) {
            List<Cell> childList = new ArrayList<>(count);
            for(int i = 0; i < count; i++) {
                childList.add(remove(start));
            }
            MergedCells mergedCells = new MergedCells(childList);
            add(start, mergedCells);
            return mergedCells;
        }
        public SeparatorCells asSeparator() {
            Cell first = get(0);
            if(first instanceof SeparatorCells) {
                return (SeparatorCells) first;
            }
            int count = size();
            List<Cell> childList = new ArrayList<>(count);
            for(int i = 0; i < count; i++) {
                childList.add(remove(0));
            }
            SeparatorCells separatorCells = new SeparatorCells(childList);
            add(separatorCells);
            return separatorCells;
        }

        public Cell get(int i) {
            if(i >= 0 && i < cellList.size()) {
                return cellList.get(i);
            }
            return null;
        }
        public Cell getLast() {
            return get(size() - 1);
        }
        private Cell getCell(int index) {
            int size = cellList.size();
            for(int i = 0; i < size; i ++) {
                Cell cell = cellList.get(i);
                if(i == index) {
                    if(cell instanceof MergedCells) {
                        return ((MergedCells)cell).get(0);
                    }
                    return cell;
                }
                if(cell instanceof MergedCells) {
                    MergedCells mergedCells = (MergedCells) cell;
                    cell = mergedCells.get(index + i);
                    if(cell != null) {
                        return cell;
                    }
                }
            }
            return null;
        }
        public Cell remove(int i) {
            if(i >= 0 && i < cellList.size()) {
                return cellList.remove(i);
            }
            return null;
        }

        public int size() {
            return cellList.size();
        }
        public int getColumnCount() {
            int result = 0;
            for(Cell cell : this) {
                result += cell.getCellCount();
            }
            return result;
        }
        public int getPrintWidth() {
            int result = getTab().length();
            for(Cell cell : this) {
                result += cell.getPrintWidth();
            }
            return result;
        }
        public int getMergedCellsWidth() {
            for (Cell cell : this) {
                if(cell instanceof MergedCells) {
                    MergedCells mergedCells = (MergedCells) cell;
                    Cell first = mergedCells.getFirst();
                    if (first != null) {
                        return first.itemWidth();
                    }
                }
            }
            return 0;
        }
        public int getColumnPrintWidth(int columnIndex) {
            Cell cell = getCell(columnIndex);
            if(cell != null) {
                return cell.getPrintWidth();
            }
            return 0;
        }

        public void ensureSize(int size) {
            int remaining = size - getColumnCount();
            if (remaining <= 0) {
                return;
            }
            Cell last = getLast();
            if (last instanceof MergedCells) {
                ((MergedCells) last).addEmptyCells(remaining);
            } else {
                for (int i = 0; i < remaining; i++) {
                    add(new Cell(""));
                }
            }
        }

        public int itemWidth(int index) {
            Cell cell = get(index);
            return cell == null ? 0 : cell.itemWidth();
        }

        public void setIndent(int columnIndex, int indent) {
            Cell cell = get(columnIndex);
            if (cell != null) {
                cell.setHorizontalIndent(indent);
            }
        }

        public void setColumnWidth(int columnIndex, int columnWidth) {
            Cell cell = getCell(columnIndex);
            if (cell != null) {
                cell.setColumnWidth(columnWidth);
            }
        }

        public void setBorderLeft(int columnIndex, String border) {
            Cell cell = get(columnIndex);
            if (cell != null) {
                cell.setBorderLeft(border);
            }
        }

        public void setBorderRight(int columnIndex, String border) {
            Cell cell = get(columnIndex);
            if (cell != null) {
                cell.setBorderRight(border);
            }
        }

        public List<Row> getSplitLines() {
            List<Row> results = new ArrayList<>();
            int splitLinesSize = getSplitLinesSize();
            setSplitLinesSize(splitLinesSize);
            int column = size();
            for (int line = 0; line < splitLinesSize; line++) {
                Row row = new Row();
                for (int i = 0; i < column; i++) {
                    Cell parent = get(i);
                    row.add(parent.getSplitLine(line));
                }
                results.add(row);
            }
            return results;
        }

        private void setSplitLinesSize(int size) {
            for (Cell cell : this) {
                cell.setSplitLinesSize(size);
            }
        }

        private int getSplitLinesSize() {
            int result = 0;
            for (Cell cell : this) {
                int width = cell.getSplitLinesSize();
                if (width > result) {
                    result = width;
                }
            }
            return result;
        }

        @Override
        public Iterator<Cell> iterator() {
            return cellList.iterator();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(getTab());
            int size = size();
            for (int i = 0; i < size; i++) {
                builder.append(get(i));
            }
            return builder.toString();
        }
    }

    public static class Cell {

        private Object item;
        private String borderLeft;
        private String borderRight;
        private int columnWidth;
        private int horizontalIndent;
        private int verticalIndent;
        private List<Cell> splitLines;

        public Cell(Object item) {
            this.item = item;
            this.borderLeft = "";
            this.borderRight = "  ";
            this.horizontalIndent = SpreadSheet.INDENT_LEFT;
            this.verticalIndent = SpreadSheet.INDENT_TOP;
        }

        public Cell() {
            this(null);
        }

        public Object getItem() {
            return item;
        }
        public void setItem(Object item) {
            this.item = item;
        }
        public Cell copyOf(Object item) {
            Cell cell = new Cell(item);
            cell.setHorizontalIndent(getHorizontalIndent());
            cell.setVerticalIndent(getVerticalIndent());
            cell.setColumnWidth(getColumnWidth());
            cell.setBorderLeft(getBorderLeft());
            cell.setBorderRight(getBorderRight());
            return cell;
        }

        public int getCellCount() {
            return 1;
        }
        public String getBorderLeft() {
            return borderLeft;
        }

        public void setBorderLeft(String borderLeft) {
            this.borderLeft = borderLeft;
        }

        public String getBorderRight() {
            return borderRight;
        }

        public void setBorderRight(String borderRight) {
            this.borderRight = borderRight;
        }

        public String getItemString() {
            Object obj = getItem();
            return obj == null ? "" : obj.toString();
        }

        public int getPrintWidth() {
            return getBorderLeft().length() + getColumnWidth() + getBorderRight().length();
        }
        public int itemWidth() {
            return widthOf(getItemString());
        }
        int widthOf(String str) {
            int length = str.length();
            int result = 0;
            int current = 0;
            for(int i = 0; i < length; i++) {
                char ch = str.charAt(i);
                current += widthOf(ch);
                if(str.charAt(i) == '\n') {
                    if(current > result) {
                        result = current;
                    }
                    current = 0;
                }
            }
            if(current > result) {
                result = current;
            }
            return result;
        }
        private int widthOf(char ch) {
            if(ch == '\n' || ch == '\r') {
                return 0;
            }
            return 1;
        }

        public int getHorizontalIndent() {
            return horizontalIndent;
        }

        public void setHorizontalIndent(int horizontalIndent) {
            if (horizontalIndent != this.horizontalIndent) {
                if (horizontalIndent < SpreadSheet.INDENT_LEFT || horizontalIndent > SpreadSheet.INDENT_RIGHT) {
                    throw new IllegalArgumentException("Invalid horizontal indent: '" + horizontalIndent + "', should be bn "
                            + SpreadSheet.INDENT_LEFT + "~" + SpreadSheet.INDENT_RIGHT);
                }
                this.horizontalIndent = horizontalIndent;
                this.splitLines = null;
            }
        }

        public int getVerticalIndent() {
            return verticalIndent;
        }

        public void setVerticalIndent(int verticalIndent) {
            if (verticalIndent != this.verticalIndent) {
                if (verticalIndent < SpreadSheet.INDENT_TOP || verticalIndent > SpreadSheet.INDENT_BOTTOM) {
                    throw new IllegalArgumentException("Invalid vertical indent: '" + verticalIndent + "', should be bn "
                            + SpreadSheet.INDENT_TOP + "~" + SpreadSheet.INDENT_BOTTOM);
                }
                this.verticalIndent = verticalIndent;
                this.splitLines = null;
            }
        }

        public int getColumnWidth() {
            int width = this.columnWidth;
            if (width == 0) {
                width = itemWidth();
            }
            return width;
        }

        public void setColumnWidth(int columnWidth) {
            if (columnWidth != this.columnWidth) {
                this.columnWidth = columnWidth;
                this.splitLines = null;
            }
        }

        public Cell getSplitLine(int index) {
            return getSplitLines().get(index);
        }

        public int getSplitLinesSize() {
            return getSplitLines().size();
        }

        public void setSplitLinesSize(int size) {
            List<Cell> splitLines = this.getSplitLines();
            String filler = fillSpace(getColumnWidth());
            int remaining = size - splitLines.size();
            int indent = getVerticalIndent();

            int topHalf;
            int bottomHalf;

            if (indent == SpreadSheet.INDENT_BOTTOM) {
                topHalf = remaining;
                bottomHalf = 0;
            } else if (indent == SpreadSheet.INDENT_MIDDLE) {
                topHalf = remaining / 2;
                bottomHalf = remaining - topHalf;
            } else {
                topHalf = 0;
                bottomHalf = remaining;
            }

            for (int i = 0; i < topHalf; i++) {
                splitLines.add(0, copyOf(filler));
            }
            for (int i = 0; i < bottomHalf; i++) {
                splitLines.add(copyOf(filler));
            }
        }

        public List<Cell> getSplitLines() {
            List<Cell> splitLines = this.splitLines;
            if (splitLines == null) {
                splitLines = splitLines();
                this.splitLines = splitLines;
            }
            return splitLines;
        }

        private List<Cell> splitLines() {
            int width = getColumnWidth();
            String[] lines = SpreadSheet.splitNewLineOrWidth(width, getItemString());
            List<Cell> results = new ArrayList<>(lines.length);
            for (String line : lines) {
                line = indentHorizontally(width, line);
                results.add(copyOf(line));
            }
            return results;
        }

        String indentHorizontally(int width, String line) {
            int remaining = width - line.length();
            if (remaining == 0) {
                return line;
            }

            int indent = getHorizontalIndent();

            int leftHalf;
            int rightHalf;

            if (indent == SpreadSheet.INDENT_RIGHT) {
                leftHalf = remaining;
                rightHalf = 0;
            } else if (indent == SpreadSheet.INDENT_CENTER) {
                leftHalf = remaining / 2;
                rightHalf = remaining - leftHalf;
            } else {
                leftHalf = 0;
                rightHalf = remaining;
            }

            StringBuilder builder = new StringBuilder();
            SpreadSheet.fillSpace(builder, leftHalf);
            builder.append(line);
            SpreadSheet.fillSpace(builder, rightHalf);
            return builder.toString();
        }

        @Override
        public String toString() {
            return getBorderLeft() + getItemString() + getBorderRight();
        }
    }
    public static class MergedCells extends Cell implements Iterable<Cell> {

        private final List<Cell> childCells;

        public MergedCells(List<Cell> childCells) {
            this.childCells = childCells;
        }

        @Override
        public int itemWidth() {
            return 0;
        }

        @Override
        String indentHorizontally(int width, String line) {
            return super.indentHorizontally(width, line);
        }

        @Override
        public MergedCells copyOf(Object item) {
            List<Cell> list = new ArrayList<>();
            Cell first = getFirst();
            if(first != null) {
                list.add(first.copyOf(item));
                int size = size();
                for(int i = 1; i < size; i++) {
                    Cell cell = get(i);
                    list.add(cell.copyOf(""));
                }
            }
            MergedCells mergedCells = createNew(list);
            mergedCells.setHorizontalIndent(getHorizontalIndent());
            mergedCells.setVerticalIndent(getVerticalIndent());
            mergedCells.setBorderLeft(getBorderLeft());
            mergedCells.setBorderRight(getBorderRight());
            return mergedCells;
        }
        MergedCells createNew(List<Cell> list) {
            return new MergedCells(list);
        }

        @Override
        public int getCellCount() {
            return this.size();
        }

        public Cell get(int i) {
            if(i >= 0 && i < childCells.size()) {
                return childCells.get(i);
            }
            return null;
        }
        public void add(Cell cell) {
            childCells.add(cell);
        }
        public int size() {
            return childCells.size();
        }
        public Cell getFirst() {
            return get(0);
        }
        @Override
        public Object getItem() {
            Cell cell = getFirst();
            if(cell != null) {
                return cell.getItem();
            }
            return null;
        }
        @Override
        public void setItem(Object item) {
            Cell cell = getFirst();
            if(cell != null) {
                cell.setItem(item);
            }
        }

        void addEmptyCells(int amount) {
            for(int i = 0; i < amount; i++) {
                add(new Cell(""));
            }
        }
        @Override
        public int getColumnWidth() {
            int result = 0;
            for(Cell cell : this) {
                result += cell.getPrintWidth();
            }
            result -= getBorderLeft().length();
            result -= getBorderRight().length();
            if(result < 0) {
                result = 0;
            }
            return result;
        }

        @Override
        public Iterator<Cell> iterator() {
            return childCells.iterator();
        }
    }

    public static class SeparatorCells extends MergedCells{

        private char mChar = '_';

        public SeparatorCells(List<Cell> childCells) {
            super(childCells);
        }

        public char getChar() {
            return mChar;
        }

        public void setChar(char mChar) {
            this.mChar = mChar;
        }

        @Override
        public MergedCells copyOf(Object item) {
            SeparatorCells separatorCells = (SeparatorCells) super.copyOf(item);
            separatorCells.setChar(getChar());
            return separatorCells;
        }

        @Override
        SeparatorCells createNew(List<Cell> list) {
            return new SeparatorCells(list);
        }

        @Override
        public String toString() {
            return getBorderLeft() + SpreadSheet.fill(getChar(), getColumnWidth()) + getBorderRight();
        }
    }


    public static String[] splitNewLineOrWidth(int width, String line) {
        int count = 0;
        int length = line.length();
        int currentWidth = 0;
        if (width == length) {
            currentWidth = -length;
        }
        StringBuilder lineBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char ch = line.charAt(i);
            lineBuilder.append(ch);
            currentWidth ++;
            if(ch == '\n' || currentWidth == width) {
                count ++;
                currentWidth = 0;
                if(ch != '\n') {
                    currentWidth = placeNewline(lineBuilder, width);
                }
            }
        }
        line = lineBuilder.toString();
        length = line.length();
        String[] results = new String[count + 1];
        int index = 0;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char ch = line.charAt(i);
            if(ch == '\n') {
                results[index] = builder.toString();
                builder = new StringBuilder();
                index ++;
            } else {
                builder.append(ch);
            }
        }
        if(index < results.length) {
            results[index] = builder.toString();
        }
        return results;
    }
    private static int placeNewline(StringBuilder builder, int width) {
        int length = builder.length();
        int half = width / 2;
        if(half >= length) {
            return 0;
        }
        half = length - half;
        for (int i = length - 1; i > half; i--) {
            char ch = builder.charAt(i);
            if (ch == '\n') {
                return 0;
            }
            if (ch == ' ' || ch == '\t') {
                builder.deleteCharAt(i);
                builder.insert(i, '\n');
                return length - i;
            }
        }
        builder.append('\n');
        return 0;
    }
    public static String fillSpace(int amount) {
        return fill(' ', amount);
    }
    public static void fillSpace(StringBuilder builder, int amount) {
        fill(builder, ' ', amount);
    }
    public static String fill(char ch, int amount) {
        StringBuilder builder = new StringBuilder(amount);
        fill(builder, ch, amount);
        return builder.toString();
    }
    public static void fill(StringBuilder builder, char ch, int amount) {
        if(ch != 0) {
            for(int i = 0; i < amount; i++) {
                builder.append(ch);
            }
        }
    }

    public static final int INDENT_LEFT = 0;
    public static final int INDENT_CENTER = 1;
    public static final int INDENT_RIGHT = 2;

    public static final int INDENT_TOP = 0;
    public static final int INDENT_MIDDLE = 1;
    public static final int INDENT_BOTTOM = 2;
}
