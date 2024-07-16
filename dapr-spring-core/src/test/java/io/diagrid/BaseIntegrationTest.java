package io.diagrid;

import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class BaseIntegrationTest {
    public static final Network DAPR_NETWORK = Network.newNetwork();
}
