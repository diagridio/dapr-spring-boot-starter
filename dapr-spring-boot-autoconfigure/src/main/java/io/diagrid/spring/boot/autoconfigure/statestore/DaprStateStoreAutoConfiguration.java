/*
 * Copyright 2024 The Dapr Authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
limitations under the License.
*/

package io.diagrid.spring.boot.autoconfigure.statestore;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.diagrid.spring.boot.autoconfigure.client.DaprClientAutoConfiguration;
import io.diagrid.spring.core.keyvalue.DaprKeyValueAdapterResolver;
import io.diagrid.spring.core.keyvalue.DaprKeyValueTemplate;
import io.diagrid.spring.core.keyvalue.KeyValueAdapterResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.keyvalue.core.KeyValueAdapter;

@AutoConfiguration(after = DaprClientAutoConfiguration.class)
@ConditionalOnClass({DaprClient.class, KeyValueAdapter.class})
@EnableConfigurationProperties(DaprStateStoreProperties.class)
public class DaprStateStoreAutoConfiguration {

  /**
   * Creates a {@link KeyValueAdapterResolver} bean if it is not already defined.
   *
   * @param daprClientBuilder        The Dapr client builder.
   * @param mapper                   The object mapper.
   * @param daprStateStoreProperties The Dapr state store properties.
   * @return A {@link KeyValueAdapterResolver} bean.
   */
  @Bean
  @ConditionalOnMissingBean
  public KeyValueAdapterResolver keyValueAdapterResolver(DaprClientBuilder daprClientBuilder, ObjectMapper mapper,
                                                         DaprStateStoreProperties daprStateStoreProperties) {
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
