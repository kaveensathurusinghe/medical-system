# Medical System

A comprehensive medical management system built with Spring Boot framework, designed to handle medical operations with security and web interface capabilities.

## Tools and Frameworks Used

### Backend Frameworks
- **Spring Boot 3.4.5** - Main application framework
- **Spring Security** - Authentication and authorization
- **Spring Web** - RESTful web services and MVC architecture
- **Spring Boot DevTools** - Development utilities for hot reloading

### Frontend Technologies  
- **Thymeleaf** - Server-side Java template engine
- **Thymeleaf Spring Security Integration** - Security integration for templates
- **HTML/CSS** - Frontend markup and styling

### Development Tools
- **Java 21** - Programming language
- **Maven** - Dependency management and build tool
- **Project Lombok** - Code generation library for reducing boilerplate
- **Spring Boot Configuration Processor** - Configuration metadata generation

### Testing Framework
- **Spring Boot Test** - Testing utilities
- **Spring Security Test** - Security testing support

## OOP Concepts Used

### Core OOP Principles
- **Encapsulation** - Data hiding through private fields and public methods
- **Inheritance** - Extending Spring Boot classes and interfaces
- **Polymorphism** - Method overriding and interface implementations
- **Abstraction** - Abstract classes and interfaces for service layers

### Design Patterns
- **Model-View-Controller (MVC)** - Architectural pattern for web applications
- **Dependency Injection** - IoC container management
- **Repository Pattern** - Data access abstraction
- **Service Layer Pattern** - Business logic separation
- **Factory Pattern** - Bean creation and management

### Spring-Specific OOP Features
- **Annotations** - Declarative programming with @Controller, @Service, @Repository
- **Aspect-Oriented Programming** - Cross-cutting concerns
- **Bean Lifecycle Management** - Object creation and destruction

## Data Structures Used

### Collection Framework
- **List** - Ordered collections for patient records, appointments
- **Set** - Unique collections for roles, permissions
- **Map** - Key-value pairs for configuration, caching
- **Queue** - Appointment scheduling and processing

### Spring Data Structures
- **Model** - Data transfer objects
- **Entity** - JPA entity representations
- **DTO (Data Transfer Objects)** - Data transportation between layers
- **Optional** - Null-safe data handling

### Custom Data Structures
- **Medical Records** - Patient information storage
- **Appointment Scheduling** - Time-based data organization
- **User Authentication** - Security credential management

## Setup and Installation

### Prerequisites
- **Java 21** or higher
- **Maven 3.6+** or use included Maven wrapper
- **IDE** (IntelliJ IDEA, Eclipse, VS Code recommended)

### Installation Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/IT-23161160/medical-system.git
   cd medical-system
   ```

2. **Build the Project**
   ```bash
   # Using Maven wrapper (recommended)
   ./mvnw clean install
   
   # Or using system Maven
   mvn clean install
   ```

3. **Run the Application**
   ```bash
   # Using Maven wrapper
   ./mvnw spring-boot:run
   
   # Or using system Maven
   mvn spring-boot:run
   
   # Or run the JAR file
   java -jar target/MedicalSystem-0.0.1-SNAPSHOT.jar
   ```

4. **Access the Application**
   - Open your web browser
   - Navigate to: `http://localhost:8080`
   - Default port is 8080 (configurable in application.properties)

### Configuration

#### Application Properties
Create `src/main/resources/application.properties` or `application.yml` for:
- Database configuration
- Server port settings
- Security configurations
- Logging levels

#### Security Configuration
The application includes Spring Security with:
- Authentication mechanisms
- Authorization rules
- CSRF protection
- Session management

## Project Structure

```
medical-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/medicalSystem/
│   │   │       ├── MedicalSystemApplication.java
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       └── config/
│   │   └── resources/
│   │       ├── templates/
│   │       ├── static/
│   │       └── application.properties
│   └── test/
├── target/
├── .mvn/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## Development

### Running Tests
```bash
./mvnw test
```

### Development Mode
The application includes Spring Boot DevTools for:
- Automatic restart on code changes
- Live reload capabilities
- Enhanced development experience

### Building for Production
```bash
./mvnw clean package
```

## Features

- **User Authentication & Authorization**
- **Medical Record Management**
- **Appointment Scheduling**
- **Patient Management**
- **Secure Web Interface**
- **RESTful API Support**

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is open source and available under the [MIT License](LICENSE).

## Author

**IT-23161160** - [GitHub Profile](https://github.com/IT-23161160)

## Support

For support and questions, please open an issue in the GitHub repository.

---

**Note**: This medical system is designed for educational and demonstration purposes. Ensure proper security measures and compliance with healthcare regulations before using in production environments.
