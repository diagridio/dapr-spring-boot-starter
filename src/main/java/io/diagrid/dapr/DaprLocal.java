package io.diagrid.dapr;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.util.Map;

public interface DaprLocal {
    
    DaprContainer dapr = new DaprContainer("daprio/daprd")
            .withAppName("local-dapr-app");

    @DynamicPropertySource
    static void daprProperties(DynamicPropertyRegistry registry) {
        System.setProperty("dapr.grpc.port", Integer.toString(dapr.getGRPCPort()));
    }
    
}
