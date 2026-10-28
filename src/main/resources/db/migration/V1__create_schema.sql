-- Parcel Tracking Portal: initial schema (PostgreSQL)
-- Versioned migrations are applied automatically by Flyway when the application starts.

CREATE TABLE parcels (
    parcel_id   VARCHAR(30)  PRIMARY KEY,
    sender      VARCHAR(100) NOT NULL,
    receiver    VARCHAR(100) NOT NULL,
    origin      VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events (
    event_id   BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parcel_id  VARCHAR(30)  NOT NULL REFERENCES parcels (parcel_id) ON DELETE CASCADE,
    status     VARCHAR(30)  NOT NULL,
    location   VARCHAR(100),
    event_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks    VARCHAR(200),
    CONSTRAINT chk_events_status CHECK (status IN
        ('Registered', 'Picked Up', 'In Transit', 'Out for Delivery', 'Delivered', 'Failed'))
);

-- Latest-event lookups (dashboard, alerts, summary) filter by parcel and sort by time.
CREATE INDEX idx_events_parcel_time ON events (parcel_id, event_time DESC, event_id DESC);
CREATE INDEX idx_parcels_created    ON parcels (created_at DESC);
