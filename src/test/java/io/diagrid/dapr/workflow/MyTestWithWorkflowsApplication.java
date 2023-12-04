package io.diagrid.dapr.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

import io.diagrid.dapr.workflows.DaprLocalWithWorkflows;

@SpringBootApplication()
public class MyTestWithWorkflowsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyTestWithWorkflowsApplication.class, args);
            
    }

    @ImportTestcontainers(DaprLocalWithWorkflows.class)
    static class DaprTestConfiguration {
       
    }
}
