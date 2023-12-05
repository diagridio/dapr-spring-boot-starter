package io.diagrid.dapr.profiles.basic;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static java.util.Collections.singletonMap;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.CloudEvent;
import io.dapr.client.domain.Metadata;
import io.dapr.client.domain.State;

import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest (classes=MyTestApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@Testcontainers
public class DaprBasicProfileTests {

    private String STATE_STORE_NAME = "kvstore";
    private String KEY = "my-key";  
    private String PUB_SUB_NAME = "pubsub";
    private String PUB_SUB_TOPIC_NAME = "topic";
    private static final String MESSAGE_TTL_IN_SECONDS = "1000";
    
    @Autowired()
    private SubscriptionsRestController subscriptionsRestController;

    @Test
    public void myTest() throws Exception {
        try (DaprClient client = (new DaprClientBuilder()).build()) {
            
            client.waitForSidecar(5000);

            String value = "value";
            // Save state
            client.saveState(STATE_STORE_NAME, KEY, value).block();

            // Get the state back from the state store
            State<String> retrievedState = client.getState(STATE_STORE_NAME, KEY, String.class).block();

            assertEquals("The value retrieved should be the same as the one stored", value,
                    retrievedState.getValue());

        }
    }

    @Test
    public void myTestSubscription() throws Exception {

        String value = "value";

        try (DaprClient client = (new DaprClientBuilder()).build()) {
            client.waitForSidecar(5000);

            // Publish Event
            client.publishEvent(PUB_SUB_NAME, PUB_SUB_TOPIC_NAME, value, 
                                singletonMap(Metadata.TTL_IN_SECONDS, MESSAGE_TTL_IN_SECONDS))
                                .block();

        }
    
        // Wait for the event to arrive
        Thread.sleep(1000);

        List<CloudEvent> events = subscriptionsRestController.getAllEvents();
        assertEquals("One published event is expected",1, events.size());
        assertEquals("The content of the cloud event should be the published value", value, events.get(0).getData());
    }

}
