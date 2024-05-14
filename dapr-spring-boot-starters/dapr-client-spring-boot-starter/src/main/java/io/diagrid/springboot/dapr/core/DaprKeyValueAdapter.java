package io.diagrid.springboot.dapr.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.util.CloseableIterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprPreviewClient;
import io.dapr.client.domain.QueryStateItem;
import io.dapr.client.domain.QueryStateRequest;
import io.dapr.client.domain.QueryStateResponse;
import io.dapr.client.domain.query.Query;
import io.dapr.client.domain.query.Sorting;
import io.dapr.client.domain.query.filters.EqFilter;

public class DaprKeyValueAdapter implements KeyValueAdapter{

    
    private DaprClient daprClient;
    private DaprPreviewClient daprPreviewClient;

    public DaprKeyValueAdapter(DaprClient daprClient, DaprPreviewClient daprPreviewClient) {
        this.daprClient = daprClient;
        this.daprPreviewClient = daprPreviewClient;
    }

    @Override
    public void destroy() throws Exception {
        daprClient.close();
    }

    @Override
    public Object put(Object id, Object item, String keyspace) {
        daprClient.saveState("kvstore", keyspace + "-" +id.toString(), item).block();
        return item;
    }

    @Override
    public boolean contains(Object id, String keyspace) {
        return (get(id, keyspace) != null);
        
    }

    @Override
    public Object get(Object id, String keyspace) {
        // TODO Auto-generated method stub
        return daprClient.getState("kvstore", keyspace + "-" + id.toString(), Object.class).block().getValue();
    }

    @Override
    public <T> T get(Object id, String keyspace, Class<T> type) {
        return (T)daprClient.getState("kvstore", keyspace + "-" + id.toString(), type).block().getValue();
    }

    @Override
    public Object delete(Object id, String keyspace) {
        // TODO Auto-generated method stub
        daprClient.deleteState("kvstore", keyspace + "-" + id.toString()).block();
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
        
        Query daprQuery = new Query()
          .setFilter(new EqFilter<>("type", "vote"))
          .setSort(Arrays.asList(new Sorting("type", Sorting.Order.DESC)));

        QueryStateRequest queryStateRequest = new QueryStateRequest("kvstore")
            .setQuery(daprQuery);

        QueryStateResponse<T> queryResults = daprPreviewClient.queryState(queryStateRequest, type).block();
        
        List<T> itemResults = new ArrayList<T>(queryResults.getResults().size());
        for(QueryStateItem<T> item : queryResults.getResults()){
            itemResults.add(item.getValue());
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
