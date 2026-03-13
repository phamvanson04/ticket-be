-- Booking schema redesign for concurrency-safe seat reservation
-- NOTE: Adjust SQL syntax if your runtime database is not MySQL-compatible.

ALTER TABLE Seats
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE Tickets
    ADD COLUMN IF NOT EXISTS booking_reference VARCHAR(64) NOT NULL DEFAULT 'legacy-booking';

CREATE INDEX IF NOT EXISTS idx_tickets_showtime_cancelled
    ON Tickets(showtime_id, is_cancelled);

CREATE INDEX IF NOT EXISTS idx_tickets_booking_reference
    ON Tickets(booking_reference);
