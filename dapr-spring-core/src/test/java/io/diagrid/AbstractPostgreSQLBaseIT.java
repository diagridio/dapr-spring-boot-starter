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

package io.diagrid;

import io.diagrid.dapr.DaprContainer;
import io.diagrid.dapr.QuotedBoolean;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.Collections;
import java.util.Map;

@SuppressWarnings("AbbreviationAsWordInName")
public abstract class AbstractPostgreSQLBaseIT extends AbstractBaseIT {
  private static final String CONNECTION_STRING =
      "host=postgres user=postgres password=password port=5432 connect_timeout=10 database=dapr_db";
  private static final Map<String, Object> STATE_STORE_PROPERTIES = Map.of(
      "keyPrefix", "name",
      "actorStateStore", new QuotedBoolean("true"),
      "connectionString", CONNECTION_STRING
  );

  private static final Map<String, Object> BINDING_PROPERTIES = Map.of(
      "connectionString", CONNECTION_STRING
  );

  @Container
  private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:16-alpine")
      .withNetworkAliases("postgres")
      .withDatabaseName("dapr_db")
      .withUsername("postgres")
      .withPassword("password")
      .withExposedPorts(5432)
      .withNetwork(DAPR_NETWORK);

  @Container
  private static final DaprContainer DAPR_CONTAINER = new DaprContainer("daprio/daprd:1.13.2")
      .withAppName("local-dapr-app")
      .withNetwork(DAPR_NETWORK)
      .withComponent(new DaprContainer.Component(STATE_STORE_NAME, "state.postgresql", "v1", STATE_STORE_PROPERTIES))
      .withComponent(new DaprContainer.Component(BINDING_NAME, "bindings.postgresql", "v1", BINDING_PROPERTIES))
      .withComponent(new DaprContainer.Component(PUBSUB_NAME, "pubsub.in-memory", "v1", Collections.emptyMap()))
      .withAppPort(8080)
      .withDaprLogLevel(DaprContainer.DaprLogLevel.debug)
      .withAppChannelAddress("host.testcontainers.internal")
      .dependsOn(POSTGRE_SQL_CONTAINER);

  @BeforeAll
  static void beforeAll() {
    org.testcontainers.Testcontainers.exposeHostPorts(8080);
    System.setProperty("dapr.grpc.port", Integer.toString(DAPR_CONTAINER.getGrpcPort()));
    System.setProperty("dapr.http.port", Integer.toString(DAPR_CONTAINER.getHttpPort()));
  }

}
