CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    url VARCHAR(1024),
    scheduled_at TIMESTAMP,
    channels VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);