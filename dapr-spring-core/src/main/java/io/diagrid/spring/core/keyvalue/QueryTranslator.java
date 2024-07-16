package io.diagrid.spring.core.keyvalue;

import org.springframework.data.keyvalue.core.query.KeyValueQuery;

public interface QueryTranslator {
    String translateDelete(String keyspace);

    String translateFind(String keyspace);

    String translateFind(String keyspace, KeyValueQuery<?> query);

    String translateCount(String keyspace);

    String translateCount(String keyspace, KeyValueQuery<?> query);
}
