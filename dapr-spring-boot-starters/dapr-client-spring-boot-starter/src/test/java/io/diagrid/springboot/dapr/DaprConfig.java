package io.diagrid.springboot.dapr;

import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Network;

import io.diagrid.dapr.DaprContainer;
import io.diagrid.dapr.DaprContainer.Component;
import io.diagrid.dapr.DaprContainer.DaprLogLevel;
import io.diagrid.dapr.DaprPlacementContainer;
import io.diagrid.dapr.QuotedBoolean;
import io.diagrid.springboot.dapr.core.DaprTemplate;



@TestConfiguration(proxyBeanMethods = false)
public class DaprConfig {

    //TODO: This needs to be populated with the Dapr Module for Testcontainers
    Network daprNetwork = getNetwork();

    //TODO: This needs to be populated with the Dapr Module for Testcontainers
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


    @Bean
    public DaprTemplate<String> getDaprTemplate(){
        return new DaprTemplate<String>();
    }
    
    @Bean
    public DaprContainer getDapr(DynamicPropertyRegistry registry) {
        //TODO: This needs to be populated with the Dapr Module for Testcontainers 
        DaprPlacementContainer daprPlacement = new DaprPlacementContainer("daprio/placement:1.13.2")
                .withNetwork(daprNetwork)
                .withNetworkAliases("placement"); 
        
        daprPlacement.start();
        
        DaprContainer dapr = new DaprContainer("daprio/daprd:1.13.2")
                .withAppName("local-dapr-app")
                //Enable Workflows
                .withComponent(new Component("kvstore", "state.in-memory", Collections.singletonMap("actorStateStore", new QuotedBoolean("true")) ))
                .withComponent(new Component("pubsub", "pubsub.in-memory", Collections.emptyMap() ))
                .withAppPort(8080)
                .withNetwork(daprNetwork)
                .withDaprLogLevel(DaprLogLevel.debug)
                .withPlacementService("placement:"+daprPlacement.getPort())
                .withAppChannelAddress("host.testcontainers.internal");

        registry.add("DAPR_GRPC_ENDPOINT", () -> ("localhost:"+dapr.getGRPCPort()));
        registry.add("DAPR_HTTP_ENDPOINT", dapr::getHttpEndpoint);
        
        dapr.start();

        System.setProperty("dapr.grpc.port", Integer.toString(dapr.getGRPCPort()));
        System.setProperty("dapr.http.port", Integer.toString(dapr.getHTTPPort()));

        System.out.println("Ports: ");
        System.out.println("Ports GRPC: " + Integer.toString(dapr.getGRPCPort()));
        System.out.println("Ports GRPC: " + Integer.toString(dapr.getHTTPPort()));
        
        return dapr;

    }

}
