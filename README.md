# 🚆 Railway Ticket Reservation System (Java Full-Stack Mini Project)

A lightweight, web-based Railway Ticket Reservation System built with **Core Java**, **HTML/CSS/JS**, and **JDBC (MySQL)** covering all 5 units of the R25 Java Programming syllabus.

## 📚 Syllabus Coverage (R25 Regulation)
- **Unit I (Java Fundamentals & OOP):** Encapsulation (`Train` model), Constructors, `this`, `static` PNR generator, `final` constants, and `StringBuilder` JSON builder.
- **Unit II (Advanced OOP & Exceptions):** Abstract class (`Coach`), Inheritance & Method Overriding (`SleeperCoach`, `ACCoach`), and Custom Exception (`SeatNotAvailableException`).
- **Unit III (Collections & Streams):** `HashMap` for O(1) train lookup, `ArrayList` for booking history, and Java `Stream` API (`.filter()`) for searching trains by station.
- **Unit IV (Multithreading & File I/O):** `synchronized` booking method to prevent race conditions during concurrent ticket booking, and `FileWriter` to export confirmed e-tickets (`Ticket_<PNR>.txt`).
- **Unit V (JDBC & Transactions):** MySQL integration using `PreparedStatement` and atomic transaction handling (`setAutoCommit(false)`, `commit()`, `rollback()`).

## 🚀 How to Run Locally

### 1. Clone the repository
```bash
git clone [https://github.com/YOUR-USERNAME/railway-ticket-reservation-system.git](https://github.com/YOUR-USERNAME/railway-ticket-reservation-system.git)
cd railway-ticket-reservation-system
