-- =============================================================
-- FreshFruit - admin approval for promotional prices
-- =============================================================
-- A promotional price is now a request that an administrator has
-- to review before it can be charged:
--
--   pending  -> submitted by the shop owner, waiting for review
--   approved -> reviewed and accepted, applies inside its dates
--   rejected -> reviewed and refused, never applies
--
-- Saving (or editing) a promotion always puts it back to
-- 'pending', so a changed price is reviewed again.
--
-- reviewed_by / reviewed_at / review_note keep the audit trail of
-- the decision, and the shop owner sees the note when a promotion
-- is refused.
--
-- The script is idempotent: everything happens only when the
-- 'status' column is still missing.
--
-- The new columns are referenced through dynamic SQL (EXEC)
-- because a single batch is compiled before it runs and could not
-- otherwise see a column that it is about to add.
-- =============================================================

SET NOCOUNT ON;
GO

IF COL_LENGTH('variant_promotions', 'status') IS NULL
BEGIN
    ALTER TABLE variant_promotions
        ADD status VARCHAR(20) NOT NULL
            CONSTRAINT DF_variant_promotions_status DEFAULT ('pending');

    ALTER TABLE variant_promotions
        ADD review_note NVARCHAR(255) NULL;

    ALTER TABLE variant_promotions
        ADD reviewed_by INT NULL;

    ALTER TABLE variant_promotions
        ADD reviewed_at DATETIME2 NULL;

    EXEC('ALTER TABLE variant_promotions
              ADD CONSTRAINT CK_variant_promotions_status
                  CHECK (status IN (''pending'', ''approved'', ''rejected''))');

    EXEC('ALTER TABLE variant_promotions
              ADD CONSTRAINT FK_variant_promotions_reviewer
                  FOREIGN KEY (reviewed_by) REFERENCES users (user_id)');

    -- Promotions created before the approval workflow were set
    -- directly by the shop owner, so they stay live as approved.
    EXEC('UPDATE variant_promotions
          SET status = ''approved'',
              reviewed_at = SYSDATETIME()');

    PRINT 'Promotion approval columns added; existing promotions approved.';
END
ELSE
BEGIN
    PRINT 'Promotion approval columns already exist.';
END
GO

SELECT promo_id, variant_id, promo_price, start_date, end_date, status,
       reviewed_by, reviewed_at, review_note
FROM variant_promotions;
GO
