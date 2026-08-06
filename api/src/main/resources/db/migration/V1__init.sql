-- Kairo schema (compatible with H2 MODE=PostgreSQL and Neon Postgres)

CREATE TABLE users (
    id UUID PRIMARY KEY,
    google_sub VARCHAR(128) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL,
    name VARCHAR(200) NOT NULL,
    avatar_url VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE event_types (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name VARCHAR(80) NOT NULL,
    color VARCHAR(16) NOT NULL,
    icon VARCHAR(40),
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_event_types_user_name UNIQUE (user_id, name)
);

CREATE TABLE plans (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type_id UUID NOT NULL REFERENCES event_types (id),
    title VARCHAR(160) NOT NULL,
    description VARCHAR(4000),
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    all_day BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_plans_status CHECK (status IN ('planned', 'done', 'cancelled'))
);

CREATE INDEX idx_plans_user_starts ON plans (user_id, starts_at);
CREATE INDEX idx_plans_user_type ON plans (user_id, type_id);

CREATE TABLE google_connections (
    user_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    refresh_token_enc VARCHAR(4000) NOT NULL,
    scopes VARCHAR(1000) NOT NULL,
    connected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE google_event_links (
    plan_id UUID PRIMARY KEY REFERENCES plans (id) ON DELETE CASCADE,
    google_event_id VARCHAR(256) NOT NULL,
    calendar_id VARCHAR(256) NOT NULL DEFAULT 'primary'
);
