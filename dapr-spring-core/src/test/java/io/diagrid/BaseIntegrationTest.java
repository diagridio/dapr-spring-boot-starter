package io.diagrid;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.diagrid.dapr.DaprContainer;
import io.diagrid.dapr.QuotedBoolean;

@Testcontainers
public abstract class BaseIntegrationTest {

    public static final Network daprNetwork = Network.newNetwork();

    
    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine")
               .withNetworkAliases("postgres")
               .withDatabaseName("dapr_db")
               .withUsername("postgres")
               .withPassword("password")
               .withExposedPorts(5432)
               .withNetwork(daprNetwork);


    @Container
    public static final DaprContainer daprContainer = new DaprContainer("daprio/daprd:1.13.2")
            .withAppName("local-dapr-app")
            .withNetwork(daprNetwork)
            .withComponent(new DaprContainer.Component("kvstore", "state.postgresql", "v1", getStateStoreProperties()))
            .withComponent(new DaprContainer.Component("kvbinding", "bindings.postgresql", "v1",  getStateStoreBindingProperties()))
            .withComponent(new DaprContainer.Component("pubsub", "pubsub.in-memory",  "v1", Collections.emptyMap() ))
            .withAppPort(8080)
            .withDaprLogLevel(DaprContainer.DaprLogLevel.debug)
            .withAppChannelAddress("host.testcontainers.internal")
            .dependsOn(postgreSQLContainer);

    private static Map<String, Object> getStateStoreProperties() {
        String connectionStringPostgreSQL = "host=postgres user=postgres password=password port=5432 connect_timeout=10 database=dapr_db";
        Map<String, Object> stateStoreProperties = new HashMap<>();
        stateStoreProperties.put("keyPrefix", "name");
        stateStoreProperties.put("actorStateStore", new QuotedBoolean("true"));
        stateStoreProperties.put("connectionString", connectionStringPostgreSQL);
        return stateStoreProperties;
    }
    private static Map<String, Object> getStateStoreBindingProperties() {
        String connectionStringPostgreSQL = "host=postgres user=postgres password=password port=5432 connect_timeout=10 database=dapr_db";
        Map<String, Object> stateStoreBindingProperties = new HashMap<>();
        stateStoreBindingProperties.put("connectionString", connectionStringPostgreSQL);
        return stateStoreBindingProperties;
    }

    @BeforeAll
    static void beforeAll() {
        org.testcontainers.Testcontainers.exposeHostPorts(8080);
        System.setProperty("dapr.grpc.port", Integer.toString(daprContainer.getGRPCPort()));
        System.setProperty("dapr.http.port", Integer.toString(daprContainer.getHTTPPort()));
    }

}
