package io.diagrid.spring.boot.autoconfigure.pubsub;

import io.diagrid.spring.boot.autoconfigure.client.DaprClientAutoConfiguration;
import io.diagrid.spring.core.messaging.DaprMessagingTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DaprPubSubAutoConfiguration}.
 */
class DaprPubSubAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DaprClientAutoConfiguration.class, DaprPubSubAutoConfiguration.class));

    @Test
    void daprMessagingTemplate() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DaprMessagingTemplate.class);
        });
    }
}