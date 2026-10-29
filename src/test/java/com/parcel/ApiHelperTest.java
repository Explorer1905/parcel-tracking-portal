package com.parcel;

import com.parcel.controller.ApiParcelServlet;
import com.parcel.util.Json;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Json/CSV helpers and ApiParcelServlet.jsonField().
 * No database or servlet container needed.
 */
class ApiHelperTest {

    // -----------------------------------------------------------
    // Json.esc
    // -----------------------------------------------------------

    @Test
    @DisplayName("Json.esc escapes double quote")
    void escDoubleQuote() {
        assertEquals("\"say \\\"hi\\\"\"", Json.esc("say \"hi\""));
    }

    @Test
    @DisplayName("Json.esc escapes backslash")
    void escBackslash() {
        assertEquals("\"a\\\\b\"", Json.esc("a\\b"));
    }

    @Test
    @DisplayName("Json.esc escapes newline")
    void escNewline() {
        assertEquals("\"line1\\nline2\"", Json.esc("line1\nline2"));
    }

    @Test
    @DisplayName("Json.esc handles null")
    void escNull() {
        assertEquals("null", Json.esc(null));
    }

    @Test
    @DisplayName("Json.esc handles plain string unchanged")
    void escPlain() {
        assertEquals("\"hello\"", Json.esc("hello"));
    }

    @Test
    @DisplayName("Json.esc escapes tab")
    void escTab() {
        assertEquals("\"a\\tb\"", Json.esc("a\tb"));
    }

    // -----------------------------------------------------------
    // Json.val
    // -----------------------------------------------------------

    @Test
    @DisplayName("Json.val returns null literal for null input")
    void valNull() {
        assertEquals("null", Json.val(null));
    }

    @Test
    @DisplayName("Json.val wraps non-null string")
    void valNonNull() {
        assertEquals("\"Mumbai\"", Json.val("Mumbai"));
    }

    // -----------------------------------------------------------
    // Json.csvCell
    // -----------------------------------------------------------

    @Test
    @DisplayName("csvCell: plain value returned unchanged")
    void csvCellPlain() {
        assertEquals("Pune", Json.csvCell("Pune"));
    }

    @Test
    @DisplayName("csvCell: comma triggers quoting")
    void csvCellComma() {
        assertEquals("\"Delhi, NCR\"", Json.csvCell("Delhi, NCR"));
    }

    @Test
    @DisplayName("csvCell: embedded quote is doubled")
    void csvCellQuote() {
        assertEquals("\"say \"\"hi\"\"\"", Json.csvCell("say \"hi\""));
    }

    @Test
    @DisplayName("csvCell: null returns empty string")
    void csvCellNull() {
        assertEquals("", Json.csvCell(null));
    }

    // -----------------------------------------------------------
    // ApiParcelServlet.jsonField
    // -----------------------------------------------------------

    @Test
    @DisplayName("jsonField extracts simple string value")
    void jsonFieldSimple() {
        String body = "{\"id\":\"PKG-001\",\"sender\":\"Alice\"}";
        assertEquals("PKG-001", ApiParcelServlet.jsonField(body, "id"));
        assertEquals("Alice",   ApiParcelServlet.jsonField(body, "sender"));
    }

    @Test
    @DisplayName("jsonField returns empty string for missing key")
    void jsonFieldMissing() {
        assertEquals("", ApiParcelServlet.jsonField("{\"id\":\"PKG-001\"}", "status"));
    }

    @Test
    @DisplayName("jsonField unescapes backslash-quote in value")
    void jsonFieldEscapedQuote() {
        String body = "{\"sender\":\"Alice \\\"A\\\" Smith\"}";
        assertEquals("Alice \"A\" Smith", ApiParcelServlet.jsonField(body, "sender"));
    }

    @Test
    @DisplayName("jsonField handles null body")
    void jsonFieldNullBody() {
        assertEquals("", ApiParcelServlet.jsonField(null, "id"));
    }
}
