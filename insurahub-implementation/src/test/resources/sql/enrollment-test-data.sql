INSERT INTO consumers (id, version, idp_id, email, first_name, last_name, personal_id, date_of_birth, address, city, created_at, created_by, deleted_at)
VALUES
    ('a1111111-1111-1111-1111-111111111111', 0, 'auth0|consumer-A', 'consumer-a@test.com', 'Test', 'User', 'PID-AAAAAAA', '2000-01-01', '123 Test St', 'Testville', NOW(), 'test', NULL),
    ('b2222222-2222-2222-2222-222222222222', 0, 'auth0|consumer-B', 'consumer-b@test.com', 'Test', 'User', 'PID-BBBBBBB', '2000-01-01', '123 Test St', 'Testville', NOW(), 'test', NULL),
    ('c3333333-3333-3333-3333-333333333333', 0, 'auth0|consumer-C', 'consumer-c@test.com', 'Test', 'User', 'PID-CCCCCCC', '2000-01-01', '123 Test St', 'Testville', NOW(), 'test', NULL);

INSERT INTO packages (id, version, name, payroll, start_date, end_date, status, created_at, created_by, deleted_at)
VALUES ('d4444444-4444-4444-4444-444444444444', 0, 'Test Package', 'MONTHLY', '2024-01-01', '2025-01-01', 'INITIALIZED', NOW(), 'test', NULL);

INSERT INTO plans (id, version, package_id, name, type, contribution, election, created_at, created_by, deleted_at)
VALUES ('e5555555-5555-5555-5555-555555555555', 0, 'd4444444-4444-4444-4444-444444444444', 'Test Plan', 'HEALTH_INSURANCE', 100.00, 50.00, NOW(), 'test', NULL);
