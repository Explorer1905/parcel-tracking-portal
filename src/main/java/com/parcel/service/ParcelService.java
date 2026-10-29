package com.parcel.service;

import com.parcel.dao.ParcelDao;
import com.parcel.model.Parcel;
import com.parcel.model.ParcelEvent;
import com.parcel.model.DashboardSummary;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParcelService {
    private final ParcelDao dao;

    /** Production constructor – uses the real database pool. */
    public ParcelService() {
        this(new ParcelDao());
    }

    /** Injection constructor for unit tests – accepts any DAO (including a fake). */
    public ParcelService(ParcelDao dao) {
        this.dao = dao;
    }

    // -----------------------------------------------------------------
    // Parcel registration
    // -----------------------------------------------------------------

    /** Original 5-argument form – weight=1.0 kg, priority=Standard, no date, no agent. */
    public void addParcel(String id, String sender, String receiver,
                          String origin, String destination) {
        addParcel(id, sender, receiver, origin, destination, "", "Standard", "", "");
    }

    /**
     * Full 9-argument form. All validation runs BEFORE any DAO call so that
     * unit tests need no database connection.
     *
     * @param weight   blank → 1.0; otherwise decimal ≥ 0.01 and ≤ 1000
     * @param priority blank → "Standard"; must be Standard / Express / Urgent
     * @param expectedDelivery blank → null; otherwise ISO yyyy-MM-dd, not before today
     * @param agent    at most 100 characters
     */
    public void addParcel(String id, String sender, String receiver,
                          String origin, String destination,
                          String weight, String priority, String expectedDelivery, String agent) {
        // --- clean inputs ---
        id          = clean(id);
        sender      = clean(sender);
        receiver    = clean(receiver);
        origin      = clean(origin);
        destination = clean(destination);
        weight      = clean(weight);
        priority    = clean(priority);
        expectedDelivery = clean(expectedDelivery);
        agent       = clean(agent);

        // --- mandatory field validation ---
        if (id.isEmpty() || sender.isEmpty() || receiver.isEmpty()
                || origin.isEmpty() || destination.isEmpty()) {
            throw new IllegalArgumentException("All fields are required.");
        }
        if (!id.matches("[A-Za-z0-9-]{3,30}")) {
            throw new IllegalArgumentException(
                    "Parcel ID must be 3 to 30 characters: letters, digits or hyphen.");
        }
        if (sender.length() > 100 || receiver.length() > 100
                || origin.length() > 100 || destination.length() > 100) {
            throw new IllegalArgumentException(
                    "Sender, receiver, origin and destination must be at most 100 characters.");
        }

        // --- weight ---
        double weightKg;
        if (weight.isEmpty()) {
            weightKg = 1.0;
        } else {
            try {
                weightKg = new BigDecimal(weight)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Weight must be a number (e.g. 2.5).");
            }
            if (weightKg < 0.01 || weightKg > 1000) {
                throw new IllegalArgumentException("Weight must be between 0.01 and 1000 kg.");
            }
        }

        // --- priority ---
        if (priority.isEmpty()) {
            priority = "Standard";
        }
        if (!List.of("Standard", "Express", "Urgent").contains(priority)) {
            throw new IllegalArgumentException("Priority must be Standard, Express or Urgent.");
        }

        // --- expected delivery date ---
        String parsedDate = null;
        if (!expectedDelivery.isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(expectedDelivery);
                if (date.isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException(
                            "Expected delivery date cannot be in the past.");
                }
                parsedDate = date.toString();
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "Expected delivery must be a date (yyyy-MM-dd).");
            }
        }

        // --- agent ---
        if (agent.length() > 100) {
            throw new IllegalArgumentException("Assigned agent name must be at most 100 characters.");
        }

        // --- duplicate check (DAO call happens AFTER all validation) ---
        if (dao.exists(id)) {
            throw new IllegalArgumentException("Parcel ID " + id + " already exists.");
        }

        dao.add(new Parcel(id, sender, receiver, origin, destination, "Registered", null,
                weightKg, priority, parsedDate, agent.isEmpty() ? null : agent));
    }

    // -----------------------------------------------------------------
    // Queries
    // -----------------------------------------------------------------

    public List<Parcel> listParcels() {
        return dao.findAll();
    }

    public List<Parcel> search(String query) {
        return dao.find(query);
    }

    public List<Parcel> search(String query, String status) {
        List<Parcel> results = dao.find(query);
        if (status == null || status.isBlank()) {
            return results;
        }
        return results.stream().filter(parcel -> status.equals(parcel.status())).toList();
    }

    public List<Parcel> findPage(String query, int offset, int limit) {
        return dao.findPage(query, offset, limit);
    }

    public int countFind(String query) {
        return dao.countFind(query);
    }

    public Parcel find(String id) {
        return id == null || id.isBlank() ? null : dao.findById(id.trim());
    }

    public List<ParcelEvent> events(String id) {
        return dao.eventsFor(id);
    }

    public DashboardSummary summary() {
        return dao.summary();
    }

    public List<Parcel> alerts() {
        return dao.alerts();
    }

    // -----------------------------------------------------------------
    // Event entry
    // -----------------------------------------------------------------

    public void addEvent(String id, String status, String location, String eventTime, String remarks) {
        id       = clean(id);
        status   = clean(status);
        location = clean(location);
        remarks  = clean(remarks);
        if (id.isEmpty() || status.isEmpty()) {
            throw new IllegalArgumentException("Parcel and status are required.");
        }
        if (location.length() > 100) {
            throw new IllegalArgumentException("Location must be at most 100 characters.");
        }
        if (remarks.length() > 200) {
            throw new IllegalArgumentException("Remarks must be at most 200 characters.");
        }
        if (find(id) == null) {
            throw new IllegalArgumentException("Parcel " + id + " was not found.");
        }
        if (!List.of("Picked Up", "In Transit", "Out for Delivery", "Delivered", "Failed")
                .contains(status)) {
            throw new IllegalArgumentException("Choose a valid status.");
        }
        Timestamp timestamp;
        try {
            timestamp = eventTime == null || eventTime.isBlank()
                    ? new Timestamp(System.currentTimeMillis())
                    : Timestamp.valueOf(LocalDateTime.parse(eventTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Event time must use YYYY-MM-DDTHH:MM.");
        }
        dao.addEvent(id, status, location, timestamp, remarks);
    }

    // -----------------------------------------------------------------
    // Utility
    // -----------------------------------------------------------------

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }
}