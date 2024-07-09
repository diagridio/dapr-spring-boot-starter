package io.diagrid.spring.core.keyvalue;


import java.util.HashMap;
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

    private String queryIndexName;
    private String statestoreName;
    private int queryMaxResults = 1000;
    private DaprClient daprClient;
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();
    private ObjectMapper mapper = new ObjectMapper();

    public DaprKeyValueAdapter(DaprClient daprClient, String statestoreName) {
        this.daprClient = daprClient;
        this.statestoreName = statestoreName;
    }

    @Override
    public void destroy() throws Exception {
        daprClient.close();
    }

    @Override
    public Object put(Object id, Object item, String keyspace) {
        Map<String, String> meta = Map.of("contentType", "application/json");
        SaveStateRequest request = new SaveStateRequest(statestoreName)
                .setStates(new State<>(keyspace + "-" + id.toString(), item, null, meta, null));

        daprClient.saveBulkState(request).block();

        return item;
    }

    @Override
    public boolean contains(Object id, String keyspace) {
        return (get(id, keyspace) != null);

    }

    @Override
    public Object get(Object id, String keyspace) {
        return daprClient.getState(statestoreName, keyspace + "-" + id.toString(), Object.class).block().getValue();
    }

    @Override
    public <T> T get(Object id, String keyspace, Class<T> type) {
        Map<String, String> meta = Map.of("contentType", "application/json");
        GetStateRequest stateRequest = new GetStateRequest(statestoreName, keyspace + "-" + id.toString())
                .setMetadata(meta);
        return daprClient.getState(stateRequest, TypeRef.get(type)).block().getValue();
    }

    @Override
    public Object delete(Object id, String keyspace) {
        daprClient.deleteState(statestoreName, keyspace + "-" + id.toString()).block();
        return null;
    }

    @Override
    public <T> T delete(Object id, String keyspace, Class<T> type) {
        // TODO Auto-generated method stub
        // daprClient.deleteState(keyspace, id.toString()).block();
        return null;
    }

    @Override
    public Iterable<?> getAllOf(String keyspace) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllOf'");
    }

    @Override
    public CloseableIterator<Entry<Object, Object>> entries(String keyspace) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'entries'");
    }

    @Override
    public void deleteAllOf(String keyspace) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllOf'");
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }

    private String findStatestoreTypeAndVersion(String statestoreName, List<ComponentMetadata> components) {

        for (ComponentMetadata cm : components) {
            if (cm.getName().equals(statestoreName)) {
                return cm.getType() + "-" + cm.getVersion();
            }
        }
        return "";
    }

     

    @Override
    public <T> Iterable<T> find(KeyValueQuery<?> query, String keyspace, Class<T> type) {

        DaprMetadata metadata = daprClient.getMetadata().block();
        List<ComponentMetadata> components = metadata.getComponents();
        String statestoreTypeAndVersion = findStatestoreTypeAndVersion(statestoreName, components);


        SpelExpression expression = PARSER.parseRaw(query.getCriteria().toString());
        SpelNode left = expression.getAST().getChild(0);
        SpelNode right = expression.getAST().getChild(1);

        String sqlText = "select regexp_replace(value#>>'{}', '\"', '\"', 'g') as value from state where key LIKE '" + statestoreName + "||" + type.getName() + "-%' and value->>"+left+"="+right+"";
        System.out.println(sqlText);

        Map<String, String> queryMetadata = new HashMap<>();
        queryMetadata.put("sql", sqlText);

        if (!statestoreTypeAndVersion.equals("")) {
            if (statestoreTypeAndVersion.equals("state.postgresql-v1")) {
                return daprClient.invokeBindingList("kvbinding", "query", null, queryMetadata, TypeRef.get(type)).block();
            }
        }
        return null;
    }

    @Override
    public long count(String keyspace) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'count'");
    }

    @Override
    public long count(KeyValueQuery<?> query, String keyspace) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'count'");
    }


    
}
