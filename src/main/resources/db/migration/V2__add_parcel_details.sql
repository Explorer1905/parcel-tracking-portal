-- Parcel Tracking Portal: richer parcel record (Phase A1)
-- Adds weight, priority, expected delivery date, and assigned agent to parcels.
-- Upgrade path: existing rows receive column defaults (Standard, 1.00 kg, no date, no agent).

ALTER TABLE parcels
    ADD COLUMN weight_kg          NUMERIC(8,2)  NOT NULL DEFAULT 1.00,
    ADD COLUMN priority           VARCHAR(10)   NOT NULL DEFAULT 'Standard',
    ADD COLUMN expected_delivery  DATE,
    ADD COLUMN assigned_agent     VARCHAR(100);

ALTER TABLE parcels
    ADD CONSTRAINT chk_parcels_weight   CHECK (weight_kg > 0 AND weight_kg <= 1000),
    ADD CONSTRAINT chk_parcels_priority CHECK (priority IN ('Standard','Express','Urgent'));

CREATE INDEX idx_parcels_priority ON parcels (priority);
