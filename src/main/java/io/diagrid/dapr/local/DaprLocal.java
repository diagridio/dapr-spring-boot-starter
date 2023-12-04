package io.diagrid.dapr.local;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.Testcontainers;
import org.testcontainers.junit.jupiter.Container;

import io.diagrid.dapr.DaprContainer;

public interface DaprLocal {
    
    @Container
    DaprContainer dapr = new DaprContainer("daprio/daprd")
            .withAppName("local-dapr-app")
            .withAppPort(8080)
            .withAppChannelAddress("host.testcontainers.internal");

    @DynamicPropertySource
    static void daprProperties(DynamicPropertyRegistry registry) {
        Testcontainers.exposeHostPorts(8080);
        System.setProperty("dapr.grpc.port", Integer.toString(dapr.getGRPCPort()));
        System.setProperty("dapr.http.port", Integer.toString(dapr.getHTTPPort()));
    }
    
}
