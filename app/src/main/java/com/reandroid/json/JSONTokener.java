/*
 * Copyright (c) 2002 JSON.org (now "Public Domain")
 * This is NOT property of REAndroid
 * This package is renamed from org.json.* to avoid class conflict when used on android platforms
*/
package com.reandroid.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

public class JSONTokener {
    /** current read character position on the current line. */
    private long character;
    /** flag to indicate if the end of the input has been found. */
    private boolean eof;
    /** current read index of the input. */
    private long index;
    /** current line of the input. */
    private long line;
    /** previous character read from the input. */
    private char previous;
    /** Reader for the input. */
    private final Reader reader;
    /** flag to indicate that a previous character was requested. */
    private boolean usePrevious;
    /** the number of characters read in the previous line. */
    private long characterPreviousLine;
    public JSONTokener(Reader reader) {
        this.reader = reader.markSupported()
                ? reader
                        : new BufferedReader(reader);
        this.eof = false;
        this.usePrevious = false;
        this.previous = 0;
        this.index = 0;
        this.character = 1;
        this.characterPreviousLine = 0;
        this.line = 1;
    }
    public JSONTokener(InputStream inputStream) {
        this(new InputStreamReader(inputStream));
    }
    public JSONTokener(String s) {
        this(new StringReader(s));
    }
    public void back() throws JSONException {
        if (this.usePrevious || this.index <= 0) {
            throw new JSONException("Stepping back two steps is not supported");
        }
        this.decrementIndexes();
        this.usePrevious = true;
        this.eof = false;
    }

    private void decrementIndexes() {
        this.index--;
        if(this.previous=='\r' || this.previous == '\n') {
            this.line--;
            this.character=this.characterPreviousLine ;
        } else if(this.character > 0){
            this.character--;
        }
    }

    public static int dehexchar(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - ('A' - 10);
        }
        if (c >= 'a' && c <= 'f') {
            return c - ('a' - 10);
        }
        return -1;
    }

    public boolean end() {
        return this.eof && !this.usePrevious;
    }
    public boolean more() throws JSONException {
        if(this.usePrevious) {
            return true;
        }
        try {
            this.reader.mark(1);
        } catch (IOException e) {
            throw new JSONException("Unable to preserve stream position", e);
        }
        try {
            // -1 is EOF, but next() can not consume the null character '\0'
            if(this.reader.read() <= 0) {
                this.eof = true;
                return false;
            }
            this.reader.reset();
        } catch (IOException e) {
            throw new JSONException("Unable to read the next character from the stream", e);
        }
        return true;
    }
    public char next() throws JSONException {
        int c;
        if (this.usePrevious) {
            this.usePrevious = false;
            c = this.previous;
        } else {
            try {
                c = this.reader.read();
            } catch (IOException exception) {
                throw new JSONException(exception);
            }
        }
        if (c <= 0) { // End of stream
            this.eof = true;
            return 0;
        }
        this.incrementIndexes(c);
        this.previous = (char) c;
        return this.previous;
    }

    private void incrementIndexes(int c) {
        if(c > 0) {
            this.index++;
            if(c=='\r') {
                this.line++;
                this.characterPreviousLine = this.character;
                this.character=0;
            }else if (c=='\n') {
                if(this.previous != '\r') {
                    this.line++;
                    this.characterPreviousLine = this.character;
                }
                this.character=0;
            } else {
                this.character++;
            }
        }
    }

    public char next(char c) throws JSONException {
        char n = this.next();
        if (n != c) {
            if(n > 0) {
                throw this.syntaxError("Expected '" + c + "' and instead saw '" +
                        n + "'");
            }
            throw this.syntaxError("Expected '" + c + "' and instead saw ''");
        }
        return n;
    }
    public String next(int n) throws JSONException {
        if (n == 0) {
            return "";
        }

        char[] chars = new char[n];
        int pos = 0;

        while (pos < n) {
            chars[pos] = this.next();
            if (this.end()) {
                throw this.syntaxError("Substring bounds error");
            }
            pos += 1;
        }
        return new String(chars);
    }
    public char nextClean() throws JSONException {
        for (;;) {
            char c = this.next();
            if (c == 0 || c > ' ') {
                return c;
            }
        }
    }
    public String nextString(char quote) throws JSONException {
        char c;
        StringBuilder sb = new StringBuilder();
        for (;;) {
            c = this.next();
            switch (c) {
            case 0:
            case '\n':
            case '\r':
                throw this.syntaxError("Unterminated string");
            case '\\':
                c = this.next();
                switch (c) {
                case 'b':
                    sb.append('\b');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 'u':
                    try {
                        sb.append((char)Integer.parseInt(this.next(4), 16));
                    } catch (NumberFormatException e) {
                        throw this.syntaxError("Illegal escape.", e);
                    }
                    break;
                case '"':
                case '\'':
                case '\\':
                case '/':
                    sb.append(c);
                    break;
                default:
                    throw this.syntaxError("Illegal escape.");
                }
                break;
            default:
                if (c == quote) {
                    return sb.toString();
                }
                sb.append(c);
            }
        }
    }
    public String nextTo(char delimiter) throws JSONException {
        StringBuilder sb = new StringBuilder();
        for (;;) {
            char c = this.next();
            if (c == delimiter || c == 0 || c == '\n' || c == '\r') {
                if (c != 0) {
                    this.back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }
    public String nextTo(String delimiters) throws JSONException {
        char c;
        StringBuilder sb = new StringBuilder();
        for (;;) {
            c = this.next();
            if (delimiters.indexOf(c) >= 0 || c == 0 ||
                    c == '\n' || c == '\r') {
                if (c != 0) {
                    this.back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }
    public Object nextValue() throws JSONException {
        char c = this.nextClean();
        String string;

        switch (c) {
        case '"':
        case '\'':
            string = this.nextString(c);
            byte[] bytes = JsonUtil.parseBase64(string);
            if(bytes == null){
                return string;
            }
            return bytes;
        case '{':
            this.back();
            return new JSONObject(this);
        case '[':
            this.back();
            return new JSONArray(this);
        }

        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */

        StringBuilder sb = new StringBuilder();
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c);
            c = this.next();
        }
        if (!this.eof) {
            this.back();
        }

        string = sb.toString().trim();
        if ("".equals(string)) {
            throw this.syntaxError("Missing value");
        }
        return JSONObject.stringToValue(string);
    }
    public char skipTo(char to) throws JSONException {
        char c;
        try {
            long startIndex = this.index;
            long startCharacter = this.character;
            long startLine = this.line;
            this.reader.mark(1000000);
            do {
                c = this.next();
                if (c == 0) {
                    // in some readers, reset() may throw an exception if
                    // the remaining portion of the input is greater than
                    // the mark size (1,000,000 above).
                    this.reader.reset();
                    this.index = startIndex;
                    this.character = startCharacter;
                    this.line = startLine;
                    return 0;
                }
            } while (c != to);
            this.reader.mark(1);
        } catch (IOException exception) {
            throw new JSONException(exception);
        }
        this.back();
        return c;
    }

    public JSONException syntaxError(String message) {
        return new JSONException(message + this.toString());
    }

    public JSONException syntaxError(String message, Throwable causedBy) {
        return new JSONException(message + this.toString(), causedBy);
    }

    @Override
    public String toString() {
        return " at " + this.index + " [character " + this.character + " line " +
                this.line + "]";
    }
}
