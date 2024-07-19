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

package io.diagrid.spring.core.keyvalue.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.diagrid.AbstractPostgreSQLBaseIT;
import io.diagrid.spring.core.keyvalue.DaprKeyValueAdapterResolver;
import io.diagrid.spring.core.keyvalue.DaprKeyValueTemplate;
import io.diagrid.spring.core.keyvalue.KeyValueAdapterResolver;
import io.diagrid.spring.core.keyvalue.TestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for {@link DaprKeyValueRepositoryIT}.
 */

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class DaprKeyValueRepositoryIT extends AbstractPostgreSQLBaseIT {

  @Autowired
  private TestTypeRepository repository;

  @BeforeEach
  public void setUp() {
    repository.deleteAll();
  }

  @Test
  public void testFindById() {
    TestType saved = repository.save(new TestType(3, "test"));
    TestType byId = repository.findById(3).get();

    assertEquals(saved, byId);
  }

  @Test
  public void testExistsById() {
    repository.save(new TestType(3, "test"));

    boolean existsById = repository.existsById(3);
    assertTrue(existsById);

    boolean existsById2 = repository.existsById(4);
    assertFalse(existsById2);
  }

  @Test
  public void testFindAll() {
    repository.save(new TestType(3, "test"));
    repository.save(new TestType(4, "test2"));

    Iterable<TestType> all = repository.findAll();

    assertEquals(2, all.spliterator().getExactSizeIfKnown());
  }

  @Test
  public void testFinUsingQuery() {
    repository.save(new TestType(3, "test"));
    repository.save(new TestType(4, "test2"));

    List<TestType> byContent = repository.findByContent("test2");

    assertEquals(1, byContent.size());
  }

  @Configuration
  @EnableDaprRepositories
  static class Config {

    @Bean
    public ObjectMapper mapper() {
      return new ObjectMapper();
    }

    @Bean
    public KeyValueAdapterResolver keyValueAdapterResolver(DaprClientBuilder daprClientBuilder, ObjectMapper mapper) {
      DaprClient daprClient = daprClientBuilder.build();

      return new DaprKeyValueAdapterResolver(daprClient, mapper, STATE_STORE_NAME, BINDING_NAME);
    }

    @Bean
    public DaprKeyValueTemplate daprKeyValueTemplate(KeyValueAdapterResolver keyValueAdapterResolver) {
      return new DaprKeyValueTemplate(keyValueAdapterResolver);
    }

    @Bean
    public DaprClientBuilder daprClientBuilder() {
      return new DaprClientBuilder();
    }

  }

}
