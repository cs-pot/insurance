DELETE FROM packages;

INSERT INTO packages (
    id,
    version,
    name,
    payroll,
    start_date,
    end_date,
    status,
    created_at,
    created_by,
    updated_at,
    updated_by,
    deleted_at,
    deleted_by
) VALUES (
    'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1',
    0,
    'Premium Health Package',
    'MONTHLY',
    '2026-01-01',
    '2026-12-31',
    'INITIALIZED',
    '2026-01-01T00:00:00Z',
    'system',
    NULL,
    NULL,
    NULL,
    NULL
);