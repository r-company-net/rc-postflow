CREATE TABLE posts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    url TEXT,
    scheduled_at TIMESTAMP,
    channels TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING'
);