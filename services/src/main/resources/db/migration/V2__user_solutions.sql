CREATE TABLE user_solutions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    question_id VARCHAR(50) NOT NULL,
    dataset_id VARCHAR(50) NOT NULL,
    user_query TEXT NOT NULL,

    is_correct BOOLEAN NOT NULL,
    result_hash VARCHAR(100),
    
    time_taken_ms INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_details(user_id)
);
