package com.example.fleetmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableJpaRepositories("com.example.fleetmanagementsystem.repositories")
@EntityScan("com.example.fleetmanagementsystem.model")
public class FleetManagementSystemApplication {

    public static void main(String[] args) {
        // Load environment variables from .env file
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        System.setProperty("SERVER_PORT", dotenv.get("SERVER_PORT", "5000"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME", ""));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD", ""));
        System.setProperty("DB_URL", dotenv.get("DB_URL", ""));
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET",""));
        System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION",""));
        System.setProperty("MAIL_USERNAME", dotenv.get("MAIL_USERNAME",""));
        System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD",""));

        SpringApplication.run(FleetManagementSystemApplication.class, args);
    }

}
