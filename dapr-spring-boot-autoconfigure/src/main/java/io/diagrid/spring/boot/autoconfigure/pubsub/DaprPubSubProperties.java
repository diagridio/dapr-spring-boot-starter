package io.diagrid.spring.boot.autoconfigure.pubsub;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = DaprPubSubProperties.CONFIG_PREFIX)
public class DaprPubSubProperties {

    public static final String CONFIG_PREFIX = "dapr.pubsub";

    /**
     * Name of the PubSub Dapr component.
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
