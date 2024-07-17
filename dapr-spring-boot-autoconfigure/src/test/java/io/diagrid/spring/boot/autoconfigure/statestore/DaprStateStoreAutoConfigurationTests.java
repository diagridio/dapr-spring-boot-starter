package io.diagrid.spring.boot.autoconfigure.statestore;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.*;
import io.diagrid.spring.boot.autoconfigure.client.DaprClientAutoConfiguration;
import io.diagrid.spring.core.keyvalue.DaprKeyValueAdapterResolver;
import io.diagrid.spring.core.keyvalue.DaprKeyValueTemplate;
import io.diagrid.spring.core.keyvalue.KeyValueAdapterResolver;
import io.grpc.stub.AbstractStub;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DaprStateStoreAutoConfiguration}.
 */
class DaprStateStoreAutoConfigurationTests {
    private static final DaprStateStoreProperties PROPERTIES = createProperties();
    private static final KeyValueAdapterResolver RESOLVER = createResolver();
    private static final AutoConfigurations AUTO_CONFIGURATIONS = AutoConfigurations.of(
            JacksonAutoConfiguration.class,
            DaprClientAutoConfiguration.class,
            DaprStateStoreAutoConfiguration.class
    );

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(DaprStateStoreProperties.class, () -> PROPERTIES)
            .withBean(KeyValueAdapterResolver.class, () -> RESOLVER)
            .withConfiguration(AUTO_CONFIGURATIONS);

    @Test
    void daprKeyValueTemplate() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DaprKeyValueTemplate.class);
        });
    }

    private static DaprStateStoreProperties createProperties() {
        DaprStateStoreProperties properties = new DaprStateStoreProperties();
        properties.setName("kvstore");
        properties.setBinding("kvbinding");
        return properties;
    }

    private static KeyValueAdapterResolver createResolver() {
        return () -> null;
    }

}
