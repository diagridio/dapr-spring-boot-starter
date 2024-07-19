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
  public DaprMessagingTemplate<?> messagingTemplate(DaprClientBuilder daprClientBuilder,
                                                    DaprPubSubProperties daprPubSubProperties) {
    return new DaprMessagingTemplate<>(daprClientBuilder.build(), daprPubSubProperties.getName());
  }

}
