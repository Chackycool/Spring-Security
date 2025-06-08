# Spring Security Auth Service

This repository contains an example Authentication service built using **Java 17** and **Spring Boot**. The project will demonstrate JWT-based authentication and authorization as part of a microservice architecture. The initial application only includes a basic Spring Boot setup. Future work will follow the agreed-upon plan:

- User registration with password hashing using BCrypt
- Login endpoint that generates access and refresh JWTs
- Automatic token refresh at `/auth/refresh`
- Token validation on each request via the `Authorization: Bearer` header
- Role support: `USER`, `ADMIN`, `MODERATOR`
- Token revocation using a blacklist stored in Redis or a database
- Logout that adds the refresh token to the blacklist
- Microservices: this `auth-service` will handle registration, login and token validation
- Event logging for logins, logouts and access attempts
- A "Security Dashboard" for administrators with activity charts and suspicious activity notifications
- Options to block tokens or terminate sessions
- MFA support

## Pushing this repository to GitHub

1. Create an empty repository on GitHub (e.g. `yourusername/auth-service`).
2. Add the remote URL to this local project:
   ```bash
   git remote add origin git@github.com:yourusername/auth-service.git
   ```
   or using HTTPS:
   ```bash
   git remote add origin https://github.com/yourusername/auth-service.git
   ```
3. Push the existing commits to GitHub:
   ```bash
   git push -u origin main
   ```

After pushing, Codex will be able to access your repository by its GitHub URL.
