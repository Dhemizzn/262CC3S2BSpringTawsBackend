CREATE SCHEMA IF NOT EXISTS audit;

CREATE TABLE IF NOT EXISTS audit.event (
    event_id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    schema_name        VARCHAR(100)  NOT NULL,
    table_name         VARCHAR(100)  NOT NULL,
    record_id          VARCHAR(150)  NOT NULL,
    user_id            BIGINT        NOT NULL,
    module             VARCHAR(100),
    operation          VARCHAR(30)   NOT NULL,
    event_date         TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    source_ip          INET,
    http_method        VARCHAR(10),
    endpoint           VARCHAR(500),
    http_status        INTEGER,
    result             VARCHAR(30)   NOT NULL,
    reason             VARCHAR(500),
    interaction_id     UUID,
    app_version        VARCHAR(50),
    CONSTRAINT chk_result
        CHECK (result IN ('SUCCESS', 'FAILURE'))
);

CREATE TABLE IF NOT EXISTS audit.change (
    change_id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id           BIGINT       NOT NULL REFERENCES audit.event(event_id),
    field_name         VARCHAR(150) NOT NULL,
    old_value          TEXT,
    new_value          TEXT
);

CREATE INDEX IF NOT EXISTS idx_event_table_record
    ON audit.event (schema_name, table_name, record_id, event_date DESC);

CREATE INDEX IF NOT EXISTS idx_event_user
    ON audit.event (user_id, event_date DESC);

CREATE INDEX IF NOT EXISTS idx_event_ip
    ON audit.event (source_ip);

CREATE INDEX IF NOT EXISTS idx_event_interaction
    ON audit.event (interaction_id);

CREATE INDEX IF NOT EXISTS idx_change_event
    ON audit.change (event_id);
