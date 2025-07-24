# Authservice
**Authservice** is a secure microservice for handling user authentication and authorization using JSON web tokens.<br><br>
It uses AES encryption to protect all user data at rest, and supports hierarchical user structures.
<br><br><br>

## Features
- AES-256 encryption for all user objects
- JSON web token (JWT) for passing user information to other services
- Ready-to-run Docker environment
- Postman collection for testing and exploration
  <br><br><br>

## Technology Stack
- Java 17+
- Spring Boot
- PostgreSQL
- Docker & Docker Compose
- JPA (Hibernate)
- JUnit 5
  <br><br><br>

## Getting Started

### Prerequisites
- Docker and Docker Compose installed
- Java 17+ (for development or test execution outside Docker)
  <br><br>

### Running the Application
Clone the repository and start the service using Docker Compose:

```
git clone https://github.com/StrangeQuark/authservice.git
cd authservice
docker-compose up --build
```
<br>

### Environment Variables
The `.env` file is required to provide necessary configuration such as encryption secrets and database credentials. Default values are provided in `.env` file so the application can run out-of-the-box for testing.

⚠️ **Warning**: Do not deploy this application to production without properly changing your environment variables. The provided `.env` is not safe to use past local deployments!
<br><br>

## API Documentation
A Postman collection is included in the root of the project:

- `Authservice.postman_collection.json`

This collection provides examples for:
- Registering and authenticating users
- Bootstrapping an initial SUPER user
- Generating and retrieving refresh and access JWT's
- Retrieving user ID's
- Enabling and disabling users
- Updating user credentials
- Adding and removing authorizations from users
- Deleting users
- Sending password reset emails (If integrated with EmailService)
  <br><br>

## Testing
Unit tests are provided for all repository and service-layer logic.
<br><br>

## Deployment
This project includes a `Jenkinsfile` for use in CI/CD pipelines. Jenkins must be configured with:

- Docker support
- Secrets or environment variables for configuration
- Access to any relevant private repositories, if needed
  <br><br>

## Optional: Emailservice Integration
Authservice can integrate with a separate Emailservice microservice to support enabling users via confirmation emails.

For more information, see: [Emailservice GitHub Repository](https://github.com/StrangeQuark/emailservice)
<br><br>

## License
This project is licensed under the GNU General Public License. See `LICENSE.md` for details.
<br><br>

## Contributing
Contributions are welcome! Feel free to open issues or submit pull requests.
