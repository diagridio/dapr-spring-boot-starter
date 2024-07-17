package io.diagrid.spring.boot.autoconfigure.statestore;

import io.diagrid.spring.boot.autoconfigure.client.DaprClientAutoConfiguration;
import io.diagrid.spring.core.keyvalue.PostgreSQLDaprKeyValueAdapter;
import io.diagrid.spring.core.keyvalue.DaprKeyValueTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DaprStateStoreAutoConfiguration}.
 */
class DaprStateStoreAutoConfigurationTests {
    private static final AutoConfigurations AUTO_CONFIGURATIONS = AutoConfigurations.of(
            JacksonAutoConfiguration.class,
            DaprClientAutoConfiguration.class,
            DaprStateStoreAutoConfiguration.class
    );

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(DaprStateStoreProperties.class, () -> {
                DaprStateStoreProperties properties = new DaprStateStoreProperties();
                properties.setName("kvstore");
                properties.setBinding("kvbinding");
                return properties;
            })
            .withConfiguration(AUTO_CONFIGURATIONS);

    @Test
    void daprKeyValueAdapter() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(PostgreSQLDaprKeyValueAdapter.class);
        });
    }

    @Test
    void daprKeyValueTemplate() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DaprKeyValueTemplate.class);
        });
    }

}