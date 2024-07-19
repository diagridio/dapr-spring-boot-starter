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

import io.diagrid.spring.boot.autoconfigure.client.DaprClientAutoConfiguration;
import io.diagrid.spring.core.keyvalue.DaprKeyValueTemplate;
import io.diagrid.spring.core.keyvalue.KeyValueAdapterResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

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

  private static DaprStateStoreProperties createProperties() {
    DaprStateStoreProperties properties = new DaprStateStoreProperties();
    properties.setName("kvstore");
    properties.setBinding("kvbinding");
    return properties;
  }

  private static KeyValueAdapterResolver createResolver() {
    return () -> null;
  }

  @Test
  void daprKeyValueTemplate() {
    contextRunner.run(context -> {
      assertThat(context).hasSingleBean(DaprKeyValueTemplate.class);
    });
  }

}
