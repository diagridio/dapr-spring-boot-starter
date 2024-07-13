package io.diagrid.spring.core.keyvalue;


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.util.CloseableIterator;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.ComponentMetadata;
import io.dapr.client.domain.DaprMetadata;
import io.dapr.client.domain.GetStateRequest;
import io.dapr.client.domain.SaveStateRequest;
import io.dapr.client.domain.State;
import io.dapr.utils.TypeRef;

public class DaprKeyValueAdapter implements KeyValueAdapter {
    private static final Map<String, String> CONTENT_TYPE_META = Map.of("contentType", "application/json");
    private static final String DELETE_BY_KEYSPACE_PATTERN = "delete from state where key LIKE '%s'";
    private static final String SELECT_BY_KEYSPACE_PATTERN = "select regexp_replace(value#>>'{}', '\"', '\"', 'g') as value from state where key LIKE '%s'";
    private static final String SELECT_BY_FILTER_PATTERN = "select regexp_replace(value#>>'{}', '\"', '\"', 'g') as value from state where key LIKE '%s' and value->>%s=%s";
    private static final String COUNT_BY_KEYSPACE_PATTERN = "select count(*) as value from state where key LIKE '%s'";
    private static final String COUNT_BY_FILTER_PATTERN = "select count(*) as value from state where key LIKE '%s' and value->>%s=%s";

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    private final DaprClient daprClient;
    private final ObjectMapper mapper;
    private final String stateStoreName;
    private final String stateStoreBinding;

    public DaprKeyValueAdapter(DaprClient daprClient, ObjectMapper mapper, String stateStoreName, String stateStoreBinding) {
        this.daprClient = daprClient;
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
        var key = resolveKey(keyspace, id);
        var state = new State<>(key, item, null, CONTENT_TYPE_META, null);
        var request = new SaveStateRequest(stateStoreName).setStates(state);

        daprClient.saveBulkState(request).block();

        return item;
    }

    @Override
    public boolean contains(Object id, String keyspace) {
        return get(id, keyspace) != null;
    }

    @Override
    public Object get(Object id, String keyspace) {
        var key = resolveKey(keyspace, id);

        return daprClient.getState(stateStoreName, key, Object.class).block().getValue();
    }

    @Override
    public <T> T get(Object id, String keyspace, Class<T> type) {
        var key = resolveKey(keyspace, id);
        var stateRequest = new GetStateRequest(stateStoreName, key).setMetadata(CONTENT_TYPE_META);

        return daprClient.getState(stateRequest, TypeRef.get(type)).block().getValue();
    }

    @Override
    public Object delete(Object id, String keyspace) {
        var result = get(id, keyspace);

        if (result == null) {
            return null;
        }

        var key = resolveKey(keyspace, id);

        daprClient.deleteState(stateStoreName, key).block();

        return result;
    }

    @Override
    public <T> T delete(Object id, String keyspace, Class<T> type) {
        var result = get(id, keyspace, type);

        if (result == null) {
            return null;
        }

        var key = resolveKey(keyspace, id);

        daprClient.deleteState(stateStoreName, key).block();

        return result;
    }

    @Override
    public Iterable<?> getAllOf(String keyspace) {
        return getAllOf(keyspace, Object.class);
    }

    @Override
    public <T> Iterable<T> getAllOf(String keyspace, Class<T> type) {
        var sql = createSql(SELECT_BY_KEYSPACE_PATTERN, keyspace);
        var meta = Map.of("sql", sql);
        var typeRef = new TypeRef<List<List<String>>>() {};
        var result = daprClient.invokeBinding(stateStoreBinding, "query", null, meta, typeRef).block();

        return result.stream()
                .flatMap(Collection::stream)
                .map(string -> deserialize(string, type))
                .toList();
    }

    @Override
    public CloseableIterator<Entry<Object, Object>> entries(String keyspace) {
        throw new UnsupportedOperationException("'entries' method is not supported");
    }

    @Override
    public void deleteAllOf(String keyspace) {
        var sql = createSql(DELETE_BY_KEYSPACE_PATTERN, keyspace);
        var meta = Map.of("sql", sql);

        daprClient.invokeBinding(stateStoreBinding, "exec", null, meta).block();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("'clear' method is not supported");
    }

    @Override
    public <T> Iterable<T> find(KeyValueQuery<?> query, String keyspace, Class<T> type) {
        var metadata = daprClient.getMetadata().block();
        var components = metadata.getComponents();
        var stateStoreTypeAndVersion = findStateStoreTypeAndVersion(components);

        if (stateStoreTypeAndVersion.isEmpty()) {
            return Collections.emptyList();
        }

        if (!stateStoreTypeAndVersion.equals("state.postgresql-v1")) {
            return Collections.emptyList();
        }

        var sql = createSql(SELECT_BY_FILTER_PATTERN, keyspace, query);
        var meta = Map.of("sql", sql);
        var typeRef = new TypeRef<List<List<String>>>() {};
        var result = daprClient.invokeBinding(stateStoreBinding, "query", null, meta, typeRef).block();

        return result.stream()
                .flatMap(Collection::stream)
                .map(string -> deserialize(string, type))
                .toList();
    }

    @Override
    public long count(String keyspace) {
        var sql = createSql(COUNT_BY_KEYSPACE_PATTERN, keyspace);
        var meta = Map.of("sql", sql);
        var typeRef = new TypeRef<List<List<Long>>>() {};
        var result = daprClient.invokeBinding(stateStoreBinding, "query", null, meta, typeRef).block();

        return result.stream()
                .flatMap(Collection::stream)
                .toList()
                .get(0);
    }

    @Override
    public long count(KeyValueQuery<?> query, String keyspace) {
        var sql = createSql(COUNT_BY_FILTER_PATTERN, keyspace, query);
        var meta = Map.of("sql", sql);
        var typeRef = new TypeRef<List<List<Long>>>() {};
        var result = daprClient.invokeBinding(stateStoreBinding, "query", null, meta, typeRef).block();

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

    private String createSql(String sqlPattern, String keyspace) {
        var keyspaceFilter = getKeyspaceFilter(keyspace);

        return String.format(sqlPattern, keyspaceFilter);
    }

    private String createSql(String sqlPattern, String keyspace, KeyValueQuery<?> query) {
        var expression = PARSER.parseRaw(query.getCriteria().toString());
        var left = expression.getAST().getChild(0);
        var right = expression.getAST().getChild(1);
        var keyspaceFilter = getKeyspaceFilter(keyspace);

        return String.format(sqlPattern, keyspaceFilter, left, right);
    }

    private String getKeyspaceFilter(String keyspace) {
        return String.format("%s||%s-%%", stateStoreName, keyspace);
    }

    private <T> T deserialize(String string, Class<T> type) {
        try {
            return mapper.readValue(string, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
