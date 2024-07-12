package io.diagrid.spring.boot.autoconfigure.statestore;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = DaprStateStoreProperties.CONFIG_PREFIX)
public class DaprStateStoreProperties {

    public static final String CONFIG_PREFIX = "dapr.statestore";

    /**
     * Name of the StateStore Dapr component.
     */
    private String name;
    private String binding;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }
}
