# Auth Server - Centralized Authentication Microservice

A comprehensive Spring Boot microservice that implements OAuth 2.1 and OpenID Connect standards for centralized authentication and authorization.

## Features

- **OAuth 2.1 / OpenID Connect** implementation with Spring Authorization Server
- **User Registration & Management** with email verification
- **Multi-Factor Authentication (MFA)** with TOTP support
- **JWT Token Management** with RSA key signing
- **Role-Based Access Control** with flexible role assignment
- **Refresh Token Rotation** for enhanced security
- **PostgreSQL Database** with Flyway migrations
- **Swagger/OpenAPI** documentation
- **Comprehensive Testing** with unit and integration tests
- **CORS Support** for cross-origin requests
- **Audit Logging** for security monitoring

## Technology Stack

- **Java 17+**
- **Spring Boot 3.2.0**
- **Spring Security 6.x**
- **Spring Authorization Server 1.2.1**
- **Spring Data JPA**
- **PostgreSQL 14+**
- **Flyway 10.7.1**
- **JWT (jjwt 0.12.3)**
- **Swagger/OpenAPI 3**
- **Maven**

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 14+
- OpenSSL (for RSA key generation)

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd auth-rest-api
```

### 2. Generate RSA Keys

```bash
# Make the script executable
chmod +x scripts/generate-keys.sh

# Generate RSA key pair
./scripts/generate-keys.sh
```

This will create:

- `src/main/resources/keys/private.pem` - Private key for JWT signing
- `src/main/resources/keys/public.pem` - Public key for JWT verification

**⚠️ Important**: Never commit the private key to version control!

### 3. Set Up PostgreSQL Database

```bash
# Connect to PostgreSQL as superuser
psql -U postgres

# Run the setup script
\i scripts/setup-postgresql.sql
```

Or manually create the database:

```sql
CREATE DATABASE auth_db;
GRANT ALL PRIVILEGES ON DATABASE auth_db TO postgres;
```

### 4. Configure Application Properties

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db
    username: postgres
    password: your_password

  mail:
    username: your-email@gmail.com
    password: your-app-password
```

### 5. Run Flyway Migrations

```bash
# Run migrations using Maven
mvn flyway:migrate

# Or using Flyway CLI
flyway -url=jdbc:postgresql://localhost:5432/auth_db -user=postgres -password=your_password migrate
```

### 6. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### User Management

| Method | Endpoint                      | Description          | Auth Required |
| ------ | ----------------------------- | -------------------- | ------------- |
| POST   | `/api/users/register`         | User registration    | No            |
| GET    | `/api/users/verify-email`     | Email verification   | No            |
| GET    | `/api/users/me`               | Current user profile | Yes           |
| GET    | `/api/users/{userId}`         | Get user by ID       | Admin only    |
| PUT    | `/api/users/{userId}/profile` | Update user profile  | Self or Admin |
| PUT    | `/api/users/{userId}/roles`   | Update user roles    | Admin only    |

### Multi-Factor Authentication

| Method | Endpoint                                 | Description             | Auth Required |
| ------ | ---------------------------------------- | ----------------------- | ------------- |
| POST   | `/api/users/mfa/setup`                   | Setup MFA               | Yes           |
| POST   | `/api/users/mfa/verify`                  | Verify MFA code         | Yes           |
| POST   | `/api/users/mfa/disable`                 | Disable MFA             | Yes           |
| POST   | `/api/users/mfa/regenerate-backup-codes` | Regenerate backup codes | Yes           |

### Authentication

| Method | Endpoint               | Description             | Auth Required |
| ------ | ---------------------- | ----------------------- | ------------- |
| POST   | `/api/auth/logout`     | Logout user             | Yes           |
| POST   | `/api/auth/logout/all` | Logout from all devices | Yes           |
| POST   | `/api/auth/refresh`    | Refresh access token    | No            |

### OAuth 2.0 / OpenID Connect

| Method | Endpoint                            | Description              |
| ------ | ----------------------------------- | ------------------------ |
| GET    | `/oauth2/authorize`                 | Authorization endpoint   |
| POST   | `/oauth2/token`                     | Token endpoint           |
| GET    | `/.well-known/openid_configuration` | OpenID Connect discovery |

## OAuth 2.0 Flow

### Authorization Code Flow with PKCE

1. **Authorization Request**

   ```
   GET /oauth2/authorize?
     response_type=code&
     client_id=postgres&
     redirect_uri=http://localhost:3000/callback&
     scope=openid profile email&
     state=random_state&
     code_challenge=code_challenge&
     code_challenge_method=S256
   ```

