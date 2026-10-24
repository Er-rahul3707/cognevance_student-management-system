CREATE DATABASE IF NOT EXISTS student_management CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS 'student_app'@'localhost' IDENTIFIED BY 'replace_with_a_local_password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX ON student_management.* TO 'student_app'@'localhost';
FLUSH PRIVILEGES;

USE student_management;
CREATE TABLE IF NOT EXISTS students (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL,
  phone VARCHAR(30),
  course VARCHAR(100) NOT NULL,
  enrollment_date DATE NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT uk_students_email UNIQUE (email)
);
