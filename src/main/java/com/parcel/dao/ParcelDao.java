package com.parcel.dao;

import com.parcel.model.Parcel;
import com.parcel.model.ParcelEvent;
import com.parcel.model.DashboardSummary;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ParcelDao {
    private static final String URL = "jdbc:h2:file:"
            + System.getProperty("user.home").replace('\\', '/') + "/parcel-portal-db";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public ParcelDao() {
        initSchema();
    }

    private Connection conn() throws SQLException {
        return DriverManager.getConnection(URL, "sa", "");
    }

    private void initSchema() {
        try (Connection c = conn(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS parcels ("
                    + "parcel_id VARCHAR(30) PRIMARY KEY, "
                    + "sender VARCHAR(100) NOT NULL, receiver VARCHAR(100) NOT NULL, "
                    + "origin VARCHAR(100) NOT NULL, destination VARCHAR(100) NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            s.execute("CREATE TABLE IF NOT EXISTS events ("
                    + "event_id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "parcel_id VARCHAR(30) NOT NULL, status VARCHAR(30) NOT NULL, "
                    + "location VARCHAR(100), event_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "remarks VARCHAR(200), "
                    + "FOREIGN KEY (parcel_id) REFERENCES parcels(parcel_id))");
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot create schema", e);
        }
    }

    public boolean exists(String id) {
        try (Connection c = conn();
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
        try (Connection c = conn()) {
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
                + "ORDER BY p.created_at DESC";
        String pattern = "%" + (query == null ? "" : query.trim().toLowerCase()) + "%";
        List<Parcel> list = new ArrayList<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, pattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Parcel(rs.getString("parcel_id"), rs.getString("sender"),
                        rs.getString("receiver"), rs.getString("origin"),
                        rs.getString("destination"), rs.getString("status"),
                        rs.getString("created_at")));
            }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return list;
    }

    public Parcel findById(String id) {
        for (Parcel parcel : find(id)) {
            if (parcel.id().equalsIgnoreCase(id)) {
                return parcel;
            }
        }
        return null;
    }

    public List<ParcelEvent> eventsFor(String parcelId) {
        List<ParcelEvent> events = new ArrayList<>();
        String sql = "SELECT event_id, parcel_id, status, location, event_time, remarks "
                + "FROM events WHERE parcel_id = ? ORDER BY event_time DESC, event_id DESC";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
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
                         java.sql.Timestamp eventTime, String remarks) {
        String sql = "INSERT INTO events (parcel_id, status, location, event_time, remarks) VALUES (?,?,?,?,?)";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, parcelId);
            ps.setString(2, status);
            ps.setString(3, location);
            ps.setTimestamp(4, eventTime);
            ps.setString(5, remarks);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public DashboardSummary summary() {
        String sql = "SELECT COUNT(*) AS total, "
                + "SUM(CASE WHEN latest.status = 'In Transit' THEN 1 ELSE 0 END) AS in_transit, "
                + "SUM(CASE WHEN latest.status = 'Delivered' THEN 1 ELSE 0 END) AS delivered, "
                + "SUM(CASE WHEN latest.status = 'Failed' OR (latest.status <> 'Delivered' "
                + "AND DATEDIFF('HOUR', latest.event_time, CURRENT_TIMESTAMP()) > 48) "
                + "THEN 1 ELSE 0 END) AS delayed "
                + "FROM parcels p JOIN events latest ON latest.event_id = "
                + "(SELECT e2.event_id FROM events e2 WHERE e2.parcel_id = p.parcel_id "
                + "ORDER BY e2.event_time DESC, e2.event_id DESC LIMIT 1)";
        try (Connection c = conn(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            rs.next();
            return new DashboardSummary(rs.getInt("total"), rs.getInt("in_transit"),
                    rs.getInt("delivered"), rs.getInt("delayed"));
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<Parcel> alerts() {
            List<Parcel> result = new ArrayList<>();
            for (Parcel parcel : find("")) {
                String sql = "SELECT status, DATEDIFF('HOUR', event_time, CURRENT_TIMESTAMP()) AS age "
                        + "FROM events WHERE parcel_id = ? ORDER BY event_time DESC, event_id DESC LIMIT 1";
                try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, parcel.id());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && ("Failed".equals(rs.getString("status"))
                                || (!"Delivered".equals(rs.getString("status")) && rs.getLong("age") > 48))) {
                            result.add(parcel);
                        }
                    }
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
            return result;
    }
}