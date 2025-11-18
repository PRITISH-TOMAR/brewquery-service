CREATE TABLE user_roles
(
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO user_roles (role_id, role_name) VALUES
(1, 'ADMIN'),
(2, 'USER');

CREATE TABLE user_details
(
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    status ENUM('ACTIVE', 'INACTIVE', 'BLOCKED', 'IN_PROGRESS')  DEFAULT 'IN_PROGRESS',
    role_id INT NOT NULL DEFAULT 1,
    phone_number VARCHAR(10),
    country_code VARCHAR(7),
    profile_picture_url TEXT,
    hashed_password TEXT NOT NULL,
    salt TEXT NOT NULL,
    last_login DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_details_role FOREIGN KEY (role_id) REFERENCES user_roles(role_id)
);

CREATE UNIQUE INDEX idx_user_email ON user_details(email);


