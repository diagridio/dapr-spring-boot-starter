package io.diagrid.dapr;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static java.util.Collections.singletonMap;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.CloudEvent;
import io.dapr.client.domain.Metadata;
import io.dapr.client.domain.State;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest (classes=MyTestWithWorkflowsApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@Testcontainers
public class DaprLocalWithWorkflowsTests {

    
    @Autowired()
    private SubscriptionsRestController subscriptionsRestController;

    @Test
    public void myWorkflowTest() throws Exception {
        try (DaprClient client = (new DaprClientBuilder()).build()) {
            
            // client.waitForSidecar(5000);

            // String value = "value";
            // // Save state
            // client.saveState(STATE_STORE_NAME, KEY, value).block();

            // // Get the state back from the state store
            // State<String> retrievedState = client.getState(STATE_STORE_NAME, KEY, String.class).block();

            // assertEquals("The value retrieved should be the same as the one stored", value,
            //         retrievedState.getValue());

        }
    }

   

}
