package com.parcel;

import com.parcel.dao.ParcelDao;
import com.parcel.model.DashboardSummary;
import com.parcel.model.Parcel;
import com.parcel.model.ParcelEvent;
import com.parcel.service.ParcelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ParcelService.addParcel validation.
 * Uses an in-memory fake DAO so no database connection is required.
 */
class ParcelServiceValidationTest {

    /** Minimal in-memory DAO that records added parcels and rejects duplicates. */
    static class FakeParcelDao extends ParcelDao {
        final List<Parcel> added = new ArrayList<>();

        @Override public boolean exists(String id) {
            return added.stream().anyMatch(p -> p.id().equalsIgnoreCase(id));
        }
        @Override public void add(Parcel p) { added.add(p); }
        @Override public List<Parcel> findAll() { return added; }
        @Override public Parcel findById(String id) {
            return added.stream().filter(p -> p.id().equalsIgnoreCase(id)).findFirst().orElse(null);
        }
        @Override public List<Parcel> find(String q) { return added; }
        @Override public List<ParcelEvent> eventsFor(String id) { return List.of(); }
        @Override public void addEvent(String pid, String status, String loc,
                                       Timestamp ts, String remarks) {}
        @Override public DashboardSummary summary() { return new DashboardSummary(0,0,0,0); }
        @Override public List<Parcel> alerts() { return List.of(); }
    }

    private FakeParcelDao fakeDao;
    private ParcelService service;

    @BeforeEach
    void setup() {
        fakeDao  = new FakeParcelDao();
        service  = new ParcelService(fakeDao);
    }

    // ---------------------------------------------------------------
    // Happy path
    // ---------------------------------------------------------------

    @Test
    @DisplayName("5-arg addParcel stores parcel with defaults")
    void fiveArgDefaults() {
        service.addParcel("PKG-001", "Alice", "Bob", "Mumbai", "Pune");
        Parcel p = fakeDao.added.get(0);
        assertEquals("PKG-001", p.id());
        assertEquals(1.0, p.weightKg(), 0.001);
        assertEquals("Standard", p.priority());
        assertNull(p.expectedDelivery());
        assertNull(p.assignedAgent());
    }

    @Test
    @DisplayName("9-arg addParcel stores all fields")
    void nineArgStoresAllFields() {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        service.addParcel("PKG-002", "Alice", "Bob", "Delhi", "Jaipur",
                "5.5", "Express", tomorrow, "Ravi Kumar");
        Parcel p = fakeDao.added.get(0);
        assertEquals(5.5, p.weightKg(), 0.001);
        assertEquals("Express", p.priority());
        assertEquals(tomorrow, p.expectedDelivery());
        assertEquals("Ravi Kumar", p.assignedAgent());
    }

    @Test
    @DisplayName("Blank optional fields use defaults")
    void blankOptionalsUseDefaults() {
        service.addParcel("PKG-003", "Alice", "Bob", "Delhi", "Jaipur",
                "", "", "", "");
        Parcel p = fakeDao.added.get(0);
        assertEquals(1.0, p.weightKg(), 0.001);
        assertEquals("Standard", p.priority());
        assertNull(p.expectedDelivery());
        assertNull(p.assignedAgent());
    }

    // ---------------------------------------------------------------
    // ID validation
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Empty ID rejected")
    void emptyIdRejected() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.addParcel("", "A", "B", "X", "Y"));
        assertTrue(ex.getMessage().contains("required"));
    }

    @Test
    @DisplayName("ID too short rejected")
    void idTooShortRejected() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.addParcel("AB", "A", "B", "X", "Y"));
        assertTrue(ex.getMessage().contains("3 to 30"));
    }

    @Test
    @DisplayName("ID with special chars rejected")
    void idSpecialCharsRejected() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.addParcel("PKG@001", "A", "B", "X", "Y"));
        assertTrue(ex.getMessage().contains("3 to 30"));
    }

    @Test
    @DisplayName("Duplicate ID rejected before DAO insert")
    void duplicateIdRejected() {
        service.addParcel("PKG-001", "A", "B", "X", "Y");
        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.addParcel("PKG-001", "C", "D", "P", "Q"));
        assertTrue(ex.getMessage().contains("already exists"));
        assertEquals(1, fakeDao.added.size()); // only one stored
    }

    // ---------------------------------------------------------------
    // Weight validation
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Non-numeric weight rejected")
    void nonNumericWeightRejected() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.addParcel("PKG-010", "A", "B", "X", "Y",
                        "abc", "", "", ""));
        assertTrue(ex.getMessage().contains("number"));
    }

    @Test
    @DisplayName("Weight zero rejected")
    void weightZeroRejected() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.addParcel("PKG-011", "A", "B", "X", "Y",
                        "0", "", "", ""));
        assertTrue(ex.getMessage().contains("0.01"));
    }

    @Test
    @DisplayName("Weight above 1000 rejected")
    void weightAbove1000Rejected() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.addParcel("PKG-012", "A", "B", "X", "Y",
                        "1001", "", "", ""));
        assertTrue(ex.getMessage().contains("1000"));
    }

    @Test
    @DisplayName("Weight rounded to 2 decimal places")
    void weightRounded() {
        service.addParcel("PKG-013", "A", "B", "X", "Y",
                "2.555", "", "", "");
        assertEquals(2.56, fakeDao.added.get(0).weightKg(), 0.001);
    }

    // ---------------------------------------------------------------
    // Priority validation
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Invalid priority rejected")
    void invalidPriorityRejected() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.addParcel("PKG-020", "A", "B", "X", "Y",
                        "", "Economy", "", ""));
        assertTrue(ex.getMessage().contains("Standard"));
    }

    // ---------------------------------------------------------------
    // Date validation
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Past date rejected")
    void pastDateRejected() {
        String yesterday = LocalDate.now().minusDays(1).toString();
        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.addParcel("PKG-030", "A", "B", "X", "Y",
                        "", "", yesterday, ""));
        assertTrue(ex.getMessage().contains("past"));
    }

    @Test
    @DisplayName("Bad date format rejected")
    void badDateFormatRejected() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.addParcel("PKG-031", "A", "B", "X", "Y",
                        "", "", "29-10-2026", ""));
        assertTrue(ex.getMessage().contains("yyyy-MM-dd"));
    }

    @Test
    @DisplayName("Today's date accepted")
    void todayDateAccepted() {
        String today = LocalDate.now().toString();
        assertDoesNotThrow(() -> service.addParcel("PKG-032", "A", "B", "X", "Y",
                "", "", today, ""));
    }

    // ---------------------------------------------------------------
    // Agent validation
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Agent name over 100 chars rejected")
    void agentTooLongRejected() {
        String longName = "A".repeat(101);
        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.addParcel("PKG-040", "A", "B", "X", "Y",
                        "", "", "", longName));
        assertTrue(ex.getMessage().contains("100"));
    }

    @Test
    @DisplayName("Agent exactly 100 chars accepted")
    void agentExactly100Accepted() {
        String name100 = "A".repeat(100);
        assertDoesNotThrow(() -> service.addParcel("PKG-041", "A", "B", "X", "Y",
                "", "", "", name100));
    }
}
