# Minibank
Create customers, open their accounts and make deposits and transfers using minibank REST APIs

## Setup
1. Clone the repository to your local machine
```bash
$ git clone https://github.com/shahlin/minibank.git

# OR, if using SSH:

$ git clone git@github.com:shahlin/minibank.git
```

2. The application properties file need to be created since its not part of the version control. Use the sample as a reference to create the actual file.
```bash
$ cp src/main/resources/application.properties.sample src/main/resources/application.properties
```

3. Create executable jar file

Note: This step requires maven CLI to be installed. [More info on maven site](https://maven.apache.org/install.html)
```bash
$ mvn clean package
```

4. Build docker image
```bash
$ docker build --tag=minibank-server:latest .
```

5. Run the docker container
```bash
# Requires port 8080 to be free
$ docker run -p 8080:8080 minibank-server:latest
```

6. That should get the project up and running! Swagger UI cannot be viewed using: http://localhost:8080/api/v1/swagger-ui/index.html

# Application Flow / Usage
1. Create a new customer
2. Open a new account for the customer
3. Deposit money into account
4. Transfer money to another account
