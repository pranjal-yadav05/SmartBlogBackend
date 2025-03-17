<p align="center">
  <img src="./backend-logo.png" alt="SmartBlog Logo" width="800" style="border-radius: 50px;"/>
</p>


SmartBlog Backend is the core API service for the SmartBlog platform, built with **Spring Boot** and **MySQL**. It handles authentication, post management, AI-generated content via Google Gemini, and more.

🚀 **Frontend Repo:** [SmartBlog Frontend](https://github.com/pranjal-yadav05/SmartBlog)

## ✨ Features

- 📂 **RESTful API** for managing blog content
- 🔐 **JWT Authentication** for secure user access
- 🏗 **MySQL Database Integration**
- 🔥 **AI-Powered Content Generation** via Google Gemini API
- 🌍 **CORS Support** for seamless frontend-backend communication
- ⚡ **Optimized Connection Pooling** with HikariCP

---

## 🛠 Installation

### Prerequisites

- Java 17+
- Maven
- MySQL Database

### Steps to Run

1. Clone the repository:
   ```sh
   git clone https://github.com/pranjal-yadav05/smartblogbackend.git
   cd smartblogbackend
   ```

2. Configure environment variables:
   Create an `application.properties` file inside `src/main/resources/` and add the following:
   ### Replace the placeholders with your actual credentials.
   ```properties
   # MySQL Database Configuration
   spring.datasource.url=${DATABASE_URL}
   spring.datasource.username=${DATABASE_USERNAME}
   spring.datasource.password=${DATABASE_PASSWORD}
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   server.port=${PORT:8080}

   # File Upload Configuration
   spring.servlet.multipart.enabled=true
   spring.servlet.multipart.max-file-size=10MB
   spring.servlet.multipart.max-request-size=10MB

   # JWT Secret
   jwt.secret=${JWT_SECRET}

   # CORS Allowed Frontend URL
   frontend.url=${FRONTEND_URL}

   # JPA/Hibernate Configuration
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true

   # HikariCP Connection Pooling
   spring.datasource.hikari.maximum-pool-size=${HIKARI_MAX_POOL_SIZE:10}
   spring.datasource.hikari.idle-timeout=${HIKARI_IDLE_TIMEOUT:30000}
   spring.datasource.hikari.connection-timeout=${HIKARI_CONNECTION_TIMEOUT:30000}

   # Gemini API Key
   gemini.api.key=${GEMINI_API_KEY}
   
   # Cloudinary URL
   CLOUDINARY_URL=${CLOUDINARY_URL}
   ```

4. Build the project:
   ```sh
   mvn clean install
   ```

5. Run the application:
   ```sh
   mvn spring-boot:run
   ```
   The backend should now be running at `http://localhost:8080`

---

## 📬 API Endpoints

The API endpoints and documentation can be found in the `docs` folder or via Swagger if enabled.

---

## 🤝 Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

---

## 📜 License

This project is licensed under the **MIT License**.

---

## 📬 Contact

For any inquiries, reach out at [yadavpranjal2105@gmail.com](mailto:yadavpranjal2105@gmail.com).

