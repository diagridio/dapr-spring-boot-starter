package io.diagrid.spring.core.messaging;

import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

public interface DaprMessagingOperations<T> {
    
    /**
	 * Sends a message to the specified topic in a blocking manner.
	 * @param topic the topic to send the message to or {@code null} to send to the
	 * default topic
	 * @param message the message to send
	 * @return the id assigned by the broker to the published message
	 */
	Void send(@Nullable String topic, @Nullable T message);

	/**
	 * Create a {@link SendMessageBuilder builder} for configuring and sending a message.
	 * @param message the payload of the message
	 * @return the builder to configure and send the message
	 */
	SendMessageBuilder<T> newMessage(@Nullable T message);

    /**
	 * Builder that can be used to configure and send a message. Provides more options
	 * than the basic send/sendAsync methods provided by {@link DaprKeyValueOperations}.
	 *
	 * @param <T> the message payload type
	 */
	interface SendMessageBuilder<T> {

		/**
		 * Specify the topic to send the message to.
		 * @param topic the destination topic
		 * @return the current builder with the destination topic specified
		 */
		SendMessageBuilder<T> withTopic(String topic);

		// /**
		//  * Specify the schema to use when sending the message.
		//  * @param schema the schema to use
		//  * @return the current builder with the schema specified
		//  */
		// SendMessageBuilder<T> withSchema(Schema<T> schema);

		/**
		

		// /**
		//  * Specifies the message customizer to use to further configure the message.
		//  * @param messageCustomizer the message customizer
		//  * @return the current builder with the message customizer specified
		//  */
		// SendMessageBuilder<T> withMessageCustomizer(TypedMessageBuilderCustomizer<T> messageCustomizer);

		// /**
		//  * Specifies the customizer to use to further configure the producer builder.
		//  * @param producerCustomizer the producer builder customizer
		//  * @return the current builder with the producer builder customizer specified
		//  */
		// SendMessageBuilder<T> withProducerCustomizer(ProducerBuilderCustomizer<T> producerCustomizer);

		/**
		 * Send the message in a blocking manner using the configured specification.
		 * @return the id assigned by the broker to the published message
		 */
		Void send();

		/**
		 * Uses the configured specification to send the message in a non-blocking manner.
		 * @return a future that holds the id assigned by the broker to the published
		 * message
		 */
		Mono<Void> sendAsync();
	}

}
