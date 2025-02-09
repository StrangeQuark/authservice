package com.strangequark.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	//Configure the CORS policy, allow the ReactService through
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				boolean dockerDeployment = Boolean.parseBoolean(System.getenv("DOCKER_DEPLOYMENT"));

				//Allow the reactService through the CORS policy
				registry.addMapping("/**")
						.allowedOrigins(
								"http://react-service:3001", "http://localhost:3001",
								"http://gateway-service:8080", "http://localhost:8080"
						)
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowCredentials(true);
			}
		};
	}
}
