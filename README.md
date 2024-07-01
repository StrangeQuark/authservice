# About this project
The purpose of this project is to provide a simple Spring Boot JWT authentication and authorization application
which can be easily launched with docker

# Getting Started
Simply clone main branch of the project and open in your favorite IDE. The `.env` file contains dummy values that
should be changed to fit your purposes.

Once configured, start your docker engine and run 
```docker-compose up```
This will start a PostgreSQL database and the auth service in separate docker containers. The service should now be live on localhost:6001

A sample Postman collection for testing this service can be found here:
[Auth Service Postman Collection](https://github.com/StrangeQuark/authservice/blob/develop/AuthService.postman_collection.json)

