package io.diagrid.dapr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@SpringBootApplication()
public class MyTestWithWorkflowsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyTestWithWorkflowsApplication.class, args);
            
    }

    @ImportTestcontainers(DaprLocalWithWorkflows.class)
    static class DaprTestConfiguration {
       
    }
}
