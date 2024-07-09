package io.diagrid.spring.boot.autoconfigure;

import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.diagrid.dapr.DaprContainer;

@Testcontainers
public abstract class BaseIntegrationTest {

    public static Network daprNetwork = Network.newNetwork();

    @Container
    public static DaprContainer daprContainer = new DaprContainer("daprio/daprd:1.13.2")
            .withAppName("local-dapr-app")
            .withNetwork(daprNetwork)
            .withComponent(new DaprContainer.Component("pubsub", "pubsub.in-memory", "v1", Collections.emptyMap()))
            .withAppPort(8080)
            .withDaprLogLevel(DaprContainer.DaprLogLevel.debug)
            .withAppChannelAddress("host.testcontainers.internal");

    @BeforeAll
    static void beforeAll() {
        org.testcontainers.Testcontainers.exposeHostPorts(8080);
        System.setProperty("dapr.grpc.port", Integer.toString(daprContainer.getGRPCPort()));
        System.setProperty("dapr.http.port", Integer.toString(daprContainer.getHTTPPort()));
    }

}
