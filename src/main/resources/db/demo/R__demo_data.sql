-- Optional demo data. Loaded only when DB_SEED_DEMO=true (see Database.java).
-- Repeatable and idempotent: safe to run more than once.

INSERT INTO parcels (parcel_id, sender, receiver, origin, destination, created_at) VALUES
    ('DEMO-1001', 'Asha Traders',     'Rohan Mehta',    'Mumbai',    'Pune',      LOCALTIMESTAMP - INTERVAL '6 days'),
    ('DEMO-1002', 'Kalyan Books',     'Neha Joshi',     'Mumbai',    'Nashik',    LOCALTIMESTAMP - INTERVAL '2 days'),
    ('DEMO-1003', 'Delhi Gadgets',    'Imran Khan',     'Delhi',     'Jaipur',    LOCALTIMESTAMP - INTERVAL '20 hours'),
    ('DEMO-1004', 'Pune Pharma',      'Sunita Rao',     'Pune',      'Mumbai',    LOCALTIMESTAMP - INTERVAL '5 days'),
    ('DEMO-1005', 'Thane Textiles',   'Vikram Shah',    'Thane',     'Surat',     LOCALTIMESTAMP - INTERVAL '3 days'),
    ('DEMO-1006', 'Nagpur Foods',     'Priya Nair',     'Nagpur',    'Indore',    LOCALTIMESTAMP - INTERVAL '8 hours'),
    ('DEMO-1007', 'Kalyan Hardware',  'Arjun Patil',    'Kalyan',    'Panvel',    LOCALTIMESTAMP - INTERVAL '3 days'),
    ('DEMO-1008', 'Chennai Cycles',   'Meera Iyer',     'Chennai',   'Bengaluru', LOCALTIMESTAMP - INTERVAL '2 days'),
    ('DEMO-1009', 'Kolkata Crafts',   'Sourav Das',     'Kolkata',   'Patna',     LOCALTIMESTAMP - INTERVAL '7 days'),
    ('DEMO-1010', 'Hyderabad Herbs',  'Farah Siddiqui', 'Hyderabad', 'Pune',      LOCALTIMESTAMP - INTERVAL '30 hours')
ON CONFLICT (parcel_id) DO NOTHING;

INSERT INTO events (parcel_id, status, location, event_time, remarks)
SELECT v.parcel_id, v.status, v.location, v.event_time, v.remarks
FROM (VALUES
    ('DEMO-1001', 'Registered',       'Mumbai',    LOCALTIMESTAMP - INTERVAL '6 days',                  'Parcel registered'),
    ('DEMO-1001', 'Picked Up',        'Mumbai',    LOCALTIMESTAMP - INTERVAL '6 days' + INTERVAL '3 hours', 'Picked up by courier'),
    ('DEMO-1001', 'In Transit',       'Lonavala',  LOCALTIMESTAMP - INTERVAL '5 days',                  'Left sorting hub'),
    ('DEMO-1001', 'Out for Delivery', 'Pune',      LOCALTIMESTAMP - INTERVAL '4 days',                  'With delivery agent'),
    ('DEMO-1001', 'Delivered',        'Pune',      LOCALTIMESTAMP - INTERVAL '4 days' + INTERVAL '5 hours', 'Signed by receiver'),

    ('DEMO-1002', 'Registered',       'Mumbai',    LOCALTIMESTAMP - INTERVAL '2 days',                  'Parcel registered'),
    ('DEMO-1002', 'Picked Up',        'Mumbai',    LOCALTIMESTAMP - INTERVAL '2 days' + INTERVAL '2 hours', 'Picked up by courier'),
    ('DEMO-1002', 'In Transit',       'Igatpuri',  LOCALTIMESTAMP - INTERVAL '10 hours',                'On the way'),

    ('DEMO-1003', 'Registered',       'Delhi',     LOCALTIMESTAMP - INTERVAL '20 hours',                'Parcel registered'),
    ('DEMO-1003', 'Picked Up',        'Delhi',     LOCALTIMESTAMP - INTERVAL '15 hours',                'Picked up by courier'),

    ('DEMO-1004', 'Registered',       'Pune',      LOCALTIMESTAMP - INTERVAL '5 days',                  'Parcel registered'),
    ('DEMO-1004', 'In Transit',       'Pune',      LOCALTIMESTAMP - INTERVAL '4 days',                  'No scan since this point'),

    ('DEMO-1005', 'Registered',       'Thane',     LOCALTIMESTAMP - INTERVAL '3 days',                  'Parcel registered'),
    ('DEMO-1005', 'In Transit',       'Vapi',      LOCALTIMESTAMP - INTERVAL '2 days',                  'On the way'),
    ('DEMO-1005', 'Failed',           'Surat',     LOCALTIMESTAMP - INTERVAL '1 day',                   'Receiver unavailable'),

    ('DEMO-1006', 'Registered',       'Nagpur',    LOCALTIMESTAMP - INTERVAL '8 hours',                 'Parcel registered'),

    ('DEMO-1007', 'Registered',       'Kalyan',    LOCALTIMESTAMP - INTERVAL '3 days',                  'Parcel registered'),
    ('DEMO-1007', 'In Transit',       'Kalyan',    LOCALTIMESTAMP - INTERVAL '3 days' + INTERVAL '2 hours', 'Awaiting transport'),

    ('DEMO-1008', 'Registered',       'Chennai',   LOCALTIMESTAMP - INTERVAL '2 days',                  'Parcel registered'),
    ('DEMO-1008', 'In Transit',       'Hosur',     LOCALTIMESTAMP - INTERVAL '1 day',                   'On the way'),
    ('DEMO-1008', 'Out for Delivery', 'Bengaluru', LOCALTIMESTAMP - INTERVAL '3 hours',                 'With delivery agent'),

    ('DEMO-1009', 'Registered',       'Kolkata',   LOCALTIMESTAMP - INTERVAL '7 days',                  'Parcel registered'),
    ('DEMO-1009', 'In Transit',       'Dhanbad',   LOCALTIMESTAMP - INTERVAL '6 days',                  'On the way'),
    ('DEMO-1009', 'Delivered',        'Patna',     LOCALTIMESTAMP - INTERVAL '5 days',                  'Signed by receiver'),

    ('DEMO-1010', 'Registered',       'Hyderabad', LOCALTIMESTAMP - INTERVAL '30 hours',                'Parcel registered'),
    ('DEMO-1010', 'In Transit',       'Solapur',   LOCALTIMESTAMP - INTERVAL '6 hours',                 'On the way')
) AS v (parcel_id, status, location, event_time, remarks)
WHERE NOT EXISTS (SELECT 1 FROM events e WHERE e.parcel_id = v.parcel_id);
