package io.diagrid.dapr.profiles.full;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

import io.diagrid.dapr.profiles.DaprFullProfile;

@SpringBootApplication()
public class MyTestWithWorkflowsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyTestWithWorkflowsApplication.class, args);
            
    }

    @ImportTestcontainers(DaprFullProfile.class)
    static class DaprTestConfiguration {
       
    }
}
