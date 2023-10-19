package io.diagrid.dapr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@SpringBootApplication
public class MyTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyTestApplication.class, args);
            
    }

    @ImportTestcontainers(DaprLocal.class)
    static class DaprTestConfiguration {
       
    }
}
