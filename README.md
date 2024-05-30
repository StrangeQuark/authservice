# About this project
The purpose of this project is to provide a simple Spring Boot JWT authentication and authorization application
which can be easily launched with docker

This project was based on a Spring Authentication and Authorization YouTube video by Amigoscode which can be found here:

[![Spring Boot 3 + Spring Security 6 - JWT Authentication and Authorisation](https://img.youtube.com/vi/KxqlJblhzfI/0.jpg)](https://www.youtube.com/watch?v=KxqlJblhzfI)

# Getting Started
Simply clone main branch of the project and open in your favorite IDE. You will need to globally search for the text \"CHANGEME\" to configure the username and password
of your \"authservice\" database

Once configured, start your docker engine and run 
```docker-compose up```
This will start a PostgreSQL database and the auth service in separate docker containers. The service should now be live on localhost:6001

A sample Postman collection for testing this service can be found here:
[Auth Service Postman Collection](https://github.com/StrangeQuark/authservice/blob/develop/AuthService.postman_collection.json)

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.0.6/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.0.6/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.0.6/reference/htmlsingle/#web)
* [Spring Security](https://docs.spring.io/spring-boot/docs/3.0.6/reference/htmlsingle/#web.security)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/3.0.6/reference/htmlsingle/#data.sql.jpa-and-spring-data)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

