package io.diagrid.spring.boot.autoconfigure.statestore;

import io.diagrid.spring.boot.autoconfigure.client.DaprClientAutoConfiguration;
import io.diagrid.spring.core.keyvalue.DaprKeyValueAdapter;
import io.diagrid.spring.core.keyvalue.DaprKeyValueTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DaprStateStoreAutoConfiguration}.
 */
class DaprStateStoreAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DaprClientAutoConfiguration.class, DaprStateStoreAutoConfiguration.class));

    @Test
    void daprKeyValueAdapter() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DaprKeyValueAdapter.class);
        });
    }

    @Test
    void daprKeyValueTemplate() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DaprKeyValueTemplate.class);
        });
    }

}