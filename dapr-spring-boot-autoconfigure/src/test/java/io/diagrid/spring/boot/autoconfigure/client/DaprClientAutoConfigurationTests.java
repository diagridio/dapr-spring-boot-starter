package io.diagrid.spring.boot.autoconfigure.client;

import io.dapr.client.DaprClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DaprClientAutoConfiguration}.
 */
class DaprClientAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DaprClientAutoConfiguration.class));

    @Test
    void daprClientBuilderConfigurer() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DaprClientBuilderConfigurer.class);
        });
    }

    @Test
    void daprClientBuilder() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DaprClientBuilder.class);
        });
    }

}