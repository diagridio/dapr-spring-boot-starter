package io.diagrid.spring.core.keyvalue;


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.dapr.client.domain.ComponentMetadata;
import io.dapr.client.domain.DaprMetadata;
import io.dapr.client.domain.GetStateRequest;
import io.dapr.client.domain.SaveStateRequest;
import io.dapr.client.domain.State;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.util.CloseableIterator;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dapr.client.DaprClient;
import io.dapr.utils.TypeRef;
import org.springframework.util.Assert;

public class DaprKeyValueAdapter implements KeyValueAdapter {
    private static final Map<String, String> CONTENT_TYPE_META = Map.of("contentType", "application/json");
    private static final TypeRef<List<List<Object>>> FILTER_TYPE_REF = new TypeRef<>() {};
    private static final TypeRef<List<List<Long>>> COUNT_TYPE_REF = new TypeRef<>() {};

    private final DaprClient daprClient;
    private final QueryTranslator queryTranslator;
    private final ObjectMapper mapper;
    private final String stateStoreName;
    private final String stateStoreBinding;

    public DaprKeyValueAdapter(DaprClient daprClient, QueryTranslator queryTranslator, ObjectMapper mapper, String stateStoreName, String stateStoreBinding) {
        Assert.notNull(daprClient, "DaprClient must not be null");
        Assert.notNull(queryTranslator, "QueryTranslator must not be null");
        Assert.notNull(mapper, "ObjectMapper must not be null");
        Assert.hasText(stateStoreName, "State store name must not be empty");
        Assert.hasText(stateStoreBinding, "State store binding must not be empty");

        this.daprClient = daprClient;
        this.queryTranslator = queryTranslator;
        this.mapper = mapper;
        this.stateStoreName = stateStoreName;
        this.stateStoreBinding = stateStoreBinding;
    }

    @Override
    public void destroy() throws Exception {
        daprClient.close();
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
    public <T> Iterable<T> getAllOf(String keyspace, Class<T> type) {
        Assert.hasText(keyspace, "Keyspace must not be empty");
        Assert.notNull(type, "Type must not be null");

        String sql = queryTranslator.translateFind(keyspace);
        List<List<Object>> result = queryUsingBinding(sql, FILTER_TYPE_REF);

        return result.stream()
                .flatMap(Collection::stream)
                .map(value -> convertValue(value, type))
                .toList();
    }

    @Override
    public CloseableIterator<Entry<Object, Object>> entries(String keyspace) {
        throw new UnsupportedOperationException("'entries' method is not supported");
    }

    @Override
    public void deleteAllOf(String keyspace) {
        Assert.hasText(keyspace, "Keyspace must not be empty");

        String sql = queryTranslator.translateDelete(keyspace);

        execUsingBinding(sql);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("'clear' method is not supported");
    }

    @Override
    public <T> Iterable<T> find(KeyValueQuery<?> query, String keyspace, Class<T> type) {
        Assert.notNull(query, "Query must not be null");
        Assert.hasText(keyspace, "Keyspace must not be empty");
        Assert.notNull(type, "Type must not be null");

        DaprMetadata metadata = daprClient.getMetadata().block();
        List<ComponentMetadata> components = metadata.getComponents();
        String stateStoreTypeAndVersion = findStateStoreTypeAndVersion(components);

        if (stateStoreTypeAndVersion.isEmpty()) {
            return Collections.emptyList();
        }

        if (!stateStoreTypeAndVersion.equals("state.postgresql-v1")) {
            return Collections.emptyList();
        }

        String sql = queryTranslator.translateFind(keyspace, query);
        List<List<Object>> result = queryUsingBinding(sql, FILTER_TYPE_REF);

        return result.stream()
                .flatMap(Collection::stream)
                .map(value -> convertValue(value, type))
                .toList();
    }

    @Override
    public long count(String keyspace) {
        Assert.hasText(keyspace, "Keyspace must not be empty");

        String sql = queryTranslator.translateCount(keyspace);
        List<List<Long>> result = queryUsingBinding(sql, COUNT_TYPE_REF);

        return result.stream()
                .flatMap(Collection::stream)
                .toList()
                .get(0);
    }

    @Override
    public long count(KeyValueQuery<?> query, String keyspace) {
        Assert.notNull(query, "Query must not be null");
        Assert.hasText(keyspace, "Keyspace must not be empty");

        String sql = queryTranslator.translateCount(keyspace, query);
        List<List<Long>> result = queryUsingBinding(sql, COUNT_TYPE_REF);

        return result.stream()
                .flatMap(Collection::stream)
                .toList()
                .get(0);
    }

    private String findStateStoreTypeAndVersion(List<ComponentMetadata> components) {
        for (ComponentMetadata cm : components) {
            if (cm.getName().equals(stateStoreName)) {
                return cm.getType() + "-" + cm.getVersion();
            }
        }
        return "";
    }

    private String resolveKey(String keyspace, Object id) {
        return String.format("%s-%s", keyspace, id);
    }

    private void execUsingBinding(String sql) {
        Map<String, String> meta = Map.of("sql", sql);

        daprClient.invokeBinding(stateStoreBinding, "exec", null, meta).block();
    }

    private <T> T queryUsingBinding(String sql, TypeRef<T> typeRef) {
        Map<String, String> meta = Map.of("sql", sql);

        return daprClient.invokeBinding(stateStoreBinding, "query", null, meta, typeRef).block();
    }

    private <T> T convertValue(Object value, Class<T> type) {
        try {
            return mapper.convertValue(value, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
