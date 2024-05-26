package io.diagrid.spring.core.client;

import io.dapr.client.DaprClientBuilder;

/**
 * Callback interface that can be used to customize a {@link DaprClientBuilder}.
 */
@FunctionalInterface
public interface DaprClientCustomizer {

    /**
     * Callback to customize a {@link DaprClientBuilder} instance.
     * @param daprClientBuilder the client builder to customize
     */
    void customize(DaprClientBuilder daprClientBuilder);

}
