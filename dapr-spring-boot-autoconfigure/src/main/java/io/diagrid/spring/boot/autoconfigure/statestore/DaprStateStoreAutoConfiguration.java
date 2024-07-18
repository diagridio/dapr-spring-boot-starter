package io.diagrid.spring.boot.autoconfigure.statestore;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.diagrid.spring.core.keyvalue.DaprKeyValueAdapterResolver;
import io.diagrid.spring.core.keyvalue.KeyValueAdapterResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.keyvalue.core.KeyValueAdapter;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.diagrid.spring.boot.autoconfigure.client.DaprClientAutoConfiguration;
import io.diagrid.spring.core.keyvalue.DaprKeyValueTemplate;

@AutoConfiguration(after = DaprClientAutoConfiguration.class)
@ConditionalOnClass({DaprClient.class, KeyValueAdapter.class})
@EnableConfigurationProperties(DaprStateStoreProperties.class)
public class DaprStateStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KeyValueAdapterResolver keyValueAdapterResolver(DaprClientBuilder daprClientBuilder, ObjectMapper mapper, DaprStateStoreProperties daprStateStoreProperties) {
        DaprClient daprClient = daprClientBuilder.build();
        String stateStoreName = daprStateStoreProperties.getName();
        String bindingName = daprStateStoreProperties.getBinding();

        return new DaprKeyValueAdapterResolver(daprClient, mapper, stateStoreName, bindingName);
    }

    @Bean
    @ConditionalOnMissingBean
    public DaprKeyValueTemplate keyValueTemplate(KeyValueAdapterResolver keyValueAdapterResolver) {
        return new DaprKeyValueTemplate(keyValueAdapterResolver);
    }

}
