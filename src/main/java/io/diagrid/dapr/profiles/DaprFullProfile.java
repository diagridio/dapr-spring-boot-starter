package io.diagrid.dapr.profiles;

import java.util.Collections;
import java.util.List;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.junit.runners.model.Statement;
import org.testcontainers.junit.jupiter.Container;

import io.diagrid.dapr.DaprContainer;
import io.diagrid.dapr.DaprPlacementContainer;
import io.diagrid.dapr.QuotedBoolean;
import io.diagrid.dapr.DaprContainer.Component;
import io.diagrid.dapr.DaprContainer.DaprLogLevel;

import org.junit.runner.Description;

public interface DaprFullProfile {

    Network daprNetwork = getNetwork();

    static Network getNetwork() {
        Network defaultDaprNetwork = new Network() {
            @Override
            public String getId() {
                return "dapr";
            }

            @Override
            public void close() {

            }

            @Override
            public Statement apply(Statement base, Description description) {
                return null;
            }
        };

        List<com.github.dockerjava.api.model.Network> networks = DockerClientFactory.instance().client().listNetworksCmd().withNameFilter("dapr").exec();
        if (networks.isEmpty()) {
            Network.builder()
                    .createNetworkCmdModifier(cmd -> cmd.withName("dapr"))
                    .build().getId();
            return defaultDaprNetwork;
        } else {
            return defaultDaprNetwork;
        }
    }

    

    @Container
    DaprPlacementContainer daprPlacement = new DaprPlacementContainer("daprio/placement:1.12.2")
            .withNetwork(daprNetwork)
            .withNetworkAliases("placement"); 
    
    @Container
    DaprContainer dapr = new DaprContainer("daprio/daprd:1.12.2")
            .withAppName("local-dapr-app")
            //Enable Workflows
            .withComponent(new Component("kvstore", "state.in-memory", Collections.singletonMap("actorStateStore", new QuotedBoolean("true")) ))
            .withAppPort(8080)
            .withNetwork(daprNetwork)
            .withDaprLogLevel(DaprLogLevel.debug)
            .withPlacementService("placement:"+daprPlacement.getPort())
            .withAppChannelAddress("host.testcontainers.internal");

    @DynamicPropertySource
    static void daprProperties(DynamicPropertyRegistry registry) {
        Testcontainers.exposeHostPorts(8080);
        System.setProperty("dapr.grpc.port", Integer.toString(dapr.getGRPCPort()));
        System.setProperty("dapr.http.port", Integer.toString(dapr.getHTTPPort()));
    }
    
}
