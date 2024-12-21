# Auth service
The purpose of this project is to provide a simple Spring Boot JWT authentication and authorization application
which can be easily launched with docker

# Tools
Spring Boot, PostgreSQL, and Docker

# Getting Started
It is highly recommended that the service is ran using docker, the `.env` file contains dummy values that should be 
changed to fit your purposes. This service also assumes that external networks are available
with the names authdb-network and shared-network. These can be created by running the following commands:

`docker network create authdb-network`

`docker network create shared-network`

Note that these commands only need to be run once. After the networks have been created you can simply use the
`docker-compose up --build` command to run the application, there are no other steps needed

A sample Postman collection for testing this service can be found here:
[Auth Service Postman Collection](https://github.com/StrangeQuark/authservice/blob/develop/AuthService.postman_collection.json)

