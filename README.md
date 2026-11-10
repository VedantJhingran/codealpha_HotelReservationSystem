#  Hotel Reservation System

A full-stack Hotel Reservation System built using **Java, Spring Boot, MySQL, Thymeleaf, and Bootstrap 5**. The application allows users to browse hotel rooms, check availability, and make reservations, while administrators can manage rooms and monitor bookings.

## ✨ Features

### User Features
- User registration and login
- Browse available hotel rooms
- View room details and pricing
- Select check-in and check-out dates
- Check room availability
- Book hotel rooms
- View booking history
- Secure logout and session management

### Admin Features
- Admin authentication
- Admin dashboard
- Add new rooms
- View and edit room details
- Delete rooms
- Manage hotel room inventory

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java | Backend programming |
| Spring Boot | Backend framework |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database operations |
| MySQL | Database |
| Flyway | Database migrations |
| Thymeleaf | Server-side frontend |
| HTML, CSS, JavaScript | Frontend development |
| Bootstrap 5 | Responsive UI |
| Maven | Build management |

## <img width="1440" height="900" alt="Screenshot 2026-11-10 at 3 24 12 PM" src="https://github.com/user-attachments/assets/6193930d-13a9-4551-beb6-d5fe45e915ce" />
<img width="1440" height="900" alt="Screenshot 2026-11-10 at 3 23 57 PM" src="https://github.com/user-attachments/assets/4894c613-99f4-4c55-90e7-75bd0326fb55" />
<img width="1440" height="900" alt="Screenshot 2026-11-10 at 3 23 44 PM" src="https://github.com/user-attachments/assets/228799ae-32d2-4d96-b687-c11ff21fb1b6" />
Screenshots



## Prerequisites

- Java 24 or compatible configured JDK
- MySQL 9.x
- Git
- Maven (Maven Wrapper is included)

## Installation and Setup

**1. Clone the repository**

```bash
git clone https://github.com/YOUR_USERNAME/hotelreservation.git
cd hotelreservation
```

**2. Create the MySQL database**

```sql
CREATE DATABASE hotel_reservation;
```

**3. Configure environment variables**

Set your own database credentials.

```bash
export MYSQL_DATABASE_URL='jdbc:mysql://localhost:3306/hotel_reservation'
export MYSQL_DATABASE_USERNAME='root'
export MYSQL_DATABASE_PASSWORD='YOUR_PASSWORD'
```

**4. Run the application**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

**5. Open in your browser**

```text
http://localhost:8080
```

## Security

- Passwords are securely hashed.
- Spring Security handles authentication and authorization.
- Role-based access control separates USER and ADMIN functionality.
- Database credentials are configured through environment variables.
- CSRF protection is enabled.

## 🗄️ Database

The application uses MySQL with Flyway database migrations to manage schema changes.

Main entities:
- Users
- Rooms
- Bookings

##  Author

Vedant Jhingran

B.Tech – Computer Science and Engineering

Galgotias University

---

