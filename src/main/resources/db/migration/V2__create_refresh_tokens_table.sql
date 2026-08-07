CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY,
                                token VARCHAR(1000) NOT NULL UNIQUE,
                                user_id UUID NOT NULL,
                                expires_at TIMESTAMP NOT NULL,
                                revoked BOOLEAN NOT NULL DEFAULT FALSE,

                                CONSTRAINT fk_refresh_tokens_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens(user_id);

CREATE INDEX idx_refresh_tokens_token
    ON refresh_tokens(token);