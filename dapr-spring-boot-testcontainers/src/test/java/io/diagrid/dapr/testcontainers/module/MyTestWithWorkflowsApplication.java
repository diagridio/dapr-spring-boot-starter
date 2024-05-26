package io.diagrid.dapr.testcontainers.module;

import io.diagrid.dapr.testcontainers.DaprModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@SpringBootApplication
public class MyTestWithWorkflowsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyTestWithWorkflowsApplication.class, args);
    }

    @ImportTestcontainers(DaprModule.class)
    static class DaprTestConfiguration {
       
    }

}
