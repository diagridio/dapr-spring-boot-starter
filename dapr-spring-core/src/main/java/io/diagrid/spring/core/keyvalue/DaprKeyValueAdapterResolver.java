package io.diagrid.spring.core.keyvalue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.ComponentMetadata;
import io.dapr.client.domain.DaprMetadata;
import org.springframework.data.keyvalue.core.KeyValueAdapter;

import java.util.List;
import java.util.Set;

public class DaprKeyValueAdapterResolver implements KeyValueAdapterResolver {
    private static final Set<String> MYSQL_MARKERS = Set.of("state.mysql-v1", "bindings.mysql-v1");
    private static final Set<String> POSTGRESQL_MARKERS = Set.of("state.postgresql-v1", "bindings.postgresql-v1");
    private final DaprClient daprClient;
    private final ObjectMapper mapper;
    private final String stateStoreName;
    private final String bindingName;

    public DaprKeyValueAdapterResolver(DaprClient daprClient, ObjectMapper mapper, String stateStoreName, String bindingName) {
        this.daprClient = daprClient;
        this.mapper = mapper;
        this.stateStoreName = stateStoreName;
        this.bindingName = bindingName;
    }

    @Override
    public KeyValueAdapter resolve() {
        DaprMetadata metadata = daprClient.getMetadata().block();
        List<ComponentMetadata> components = metadata.getComponents();

        if (components == null || components.isEmpty()) {
            throw new IllegalArgumentException("No components found in Dapr metadata");
        }

        if (shouldUseMySQL(components, stateStoreName, bindingName)) {
            return new MySQLDaprKeyValueAdapter(daprClient, mapper, stateStoreName, bindingName);
        }

        if (shouldUsePostgreSQL(components, stateStoreName, bindingName)) {
            return new PostgreSQLDaprKeyValueAdapter(daprClient, mapper, stateStoreName, bindingName);
        }

        throw new IllegalArgumentException("Could find any adapter matching the given state store and binding");
    }

    private boolean shouldUseMySQL(List<ComponentMetadata> components, String stateStoreName, String bindingName) {
        boolean stateStoreMatched = components.stream().anyMatch(x -> matchBy(stateStoreName, MYSQL_MARKERS, x));
        boolean bindingMatched = components.stream().anyMatch(x -> matchBy(bindingName, MYSQL_MARKERS, x));

        return stateStoreMatched && bindingMatched;
    }

    private boolean shouldUsePostgreSQL(List<ComponentMetadata> components, String stateStoreName, String bindingName) {
        boolean stateStoreMatched = components.stream().anyMatch(x -> matchBy(stateStoreName, POSTGRESQL_MARKERS, x));
        boolean bindingMatched = components.stream().anyMatch(x -> matchBy(bindingName, POSTGRESQL_MARKERS, x));

        return stateStoreMatched && bindingMatched;
    }

    private boolean matchBy(String name, Set<String> markers, ComponentMetadata componentMetadata) {
        return componentMetadata.getName().equals(name) && markers.contains(getTypeAndVersion(componentMetadata));
    }

    private String getTypeAndVersion(ComponentMetadata component) {
        return component.getType() + "-" + component.getVersion();
    }
}
