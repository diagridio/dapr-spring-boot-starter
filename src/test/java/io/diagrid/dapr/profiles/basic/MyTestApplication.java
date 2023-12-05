package io.diagrid.dapr.profiles.basic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

import io.diagrid.dapr.profiles.DaprBasicProfile;

@SpringBootApplication
public class MyTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyTestApplication.class, args);
            
    }

    @ImportTestcontainers(DaprBasicProfile.class)
    static class DaprTestConfiguration {
       
    }
}
