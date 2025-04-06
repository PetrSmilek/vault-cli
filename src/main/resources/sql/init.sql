CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE password_entries (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    encrypted_password TEXT NOT NULL,
    iv TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user
        FOREIGN KEY(user_id) 
        REFERENCES users(id)
        ON DELETE CASCADE,
    
    CONSTRAINT unique_service_per_user
        UNIQUE(user_id, service_name)
);

CREATE INDEX idx_user_id ON password_entries(user_id);
CREATE INDEX idx_service_name ON password_entries(service_name);

CREATE USER vault_user WITH PASSWORD '42';
GRANT ALL PRIVILEGES ON TABLE users TO vault_user;
GRANT ALL PRIVILEGES ON SEQUENCE users_id_seq TO vault_user;
GRANT ALL PRIVILEGES ON TABLE password_entries TO vault_user;
GRANT ALL PRIVILEGES ON SEQUENCE password_entries_id_seq TO vault_user;
