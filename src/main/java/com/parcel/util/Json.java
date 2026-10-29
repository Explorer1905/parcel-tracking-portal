package com.parcel.util;

/**
 * Minimal hand-written JSON builder.
 * Produces well-formed JSON with proper string escaping (no external library needed).
 */
public final class Json {

    private Json() {}

    /** Escape a Java string for inclusion inside a JSON string literal. */
    public static String esc(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder(s.length() + 4);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /** Render a nullable string as a JSON value: quoted and escaped, or "null". */
    public static String val(String s) {
        return s == null ? "null" : esc(s);
    }

    /** Render a nullable String that holds a date or number as a JSON value. */
    public static String numOrNull(String s) {
        return s == null || s.isBlank() ? "null" : s;
    }

    /** CSV: escape a cell value (wrap in quotes if it contains comma, quote or newline). */
    public static String csvCell(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
