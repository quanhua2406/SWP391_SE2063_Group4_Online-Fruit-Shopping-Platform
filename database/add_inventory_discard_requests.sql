-- =============================================================
-- FreshFruit - Approval for writing off expired batches
-- =============================================================
-- The shop owner requests to discard an expired batch; the request
-- waits as 'pending' until the shop owner approves or rejects it.
-- Idempotent: safe to run more than once.
-- =============================================================

SET NOCOUNT ON;
GO

IF OBJECT_ID('inventory_discard_requests', 'U') IS NULL
BEGIN
    CREATE TABLE inventory_discard_requests (
        request_id    INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_inventory_discard_requests PRIMARY KEY,
        batch_id      INT               NOT NULL,
        requested_by  INT               NOT NULL,
        status        VARCHAR(10)       NOT NULL CONSTRAINT DF_inventory_discard_status DEFAULT ('pending'),
        requested_at  DATETIME2         NOT NULL CONSTRAINT DF_inventory_discard_requested DEFAULT (SYSDATETIME()),
        resolved_by   INT               NULL,
        resolved_at   DATETIME2         NULL,
        CONSTRAINT CK_inventory_discard_status CHECK (status IN ('pending', 'approved', 'rejected')),
        CONSTRAINT FK_inventory_discard_batch FOREIGN KEY (batch_id)
            REFERENCES inventory_batches (batch_id) ON DELETE CASCADE
    );
END;
GO
