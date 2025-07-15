/*
 * Copyright (c) 2002 JSON.org (now "Public Domain")
 * This is NOT property of REAndroid
 * This package is renamed from org.json.* to avoid class conflict when used on android platforms
*/
package com.reandroid.json;

import android.os.Build;

import java.util.Locale;

public class HTTP {

    /** Carriage return/line feed. */
    public static final String CRLF = "\r\n";

    public static JSONObject toJSONObject(String string) throws JSONException {
        JSONObject jo = new JSONObject();
        HTTPTokener    x = new HTTPTokener(string);
        String         token;

        token = x.nextToken();
        if (token.toUpperCase(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? Locale.ROOT : Locale.getDefault()).startsWith("HTTP")) {

// Response

            jo.put("HTTP-Version", token);
            jo.put("Status-Code", x.nextToken());
            jo.put("Reason-Phrase", x.nextTo('\0'));
            x.next();

        } else {

// Request

            jo.put("Method", token);
            jo.put("Request-URI", x.nextToken());
            jo.put("HTTP-Version", x.nextToken());
        }

// Fields

        while (x.more()) {
            String name = x.nextTo(':');
            x.next(':');
            jo.put(name, x.nextTo('\0'));
            x.next();
        }
        return jo;
    }


    public static String toString(JSONObject jo) throws JSONException {
        StringBuilder       sb = new StringBuilder();
        if (jo.has("Status-Code") && jo.has("Reason-Phrase")) {
            sb.append(jo.getString("HTTP-Version"));
            sb.append(' ');
            sb.append(jo.getString("Status-Code"));
            sb.append(' ');
            sb.append(jo.getString("Reason-Phrase"));
        } else if (jo.has("Method") && jo.has("Request-URI")) {
            sb.append(jo.getString("Method"));
            sb.append(' ');
            sb.append('"');
            sb.append(jo.getString("Request-URI"));
            sb.append('"');
            sb.append(' ');
            sb.append(jo.getString("HTTP-Version"));
        } else {
            throw new JSONException("Not enough material for an HTTP header.");
        }
        sb.append(CRLF);
        // Don't use the new entrySet API to maintain Android support
        for (final String key : jo.keySet()) {
            String value = jo.optString(key);
            if (!"HTTP-Version".equals(key)      && !"Status-Code".equals(key) &&
                    !"Reason-Phrase".equals(key) && !"Method".equals(key) &&
                    !"Request-URI".equals(key)   && !JSONObject.NULL.equals(value)) {
                sb.append(key);
                sb.append(": ");
                sb.append(jo.optString(key));
                sb.append(CRLF);
            }
        }
        sb.append(CRLF);
        return sb.toString();
    }
}