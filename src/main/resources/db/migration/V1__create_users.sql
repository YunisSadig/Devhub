CREATE TABLE users
(
    id                BIGSERIAL PRIMARY KEY,
    version           BIGINT,

    first_name        VARCHAR(50)  NOT NULL,
    last_name         VARCHAR(50)  NOT NULL,

    email             VARCHAR(255) NOT NULL,
    password          VARCHAR(255) NOT NULL,

    role              VARCHAR(20)  NOT NULL,

    enabled           BOOLEAN      NOT NULL DEFAULT TRUE,
    email_verified    BOOLEAN      NOT NULL DEFAULT FALSE,

    bio               VARCHAR(500),
    profile_image_url VARCHAR(255),

    country           VARCHAR(100),
    city              VARCHAR(100),

    last_login_at     TIMESTAMP,

    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL,

    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email
    ON users (email);