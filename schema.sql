-- ===================================================================
-- TASKNEST - Database Schema & Sample Data (MySQL)
-- College DBMS Mini Project Reference Script
-- ===================================================================

CREATE DATABASE IF NOT EXISTS tasknest_db;
USE tasknest_db;

-- 1. Table: users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

-- 2. Table: task_lists
CREATE TABLE IF NOT EXISTS task_lists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id BIGINT,
    CONSTRAINT fk_task_list_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Table: tasks
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATE,
    priority ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL DEFAULT 'MEDIUM',
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    task_list_id BIGINT NOT NULL,
    CONSTRAINT fk_task_task_list FOREIGN KEY (task_list_id) REFERENCES task_lists(id) ON DELETE CASCADE
);

-- ===================================================================
-- Sample Data Insertion
-- ===================================================================

INSERT INTO users (name, email) VALUES
('Karnika', 'karnika@tasknest.com');

INSERT INTO task_lists (name, user_id) VALUES
('Academics', 1),
('Personal', 1),
('Projects', 1);

INSERT INTO tasks (title, description, due_date, priority, completed, task_list_id) VALUES
('DBMS Lab Record Submission', 'Complete ER diagrams and normalization write-up', CURDATE(), 'HIGH', FALSE, 1),
('Operating Systems Viva Prep', 'Revise CPU scheduling algorithms and paging', DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'MEDIUM', FALSE, 1),
('Gym Workout - Leg Day', 'Squats, lunges, and calf raises', CURDATE(), 'LOW', TRUE, 2),
('Pay Electricity Bill', 'Pay utility bill before due date', DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'HIGH', FALSE, 2),
('TaskNest Frontend Polishing', 'Add responsive CSS and toast alerts', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'MEDIUM', FALSE, 3),
('Setup MySQL Database', 'Configure user permissions and create schema', DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'HIGH', TRUE, 3);
