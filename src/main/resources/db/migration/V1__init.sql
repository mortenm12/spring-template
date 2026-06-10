-- Initial schema for the template microservice
CREATE TABLE IF NOT EXISTS items
(
    id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_items_name ON items (name);

COMMENT ON TABLE items IS 'Template entity — replace with your domain-specific table';
