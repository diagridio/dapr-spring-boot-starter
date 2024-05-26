package io.diagrid.spring.boot.autoconfigure.pubsub;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.diagrid.spring.boot.autoconfigure.client.DaprClientAutoConfiguration;
import io.diagrid.spring.core.messaging.DaprMessagingTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = DaprClientAutoConfiguration.class)
@ConditionalOnClass(DaprClient.class)
@EnableConfigurationProperties(DaprPubSubProperties.class)
public class DaprPubSubAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DaprMessagingTemplate<?> messagingTemplate(DaprClientBuilder daprClientBuilder, DaprPubSubProperties daprPubSubProperties) {
        return new DaprMessagingTemplate<>(daprClientBuilder.build(), daprPubSubProperties.getName());
    }

}
