-- Test-only compatibility additions for UserDAO.findByEmail ordering.
-- These columns are NOT present in the source database metadata snapshot.
IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
IF COL_LENGTH('dbo.user_roles','assigned_at') IS NULL
    ALTER TABLE dbo.user_roles ADD assigned_at datetime2 NOT NULL CONSTRAINT DF_test_role_assigned DEFAULT SYSUTCDATETIME();
IF COL_LENGTH('dbo.user_roles','user_role_id') IS NULL
    ALTER TABLE dbo.user_roles ADD user_role_id int IDENTITY(1,1) NOT NULL;
