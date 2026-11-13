CREATE DATABASE IF NOT EXISTS railway_db;
USE railway_db;

CREATE TABLE trains (
    train_no INT PRIMARY KEY,
    name VARCHAR(100),
    source VARCHAR(50),
    destination VARCHAR(50),
    seats INT,
    distance INT
);

CREATE TABLE bookings (
    pnr BIGINT PRIMARY KEY,
    train_no INT,
    passenger_name VARCHAR(100),
    coach_type VARCHAR(20),
    seats INT,
    total_fare DOUBLE
);

INSERT INTO trains VALUES 
(12723, 'Telangana Express', 'Hyderabad', 'New Delhi', 50, 1670),
(12759, 'Charminar Express', 'Chennai', 'Hyderabad', 40, 790),
(12704, 'Falaknuma Express', 'Secunderabad', 'Howrah', 30, 1545);
