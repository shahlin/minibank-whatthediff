package com.example.minibank;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Minibank API", version = "1.0", description = "Create customers, open their accounts and make deposits and transfers using minibank REST APIs"))
public class MinibankApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinibankApplication.class, args);
    }

}
