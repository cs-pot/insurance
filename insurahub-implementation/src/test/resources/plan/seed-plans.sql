DELETE FROM plans;

INSERT INTO plans (
    id,
    version,
    package_id,
    name,
    type,
    contribution,
    election,
    created_at,
    created_by,
    updated_at,
    updated_by,
    deleted_at,
    deleted_by
) VALUES
    (
        'bbbbbbbb-0001-0001-0001-000000000001',
        0,
        'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1',
        'Standard Health',
        'HEALTH_INSURANCE',
        250.00,
        500.00,
        '2026-01-02T00:00:00Z',
        'system',
        NULL,
        NULL,
        NULL,
        NULL
    ),
    (
        'bbbbbbbb-0002-0002-0002-000000000002',
        0,
        'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1',
        'Dental Care',
        'DENTAL_INSURANCE',
        100.00,
        300.00,
        '2026-01-02T00:00:00Z',
        'system',
        NULL,
        NULL,
        NULL,
        NULL
    );