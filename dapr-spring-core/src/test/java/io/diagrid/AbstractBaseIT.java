package io.diagrid;

import io.diagrid.dapr.DaprContainer;
import io.diagrid.dapr.QuotedBoolean;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.Map;

@Testcontainers
public abstract class AbstractBaseIT {
    public static final Network DAPR_NETWORK = Network.newNetwork();
    public static final String STATE_STORE_NAME = "kvstore";
    public static final String BINDING_NAME = "kvbinding";
    public static final String PUBSUB_NAME = "pubsub";
}
