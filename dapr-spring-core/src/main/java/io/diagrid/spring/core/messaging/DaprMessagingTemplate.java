package io.diagrid.spring.core.messaging;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.Metadata;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import static java.util.Collections.singletonMap;

public class DaprMessagingTemplate<T> implements DaprMessagingOperations<T>, ApplicationContextAware, BeanNameAware {
    
    private String beanName = "";

    @Nullable
	private ApplicationContext applicationContext;

	private final DaprClient daprClient;
	
	private String pubsubName;
	
	public DaprMessagingTemplate(DaprClient daprClient, String pubsubName) {
		this.daprClient = daprClient;
		this.pubsubName = pubsubName;
	}

	private String MESSAGE_TTL_IN_SECONDS = "10";

    @Override
	public Void send(@Nullable String topic, @Nullable T message) {
		return doSend(topic, message);
	}

    @Override
    public void setBeanName(String beanName){
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

	private Void doSend(@Nullable String topic, @Nullable T message){
		
		
			return doSendAsync(topic, message).block();
		
	}

	private Mono<Void> doSendAsync(@Nullable String topic, @Nullable T message){
			System.out.println("Publishing Event From Dapr Messaging Template to Pubsub: " + pubsubName + " and topic: " + topic + " Message: " + message);
			return daprClient.publishEvent(pubsubName,
				topic,
				message,
				singletonMap(Metadata.TTL_IN_SECONDS, MESSAGE_TTL_IN_SECONDS));
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
