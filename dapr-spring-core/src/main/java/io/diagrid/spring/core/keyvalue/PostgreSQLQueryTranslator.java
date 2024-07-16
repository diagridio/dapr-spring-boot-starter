package io.diagrid.spring.core.keyvalue;

import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;

public class PostgreSQLQueryTranslator implements QueryTranslator {
    private static final String DELETE_BY_KEYSPACE_PATTERN = "delete from state where key LIKE '%s'";
    private static final String SELECT_BY_KEYSPACE_PATTERN = "select value from state where key LIKE '%s'";
    private static final String SELECT_BY_FILTER_PATTERN = "select value from state where key LIKE '%s' and value->>%s=%s";
    private static final String COUNT_BY_KEYSPACE_PATTERN = "select count(*) as value from state where key LIKE '%s'";
    private static final String COUNT_BY_FILTER_PATTERN = "select count(*) as value from state where key LIKE '%s' and value->>%s=%s";

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    private final String stateStoreName;

    public PostgreSQLQueryTranslator(String stateStoreName) {
        Assert.hasText(stateStoreName, "State store name must not be empty");

        this.stateStoreName = stateStoreName;
    }

    @Override
    public String translateDelete(String keyspace) {
        return createSql(DELETE_BY_KEYSPACE_PATTERN, keyspace);
    }

    @Override
    public String translateFind(String keyspace) {
        return createSql(SELECT_BY_KEYSPACE_PATTERN, keyspace);
    }

    @Override
    public String translateFind(String keyspace, KeyValueQuery<?> query) {
        if (query.getCriteria() == null) {
            return createSql(SELECT_BY_KEYSPACE_PATTERN, keyspace);
        }

        return createSql(SELECT_BY_FILTER_PATTERN, keyspace, query);
    }

    @Override
    public String translateCount(String keyspace) {
        return createSql(COUNT_BY_KEYSPACE_PATTERN, keyspace);
    }

    @Override
    public String translateCount(String keyspace, KeyValueQuery<?> query) {
        if (query.getCriteria() == null) {
            return createSql(COUNT_BY_KEYSPACE_PATTERN, keyspace);
        }

        return createSql(COUNT_BY_FILTER_PATTERN, keyspace, query);
    }

    private String getKeyspaceFilter(String keyspace) {
        return String.format("%s||%s-%%", stateStoreName, keyspace);
    }

    private String createSql(String sqlPattern, String keyspace) {
        String keyspaceFilter = getKeyspaceFilter(keyspace);

        return String.format(sqlPattern, keyspaceFilter);
    }

    private String createSql(String sqlPattern, String keyspace, KeyValueQuery<?> query) {
        String keyspaceFilter = getKeyspaceFilter(keyspace);
        SpelExpression expression = PARSER.parseRaw(query.getCriteria().toString());
        SpelNode left = expression.getAST().getChild(0);
        SpelNode right = expression.getAST().getChild(1);

        return String.format(sqlPattern, keyspaceFilter, left, right);
    }
}
