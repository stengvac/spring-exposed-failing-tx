----------------------------------------
-- SCRIPT START
----------------------------------------
--
CREATE USER some_user WITH ENCRYPTED PASSWORD 'pwd';
--
GRANT ALL PRIVILEGES ON DATABASE postgres TO some_user;
--
CREATE SCHEMA some_schema AUTHORIZATION some_user;
----------------------------------------
-- GRANTS
----------------------------------------
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA some_schema TO some_user;
----------------------------------------
-- SCRIPT END
----------------------------------------

CREATE TABLE some_table (
    some_id uuid PRIMARY KEY
);