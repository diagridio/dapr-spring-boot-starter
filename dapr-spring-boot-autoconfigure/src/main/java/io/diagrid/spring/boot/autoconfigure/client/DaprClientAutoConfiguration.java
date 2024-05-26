package io.diagrid.spring.boot.autoconfigure.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.diagrid.spring.core.client.DaprClientCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(DaprClient.class)
public class DaprClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    DaprClientBuilderConfigurer daprClientBuilderConfigurer(ObjectProvider<DaprClientCustomizer> customizerProvider) {
        DaprClientBuilderConfigurer configurer = new DaprClientBuilderConfigurer();
        configurer.setDaprClientCustomizer(customizerProvider.orderedStream().toList());
        return configurer;
    }

    @Bean
    @ConditionalOnMissingBean
    DaprClientBuilder daprClientBuilder(DaprClientBuilderConfigurer daprClientBuilderConfigurer) {
        var daprClientBuilder = new DaprClientBuilder();
        return daprClientBuilderConfigurer.configure(daprClientBuilder);
    }

}
