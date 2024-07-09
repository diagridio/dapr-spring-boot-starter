package io.diagrid.dapr.testcontainers;

import java.util.Collections;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.Testcontainers;
import org.testcontainers.junit.jupiter.Container;

import io.diagrid.dapr.DaprContainer;
import io.diagrid.dapr.DaprContainer.Component;
import io.diagrid.dapr.DaprContainer.DaprLogLevel;
import io.diagrid.dapr.QuotedBoolean;

public interface DaprModule {
 
    @Container
    DaprContainer dapr = new DaprContainer("daprio/daprd:1.13.2")
            .withAppName("local-dapr-app")
            //Enable Workflows
            .withComponent(new Component("kvstore", "state.in-memory", "v1", Collections.singletonMap("actorStateStore", new QuotedBoolean("true")) ))
            .withComponent(new Component("pubsub", "pubsub.in-memory", "v1", Collections.emptyMap() ))
            .withAppPort(8080)
            .withDaprLogLevel(DaprLogLevel.debug)
            .withAppChannelAddress("host.testcontainers.internal");

    @DynamicPropertySource
    static void daprProperties(DynamicPropertyRegistry registry) {
        Testcontainers.exposeHostPorts(8080);
        dapr.start();
        System.setProperty("dapr.grpc.port", Integer.toString(dapr.getGRPCPort()));
        System.setProperty("dapr.http.port", Integer.toString(dapr.getHTTPPort()));
    }
    
}
