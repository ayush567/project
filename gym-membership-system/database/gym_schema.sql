-- =====================================================================
-- Gym Membership Tracking and Management System Database Schema
-- Database: MySQL
-- =====================================================================

CREATE DATABASE IF NOT EXISTS gym_db;
USE gym_db;

-- 1. Drop existing tables if they exist (in reverse order of foreign keys)
DROP TABLE IF EXISTS renewals;
DROP TABLE IF EXISTS lockers;
DROP TABLE IF EXISTS members;

-- 2. Table: members
-- Stores member personal info, membership plan, dates, trainer, and status
CREATE TABLE members (
    member_id VARCHAR(20) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    emergency_contact_name VARCHAR(100) NOT NULL,
    emergency_contact_number VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    membership_type VARCHAR(50) NOT NULL,
    duration VARCHAR(20) NOT NULL,
    membership_fee DOUBLE NOT NULL,
    trainer_name VARCHAR(100) DEFAULT 'None',
    joining_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL
);

-- 3. Table: lockers
-- Stores physical locker location assigned to each member
CREATE TABLE lockers (
    locker_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id VARCHAR(20) UNIQUE NOT NULL,
    zone VARCHAR(50) NOT NULL,
    `row_number` VARCHAR(20) NOT NULL,
    locker_number VARCHAR(20) NOT NULL,
    FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
);

-- 4. Table: renewals
-- Stores history of membership renewals
CREATE TABLE renewals (
    renewal_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id VARCHAR(20) NOT NULL,
    renewal_date DATE NOT NULL,
    new_expiry_date DATE NOT NULL,
    amount_paid DOUBLE NOT NULL,
    payment_mode VARCHAR(50) NOT NULL,
    FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
);

-- 5. Optional Sample Data for Immediate Testing
INSERT INTO members (member_id, full_name, contact_number, emergency_contact_name, emergency_contact_number, address, membership_type, duration, membership_fee, trainer_name, joining_date, expiry_date, status)
VALUES
('M1001', 'Rahul Sharma', '9876543210', 'Sunil Sharma', '9876543211', '12 Park Street, Mumbai', 'Premium', 'Yearly', 19200.0, 'Vikram Singh', '2025-01-10', '2026-01-10', 'Active'),
('M1002', 'Pooja Verma', '9123456780', 'Anita Verma', '9123456781', '45 MG Road, Pune', 'Basic', 'Monthly', 1000.0, 'None', '2025-09-01', '2025-10-01', 'Due for Renewal'),
('M1003', 'Aman Gupta', '9988776655', 'Ramesh Gupta', '9988776650', '88 Civil Lines, Delhi', 'Student', 'Quarterly', 2160.0, 'None', '2025-06-15', '2025-09-15', 'Expired');

INSERT INTO lockers (member_id, zone, `row_number`, locker_number)
VALUES
('M1001', 'Men\'s', 'Row-A', 'L-101'),
('M1002', 'Women\'s', 'Row-B', 'L-204'),
('M1003', 'Men\'s', 'Row-C', 'L-310');

INSERT INTO renewals (member_id, renewal_date, new_expiry_date, amount_paid, payment_mode)
VALUES
('M1001', '2025-01-10', '2026-01-10', 19200.0, 'Card');