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

package io.diagrid.spring.core.keyvalue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.diagrid.AbstractPostgreSQLBaseIT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for {@link PostgreSQLDaprKeyValueTemplateIT}.
 */
@SuppressWarnings("AbbreviationAsWordInName")
public class PostgreSQLDaprKeyValueTemplateIT extends AbstractPostgreSQLBaseIT {

  private final DaprClient daprClient = new DaprClientBuilder().build();
  private final ObjectMapper mapper = new ObjectMapper();
  private final KeyValueAdapterResolver daprKeyValueAdapter = new DaprKeyValueAdapterResolver(
      daprClient,
      mapper,
      STATE_STORE_NAME,
      BINDING_NAME
  );
  private final DaprKeyValueTemplate keyValueTemplate = new DaprKeyValueTemplate(daprKeyValueAdapter);

  /**
   * Cleans up the state store after each test.
   */
  @AfterEach
  public void tearDown() {
    var meta = Map.of("sql", "delete from state");

    daprClient.invokeBinding(BINDING_NAME, "exec", null, meta).block();
  }

  @Test
  public void testInsertAndQueryDaprKeyValueTemplate() {
    int itemId = 3;
    TestType savedType = keyValueTemplate.insert(new TestType(itemId, "test"));
    assertThat(savedType).isNotNull();

    Optional<TestType> findById = keyValueTemplate.findById(itemId, TestType.class);
    assertThat(findById.isEmpty()).isFalse();
    assertThat(findById.get()).isEqualTo(savedType);

    KeyValueQuery<String> keyValueQuery = new KeyValueQuery<>("content == 'test'");

    Iterable<TestType> myTypes = keyValueTemplate.find(keyValueQuery, TestType.class);
    assertThat(myTypes.iterator().hasNext()).isTrue();

    TestType item = myTypes.iterator().next();
    assertThat(item.getId()).isEqualTo(Integer.valueOf(itemId));
    assertThat(item.getContent()).isEqualTo("test");

    keyValueQuery = new KeyValueQuery<>("content == 'asd'");

    myTypes = keyValueTemplate.find(keyValueQuery, TestType.class);
    assertThat(!myTypes.iterator().hasNext()).isTrue();
  }

  @Test
  public void testInsertMoreThan10AndQueryDaprKeyValueTemplate() {
    int count = 10;
    List<TestType> items = new ArrayList<>();

    for (int i = 0; i < count; i++) {
      items.add(keyValueTemplate.insert(new TestType(i, "test")));
    }

    KeyValueQuery<String> keyValueQuery = new KeyValueQuery<>("content == 'test'");
    keyValueQuery.setRows(100);
    keyValueQuery.setOffset(0);

    Iterable<TestType> foundItems = keyValueTemplate.find(keyValueQuery, TestType.class);
    assertThat(foundItems.iterator().hasNext()).isTrue();

    int index = 0;

    for (TestType foundItem : foundItems) {
      TestType item = items.get(index);

      assertEquals(item.getId(), foundItem.getId());
      assertEquals(item.getContent(), foundItem.getContent());

      index++;
    }

    assertEquals(index, items.size());
  }

  @Test
  public void testUpdateDaprKeyValueTemplate() {
    int itemId = 2;
    TestType insertedType = keyValueTemplate.insert(new TestType(itemId, "test"));
    assertThat(insertedType).isNotNull();

    TestType updatedType = keyValueTemplate.update(new TestType(itemId, "test2"));
    assertThat(updatedType).isNotNull();
  }

  @Test
  public void testDeleteAllOfDaprKeyValueTemplate() {
    int itemId = 1;
    TestType insertedType = keyValueTemplate.insert(new TestType(itemId, "test"));
    assertThat(insertedType).isNotNull();

    keyValueTemplate.delete(TestType.class);

    Optional<TestType> result = keyValueTemplate.findById(itemId, TestType.class);

    assertThat(result).isEmpty();
  }

  @Test
  public void testGetAllOfDaprKeyValueTemplate() {
    int itemId = 1;
    TestType insertedType = keyValueTemplate.insert(new TestType(itemId, "test"));
    assertThat(insertedType).isNotNull();

    Iterable<TestType> result = keyValueTemplate.findAll(TestType.class);

    assertThat(result.iterator().hasNext()).isTrue();
  }

  @Test
  public void testCountDaprKeyValueTemplate() {
    int itemId = 1;
    TestType insertedType = keyValueTemplate.insert(new TestType(itemId, "test"));
    assertThat(insertedType).isNotNull();

    long result = keyValueTemplate.count(TestType.class);

    assertThat(result).isEqualTo(1);
  }

  @Test
  public void testCountWithQueryDaprKeyValueTemplate() {
    int itemId = 1;
    TestType insertedType = keyValueTemplate.insert(new TestType(itemId, "test"));
    assertThat(insertedType).isNotNull();

    KeyValueQuery<String> keyValueQuery = new KeyValueQuery<>("content == 'test'");
    keyValueQuery.setRows(100);
    keyValueQuery.setOffset(0);

    long result = keyValueTemplate.count(keyValueQuery, TestType.class);

    assertThat(result).isEqualTo(1);
  }

}
