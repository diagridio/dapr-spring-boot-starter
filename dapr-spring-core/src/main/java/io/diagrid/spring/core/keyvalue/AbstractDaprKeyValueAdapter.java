package io.diagrid.spring.core.keyvalue;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.GetStateRequest;
import io.dapr.client.domain.SaveStateRequest;
import io.dapr.client.domain.State;
import io.dapr.utils.TypeRef;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.util.CloseableIterator;
import org.springframework.util.Assert;

import java.util.Map;

public abstract class AbstractDaprKeyValueAdapter implements KeyValueAdapter {
    private static final Map<String, String> CONTENT_TYPE_META = Map.of("contentType", "application/json");

    private final DaprClient daprClient;
    private final String stateStoreName;

    protected AbstractDaprKeyValueAdapter(DaprClient daprClient, String stateStoreName) {
        Assert.notNull(daprClient, "DaprClient must not be null");
        Assert.hasText(stateStoreName, "State store name must not be empty");

        this.daprClient = daprClient;
        this.stateStoreName = stateStoreName;
    }

    @Override
    public void destroy() throws Exception {
        daprClient.close();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("'clear' method is not supported");
    }

    @Override
    public Object put(Object id, Object item, String keyspace) {
        Assert.notNull(id, "Id must not be null");
        Assert.notNull(item, "Item must not be null");
        Assert.hasText(keyspace, "Keyspace must not be empty");

        String key = resolveKey(keyspace, id);
        State<Object> state = new State<>(key, item, null, CONTENT_TYPE_META, null);
        SaveStateRequest request = new SaveStateRequest(stateStoreName).setStates(state);

        daprClient.saveBulkState(request).block();

        return item;
    }

    @Override
    public boolean contains(Object id, String keyspace) {
        return get(id, keyspace) != null;
    }

    @Override
    public Object get(Object id, String keyspace) {
        Assert.notNull(id, "Id must not be null");
        Assert.hasText(keyspace, "Keyspace must not be empty");

        String key = resolveKey(keyspace, id);

        return daprClient.getState(stateStoreName, key, Object.class).block().getValue();
    }

    @Override
    public <T> T get(Object id, String keyspace, Class<T> type) {
        Assert.notNull(id, "Id must not be null");
        Assert.hasText(keyspace, "Keyspace must not be empty");
        Assert.notNull(type, "Type must not be null");

        String key = resolveKey(keyspace, id);
        GetStateRequest stateRequest = new GetStateRequest(stateStoreName, key).setMetadata(CONTENT_TYPE_META);

        return daprClient.getState(stateRequest, TypeRef.get(type)).block().getValue();
    }

    @Override
    public Object delete(Object id, String keyspace) {
        Object result = get(id, keyspace);

        if (result == null) {
            return null;
        }

        String key = resolveKey(keyspace, id);

        daprClient.deleteState(stateStoreName, key).block();

        return result;
    }

    @Override
    public <T> T delete(Object id, String keyspace, Class<T> type) {
        T result = get(id, keyspace, type);

        if (result == null) {
            return null;
        }

        String key = resolveKey(keyspace, id);

        daprClient.deleteState(stateStoreName, key).block();

        return result;
    }

    @Override
    public Iterable<?> getAllOf(String keyspace) {
        return getAllOf(keyspace, Object.class);
    }

    @Override
    public CloseableIterator<Map.Entry<Object, Object>> entries(String keyspace) {
        throw new UnsupportedOperationException("'entries' method is not supported");
    }

    private String resolveKey(String keyspace, Object id) {
        return String.format("%s-%s", keyspace, id);
    }

}
