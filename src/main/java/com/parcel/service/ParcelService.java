package com.parcel.service;

import com.parcel.dao.ParcelDao;
import com.parcel.model.Parcel;
import com.parcel.model.ParcelEvent;
import com.parcel.model.DashboardSummary;
import java.util.List;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParcelService {
    private final ParcelDao dao = new ParcelDao();

    public void addParcel(String id, String sender, String receiver, String origin, String destination) {
        id = clean(id);
        sender = clean(sender);
        receiver = clean(receiver);
        origin = clean(origin);
        destination = clean(destination);

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
        if (dao.exists(id)) {
            throw new IllegalArgumentException("Parcel ID " + id + " already exists.");
        }
        dao.add(new Parcel(id, sender, receiver, origin, destination, "Registered", null));
    }

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

    public void addEvent(String id, String status, String location, String eventTime, String remarks) {
        id = clean(id);
        status = clean(status);
        location = clean(location);
        remarks = clean(remarks);
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
        if (!List.of("Picked Up", "In Transit", "Out for Delivery", "Delivered", "Failed").contains(status)) {
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

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }
}