-- =====================================================================
-- Migration Script: Add Occasional Plan & Attendance Support
-- Database: MySQL
-- Safe for existing data (no tables dropped, no data deleted)
-- =====================================================================

USE gym_db;

-- 1. Modify expiry_date in members to allow NULL for Occasional members
ALTER TABLE members MODIFY COLUMN expiry_date DATE NULL;

-- 2. Create the attendance table for tracking occasional members
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id VARCHAR(20) NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(10) NOT NULL CHECK (status IN ('Present', 'Absent')),
    paid TINYINT(1) DEFAULT 0,
    UNIQUE KEY unique_member_attendance_date (member_id, attendance_date),
    FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
);
