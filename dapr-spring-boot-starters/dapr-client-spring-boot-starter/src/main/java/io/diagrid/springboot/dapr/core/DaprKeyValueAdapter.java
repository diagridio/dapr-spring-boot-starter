package io.diagrid.springboot.dapr.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.util.CloseableIterator;
import org.springframework.expression.spel.ExpressionState;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprPreviewClient;
import io.dapr.client.domain.GetStateRequest;
import io.dapr.client.domain.QueryStateItem;
import io.dapr.client.domain.QueryStateRequest;
import io.dapr.client.domain.QueryStateResponse;
import io.dapr.client.domain.SaveStateRequest;
import io.dapr.client.domain.State;
import io.dapr.client.domain.query.Query;
import io.dapr.client.domain.query.filters.EqFilter;
import io.dapr.client.domain.query.filters.Filter;
import io.dapr.exceptions.DaprException;
import io.dapr.utils.TypeRef;

public class DaprKeyValueAdapter implements KeyValueAdapter{


    private String queryIndexName;
    private String statestoreName;
    
    private DaprClient daprClient;
    private DaprPreviewClient daprPreviewClient;
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    public DaprKeyValueAdapter(DaprClient daprClient, DaprPreviewClient daprPreviewClient, String statestoreName, String queryIndexName) {
        this.daprClient = daprClient;
        this.daprPreviewClient = daprPreviewClient;
        this.statestoreName = statestoreName;
        this.queryIndexName = queryIndexName;
    }

    @Override
    public void destroy() throws Exception {
        daprClient.close();
    }

    @Override
    public Object put(Object id, Object item, String keyspace) {
        Map<String, String> meta = Map.of("contentType", "application/json");
        SaveStateRequest request = new SaveStateRequest(statestoreName)
			 		.setStates(new State<>(keyspace + "-" +id.toString(), item, null, meta, null));

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
        GetStateRequest stateRequest = new GetStateRequest(statestoreName, keyspace + "-" + id.toString()).setMetadata(meta);
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
        //daprClient.deleteState(keyspace, id.toString()).block();
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

    @Override
    public <T> Iterable<T> find(KeyValueQuery<?> query, String keyspace, Class<T> type) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("contentType","application/json");
        metadata.put("queryIndexName", queryIndexName);

        System.out.println("Querying statestore: " + statestoreName + ", with query index: " + queryIndexName + " for type: " + type.getCanonicalName());
        SpelExpression expression = PARSER.parseRaw(query.getCriteria().toString());
        SpelNode left = expression.getAST().getChild(0);
        SpelNode right = expression.getAST().getChild(1);

        Filter filter = null;
        if(expression.getAST().getClass().getSimpleName().equals("OpEQ")  ){
            filter = new EqFilter<>(
                (String)left.getValue(new ExpressionState(new StandardEvaluationContext())), 
                right.getValue(new ExpressionState(new StandardEvaluationContext())));
        }
        
        Query daprQuery = new Query().setFilter(filter);

        QueryStateRequest queryStateRequest = new QueryStateRequest(statestoreName)
            .setQuery(daprQuery).setMetadata(metadata);

        QueryStateResponse<T> queryResults = null;
        List<T> itemResults = new ArrayList<T>();
        try{
            queryResults = daprPreviewClient.queryState(queryStateRequest, type).block();
            itemResults = new ArrayList<T>(queryResults.getResults().size());
            for(QueryStateItem<T> item : queryResults.getResults()){
                itemResults.add(item.getValue());
            }
        }catch(DaprException de){
            //TODO: deal with invalid output (related to json)
            de.printStackTrace();
        }
        
        return itemResults;
        
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
