package io.diagrid.dapr;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.State;

@SpringBootTest
@ImportTestcontainers(DaprLocal.class)
@Testcontainers
public class DaprLocalTest {


    private String STATE_STORE_NAME = "statestore";
    private String KEY = "my-key";  
    

    @Test
    public void myTest() throws Exception {
        try (DaprClient client = (new DaprClientBuilder()).build()) {

            String value = "value";
            // Save state
            client.saveState(STATE_STORE_NAME, KEY, value).block();

            // Get the state back from the state store
            State<String> retrievedState = client.getState(STATE_STORE_NAME, KEY, String.class).block();

            Assert.assertEquals("The value retrieved should be the same as the one stored", value,
                    retrievedState.getValue());

        }
    }

}
