# Spring Security Auth Service

This repository contains an example Authentication service built using **Java 17** and **Spring Boot**. The project demonstrates JWT-based authentication and authorization as part of a microservice architecture. The service currently includes the following features:

- User registration with password hashing using BCrypt
- Login endpoint that generates access and refresh JWTs
- Automatic token refresh at `/auth/refresh`
- Token validation on each request via the `Authorization: Bearer` header
- Role support: `USER`, `ADMIN`, `MODERATOR`
- Token revocation using a blacklist stored in a database
- Logout that adds the refresh token to the blacklist
- Ability to maintain a blacklist of usernames
- Event logging for logins, logouts and access attempts
- A simple Security Dashboard for administrators
- Endpoints to block tokens or terminate sessions
- Optional MFA support using TOTP codes
- Login page prompts for MFA code when required
- Persistent H2 file database for data retention across restarts
- Dashboard displays recent events and allows managing user roles

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
