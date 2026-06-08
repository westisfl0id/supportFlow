INSERT INTO users (
    name,
    email,
    password,
    role,
    status,
    created_at
)
VALUES (
           'Administrator',
           'admin@supportflow.local',
           '$2a$10$tXX0gRdJ0Zhe0dmBLToGZu/a7zrHHxdLxEmgrtXlKfTg/mr8.Im2W',
           'ADMIN',
           'ACTIVE',
           CURRENT_TIMESTAMP
       )
    ON CONFLICT (email) DO NOTHING;