2. **Token Request**

   ```
   POST /oauth2/token
   Content-Type: application/x-www-form-urlencoded

   grant_type=authorization_code&
   code=authorization_code&
   redirect_uri=http://localhost:3000/callback&
   client_id=postgres&
   client_secret=postgres-secret&
   code_verifier=code_verifier
   ```

3. **Response**
   ```json
   {
     "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
     "token_type": "Bearer",
     "expires_in": 3600,
     "refresh_token": "refresh_token_value",
     "scope": "openid profile email"
   }
   ```

## Database Schema

The application uses the following main tables:

- **users** - User accounts and profiles
- **roles** - Available roles
- **user_roles** - User-role assignments
- **refresh_tokens** - Refresh token management
- **audit_logs** - Security audit trail
- **oauth2_registered_client** - OAuth client registrations
- **oauth2_authorization** - OAuth authorization records

## Security Features

### Password Security

- **bcrypt** hashing (default) with configurable cost factor
- **Argon2** support for enhanced security
- Password strength validation

### Token Security

- **RSA-256** signed JWT tokens
- Configurable token expiration
- Refresh token rotation
- Token revocation on logout

### Multi-Factor Authentication

- **TOTP** (Time-based One-Time Password)
- Backup codes for account recovery
- Configurable MFA settings

### Access Control

- Role-based authorization
- Method-level security with `@PreAuthorize`
- CORS configuration for client applications

## Configuration

### Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/auth_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# JWT
APP_JWT_ACCESS_TOKEN_VALIDITY=3600
APP_JWT_REFRESH_TOKEN_VALIDITY=86400
```

### Application Properties

Key configuration options in `application.yml`:

```yaml
app:
  jwt:
    access-token-validity: 3600 # 1 hour
    refresh-token-validity: 86400 # 24 hours

  cors:
    allowed-origins:
      - http://localhost:3000
      - https://yourdomain.com

  email:
    verification:
      enabled: true
      expiration-hours: 24

  mfa:
    enabled: true
    digits: 6
    period: 30
```

## Testing

### Unit Tests

```bash
# Run unit tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest
```

### Integration Tests

```bash
# Run integration tests
mvn verify

# Run with TestContainers
mvn test -Dspring.profiles.active=test
```

### Test Coverage

```bash
# Generate coverage report
mvn jacoco:report
```

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/auth/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data transfer objects
│   │   ├── entity/         # JPA entities
│   │   ├── repository/     # Data access layer
│   │   └── service/        # Business logic
│   └── resources/
│       ├── db/migration/   # Flyway migrations
│       ├── keys/           # RSA keys (not in version control)
│       └── application.yml # Configuration
├── test/                   # Test classes and resources
└── scripts/                # Utility scripts
```

### Adding New Features

1. **Create Entity** - Add JPA entity in `entity/` package
2. **Create Repository** - Add repository interface in `repository/` package
3. **Create Service** - Add business logic in `service/` package
4. **Create Controller** - Add REST endpoints in `controller/` package
5. **Add Tests** - Create unit and integration tests
6. **Update Documentation** - Update this README and API docs

## Deployment

### Docker

```dockerfile
FROM openjdk:17-jre-slim
COPY target/auth-rest-api-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Docker Compose

```yaml
version: "3.8"
services:
  auth-server:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/auth_db
    depends_on:
      - postgres

  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: auth_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### Production Considerations

- Use external PostgreSQL instance
- Configure proper CORS origins
- Set up monitoring and logging
- Use external email service (SendGrid, AWS SES)
- Implement rate limiting
- Set up SSL/TLS termination
- Configure backup strategies

## Monitoring and Health Checks

### Actuator Endpoints

- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

### Health Indicators

- Database connectivity
- JWT key availability
- Email service status

## Troubleshooting

### Common Issues

1. **Database Connection Failed**

   - Verify PostgreSQL is running
   - Check database credentials
   - Ensure database exists

2. **JWT Key Errors**

   - Verify RSA keys exist in `src/main/resources/keys/`
   - Check key permissions
   - Regenerate keys if needed

3. **Email Not Sending**

   - Verify SMTP configuration
   - Check email credentials
   - Test with simple email client

4. **OAuth Flow Issues**
   - Verify client registration
   - Check redirect URI configuration
   - Validate scope configuration

### Logs

Enable debug logging:

```yaml
logging:
  level:
    com.auth: DEBUG
    org.springframework.security: DEBUG
    org.springframework.security.oauth2: DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:

- Create an issue in the repository
- Check the documentation
- Review the test examples
- Contact the development team

## Changelog

### Version 1.0.0

- Initial release
- OAuth 2.1 / OpenID Connect implementation
- User management with MFA
- JWT token management
- PostgreSQL with Flyway migrations
- Comprehensive testing suite
