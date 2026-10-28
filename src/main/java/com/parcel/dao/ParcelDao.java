package com.parcel.dao;

import com.parcel.model.DashboardSummary;
import com.parcel.model.Parcel;
import com.parcel.model.ParcelEvent;
import com.parcel.util.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for parcels and tracking events (PostgreSQL, JDBC).
 * The schema is created by Flyway migrations (src/main/resources/db/migration).
 */
public class ParcelDao {
    private static final String UNIQUE_VIOLATION = "23505";
    private static final String FOREIGN_KEY_VIOLATION = "23503";
    private static final String CHECK_VIOLATION = "23514";

    /** Joins each parcel to its most recent event (latest time, then latest insert). */
    private static final String LATEST_EVENT_JOIN =
            "JOIN events latest ON latest.event_id = (SELECT e2.event_id FROM events e2 "
                    + "WHERE e2.parcel_id = p.parcel_id "
                    + "ORDER BY e2.event_time DESC, e2.event_id DESC LIMIT 1) ";

    /** Alert rule: the latest status is Failed, or not Delivered and silent for over 48 hours. */
    private static final String DELAYED_RULE =
            "(latest.status = 'Failed' OR (latest.status <> 'Delivered' "
                    + "AND latest.event_time < LOCALTIMESTAMP - INTERVAL '48 hours'))";

    public boolean exists(String id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM parcels WHERE parcel_id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void add(Parcel p) {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO parcels (parcel_id, sender, receiver, origin, destination) VALUES (?,?,?,?,?)");
                 PreparedStatement pe = c.prepareStatement(
                         "INSERT INTO events (parcel_id, status, location, remarks) VALUES (?,?,?,?)")) {
                ps.setString(1, p.id());
                ps.setString(2, p.sender());
                ps.setString(3, p.receiver());
                ps.setString(4, p.origin());
                ps.setString(5, p.destination());
                ps.executeUpdate();
                pe.setString(1, p.id());
                pe.setString(2, "Registered");
                pe.setString(3, p.origin());
                pe.setString(4, "Parcel registered");
                pe.executeUpdate();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                // Two simultaneous requests can both pass exists(); the primary key decides.
                throw new IllegalArgumentException("Parcel ID " + p.id() + " already exists.");
            }
            throw new IllegalStateException(e);
        }
    }

    public List<Parcel> findAll() {
        return find("");
    }

    public List<Parcel> find(String query) {
        String sql = "SELECT p.parcel_id, p.sender, p.receiver, p.origin, p.destination, p.created_at, "
                + "(SELECT e.status FROM events e WHERE e.parcel_id = p.parcel_id "
                + "ORDER BY e.event_time DESC, e.event_id DESC LIMIT 1) AS status "
                + "FROM parcels p WHERE LOWER(p.parcel_id) LIKE ? OR LOWER(p.sender) LIKE ? "
                + "OR LOWER(p.receiver) LIKE ? OR LOWER(p.origin) LIKE ? OR LOWER(p.destination) LIKE ? "
                + "ORDER BY p.created_at DESC, p.parcel_id";
        String pattern = "%" + (query == null ? "" : query.trim().toLowerCase()) + "%";
        List<Parcel> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, pattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(parcel(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return list;
    }

    public Parcel findById(String id) {
        String sql = "SELECT p.parcel_id, p.sender, p.receiver, p.origin, p.destination, p.created_at, "
                + "(SELECT e.status FROM events e WHERE e.parcel_id = p.parcel_id "
                + "ORDER BY e.event_time DESC, e.event_id DESC LIMIT 1) AS status "
                + "FROM parcels p WHERE LOWER(p.parcel_id) = LOWER(?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? parcel(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<ParcelEvent> eventsFor(String parcelId) {
        List<ParcelEvent> events = new ArrayList<>();
        String sql = "SELECT event_id, parcel_id, status, location, event_time, remarks "
                + "FROM events WHERE parcel_id = ? ORDER BY event_time DESC, event_id DESC";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, parcelId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(new ParcelEvent(rs.getLong("event_id"), rs.getString("parcel_id"),
                            rs.getString("status"), rs.getString("location"),
                            rs.getString("event_time"), rs.getString("remarks"), false));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return events;
    }

    public void addEvent(String parcelId, String status, String location,
                         Timestamp eventTime, String remarks) {
        String sql = "INSERT INTO events (parcel_id, status, location, event_time, remarks) VALUES (?,?,?,?,?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, parcelId);
            ps.setString(2, status);
            ps.setString(3, location);
            ps.setTimestamp(4, eventTime);
            ps.setString(5, remarks);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (FOREIGN_KEY_VIOLATION.equals(e.getSQLState())) {
                throw new IllegalArgumentException("Parcel " + parcelId + " was not found.");
            }
            if (CHECK_VIOLATION.equals(e.getSQLState())) {
                throw new IllegalArgumentException("Choose a valid status.");
            }
            throw new IllegalStateException(e);
        }
    }

    public DashboardSummary summary() {
        String sql = "SELECT COUNT(*) AS total, "
                + "COALESCE(SUM(CASE WHEN latest.status = 'In Transit' THEN 1 ELSE 0 END), 0) AS in_transit, "
                + "COALESCE(SUM(CASE WHEN latest.status = 'Delivered' THEN 1 ELSE 0 END), 0) AS delivered, "
                + "COALESCE(SUM(CASE WHEN " + DELAYED_RULE + " THEN 1 ELSE 0 END), 0) AS delayed "
                + "FROM parcels p " + LATEST_EVENT_JOIN;
        try (Connection c = Database.getConnection(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            rs.next();
            return new DashboardSummary(rs.getInt("total"), rs.getInt("in_transit"),
                    rs.getInt("delivered"), rs.getInt("delayed"));
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<Parcel> alerts() {
        String sql = "SELECT p.parcel_id, p.sender, p.receiver, p.origin, p.destination, p.created_at, "
                + "latest.status AS status FROM parcels p " + LATEST_EVENT_JOIN
                + "WHERE " + DELAYED_RULE + " ORDER BY p.created_at DESC, p.parcel_id";
        List<Parcel> result = new ArrayList<>();
        try (Connection c = Database.getConnection(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                result.add(parcel(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    private static Parcel parcel(ResultSet rs) throws SQLException {
        return new Parcel(rs.getString("parcel_id"), rs.getString("sender"),
                rs.getString("receiver"), rs.getString("origin"),
                rs.getString("destination"), rs.getString("status"),
                rs.getString("created_at"));
    }
}
