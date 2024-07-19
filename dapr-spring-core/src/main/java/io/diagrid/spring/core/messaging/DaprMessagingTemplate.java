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

package io.diagrid.spring.core.messaging;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.Metadata;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.util.Map;

public class DaprMessagingTemplate<T> implements DaprMessagingOperations<T>, ApplicationContextAware, BeanNameAware {

  private static final String MESSAGE_TTL_IN_SECONDS = "10";

  private final DaprClient daprClient;
  private final String pubsubName;

  @Nullable
  private ApplicationContext applicationContext;
  private String beanName;

  public DaprMessagingTemplate(DaprClient daprClient, String pubsubName) {
    this.daprClient = daprClient;
    this.pubsubName = pubsubName;
  }

  @Override
  public Void send(@Nullable String topic, @Nullable T message) {
    return doSend(topic, message);
  }

  @Override
  public void setBeanName(String beanName) {
    this.beanName = beanName;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  public SendMessageBuilder<T> newMessage(@Nullable T message) {
    return new SendMessageBuilderImpl<>(this, message);
  }

  private Void doSend(@Nullable String topic, @Nullable T message) {
    return doSendAsync(topic, message).block();
  }

  private Mono<Void> doSendAsync(@Nullable String topic, @Nullable T message) {
    return daprClient.publishEvent(pubsubName,
        topic,
        message,
        Map.of(Metadata.TTL_IN_SECONDS, MESSAGE_TTL_IN_SECONDS));
  }

  public static class SendMessageBuilderImpl<T> implements SendMessageBuilder<T> {

    private final DaprMessagingTemplate<T> template;

    @Nullable
    private final T message;

    @Nullable
    private String topic;

    SendMessageBuilderImpl(DaprMessagingTemplate<T> template, @Nullable T message) {
      this.template = template;
      this.message = message;
    }

    @Override
    public SendMessageBuilder<T> withTopic(String topic) {
      this.topic = topic;
      return this;
    }


    @Override
    public Void send() {
      return this.template.doSend(this.topic, this.message);
    }

    @Override
    public Mono<Void> sendAsync() {
      return this.template.doSendAsync(this.topic, this.message);
    }

  }

}
