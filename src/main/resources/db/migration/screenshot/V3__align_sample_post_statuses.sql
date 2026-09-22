UPDATE posts
SET status = 'PENDING'
WHERE status = 'SCHEDULED'
  AND url IN (
      'https://example.invalid/product-update',
      'https://example.invalid/webinar',
      'https://example.invalid/team-interview',
      'https://example.invalid/newsletter'
  );
