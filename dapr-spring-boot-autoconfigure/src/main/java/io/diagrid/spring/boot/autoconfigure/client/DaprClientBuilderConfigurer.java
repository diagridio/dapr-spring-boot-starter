package io.diagrid.spring.boot.autoconfigure.client;

import io.dapr.client.DaprClientBuilder;
import io.diagrid.spring.core.client.DaprClientCustomizer;

import java.util.List;

/**
 * Builder for configuring a {@link DaprClientBuilder}.
 */
public class DaprClientBuilderConfigurer {

    private List<DaprClientCustomizer> customizers;

    void setDaprClientCustomizer(List<DaprClientCustomizer> customizers) {
        this.customizers = customizers;
    }

    /**
     * Configure the specified {@link DaprClientBuilder}. The builder can be further
     * tuned and default settings can be overridden.
     * @param builder the {@link DaprClientBuilder} instance to configure
     * @return the configured builder
     */
    public DaprClientBuilder configure(DaprClientBuilder builder) {
        applyCustomizers(builder);
        return builder;
    }

    private void applyCustomizers(DaprClientBuilder builder) {
        if (this.customizers != null) {
            for (DaprClientCustomizer customizer : this.customizers) {
                customizer.customize(builder);
            }
        }
    }

}
