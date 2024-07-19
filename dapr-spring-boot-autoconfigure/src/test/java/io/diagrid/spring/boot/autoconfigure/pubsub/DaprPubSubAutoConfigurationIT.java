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

import io.dapr.client.domain.CloudEvent;
import io.diagrid.spring.boot.autoconfigure.BaseIntegrationTest;
import io.diagrid.spring.boot.autoconfigure.client.DaprClientAutoConfiguration;
import io.diagrid.spring.core.messaging.DaprMessagingTemplate;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = {
        TestApplication.class,
        TestRestController.class,
        TestTypeRepository.class,
        DaprClientAutoConfiguration.class,
        DaprPubSubAutoConfiguration.class,
    },
    properties = {
        "dapr.pubsub.name=pubsub"
    }
)
public class DaprPubSubAutoConfigurationIT extends BaseIntegrationTest {

  private static final Logger logger = LoggerFactory.getLogger(DaprPubSubAutoConfigurationIT.class);

  private static final String TOPIC = "mockTopic";

  @Autowired
  private DaprMessagingTemplate<String> messagingTemplate;

  @Autowired
  private TestRestController testRestController;

  @Test
  public void testDaprMessagingTemplate() throws InterruptedException {
    for (int i = 0; i < 10; i++) {
      var msg = "ProduceAndReadWithPrimitiveMessageType:" + i;
      messagingTemplate.send(TOPIC, msg);
      logger.info("++++++PRODUCE {}------", msg);
    }

    // Wait for the messages to arrive
    Thread.sleep(1000);

    List<CloudEvent<String>> events = testRestController.getEvents();
    assertThat(events.size()).isEqualTo(10);
  }

}
