package io.diagrid.spring.core.kvstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.diagrid.BaseIntegrationTest;
import io.diagrid.dapr.DaprContainer;
import io.diagrid.dapr.QuotedBoolean;
import io.diagrid.spring.core.keyvalue.DaprKeyValueAdapter;
import io.diagrid.spring.core.keyvalue.DaprKeyValueTemplate;
import io.diagrid.spring.core.keyvalue.PostgreSQLQueryTranslator;
import io.diagrid.spring.core.keyvalue.QueryTranslator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for {@link MySQLDaprKeyValueTemplateIT}.
 */
public class MySQLDaprKeyValueTemplateIT extends BaseIntegrationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySQLDaprKeyValueTemplateIT.class);

    private static final String STATE_STORE_DSN = "mysql:password@tcp(mysql:3306)/";
    private static final String BINDING_DSN = "mysql:password@tcp(mysql:3306)/dapr_db";
    private static final String STATE_STORE_NAME = "kvstore";
    private static final String BINDING_NAME = "kvbinding";
    private static final Map<String, Object> STATE_STORE_PROPERTIES = Map.of(
            "keyPrefix", "name",
            "schemaName", "dapr_db",
            "actorStateStore", new QuotedBoolean("true"),
            "connectionString", STATE_STORE_DSN
    );

    private static final Map<String, Object> BINDING_PROPERTIES = Map.of(
            "url", BINDING_DSN
    );

    @Container
    private static final MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer<>("mysql:5.7.34")
            .withNetworkAliases("mysql")
            .withDatabaseName("dapr_db")
            .withUsername("mysql")
            .withPassword("password")
            .withExposedPorts(3306)
            .withNetwork(DAPR_NETWORK);

    @Container
    private static final DaprContainer DAPR_CONTAINER = new DaprContainer("daprio/daprd:1.13.2")
            .withAppName("local-dapr-app")
            .withNetwork(DAPR_NETWORK)
            .withComponent(new DaprContainer.Component("kvstore", "state.mysql", STATE_STORE_PROPERTIES))
            .withComponent(new DaprContainer.Component("kvbinding", "bindings.mysql", BINDING_PROPERTIES))
            .withComponent(new DaprContainer.Component("pubsub", "pubsub.in-memory", Collections.emptyMap()))
            .withAppPort(8080)
            .withDaprLogLevel(DaprContainer.DaprLogLevel.debug)
            .withAppChannelAddress("host.testcontainers.internal")
            .withLogConsumer(new Slf4jLogConsumer(LOGGER))
            .dependsOn(MY_SQL_CONTAINER);

    private final DaprClient daprClient = new DaprClientBuilder().build();
    private final QueryTranslator queryTranslator = new PostgreSQLQueryTranslator(STATE_STORE_NAME);
    private final ObjectMapper mapper = new ObjectMapper();
    private final DaprKeyValueAdapter daprKeyValueAdapter = new DaprKeyValueAdapter(
            daprClient,
            queryTranslator,
            mapper,
            STATE_STORE_NAME,
            BINDING_NAME
    );
    private final DaprKeyValueTemplate keyValueTemplate = new DaprKeyValueTemplate(daprKeyValueAdapter);

    @BeforeAll
    static void beforeAll() {
        org.testcontainers.Testcontainers.exposeHostPorts(8080);
        System.setProperty("dapr.grpc.port", Integer.toString(DAPR_CONTAINER.getGRPCPort()));
        System.setProperty("dapr.http.port", Integer.toString(DAPR_CONTAINER.getHTTPPort()));
    }

    @AfterEach
    public void tearDown() {
        var meta = Map.of("sql", "delete from state");

        daprClient.invokeBinding("kvbinding", "exec", null, meta).block();
    }

    @Test
    public void testInsertAndQueryDaprKeyValueTemplate() {
        int itemId = 3;
        TestType savedType = keyValueTemplate.insert(new TestType(itemId, "test"));
        assertThat(savedType).isNotNull();

        Optional<TestType> findById = keyValueTemplate.findById(itemId, TestType.class);
        assertThat(findById.isEmpty()).isFalse();
        assertThat(findById.get()).isEqualTo(savedType);

        KeyValueQuery<String> keyValueQuery = new KeyValueQuery<>("'content' == 'test'");

        Iterable<TestType> myTypes = keyValueTemplate.find(keyValueQuery, TestType.class);
        assertThat(myTypes.iterator().hasNext()).isTrue();

        TestType item = myTypes.iterator().next();
        assertThat(item.getId()).isEqualTo(Integer.valueOf(itemId));
        assertThat(item.getContent()).isEqualTo("test");

        keyValueQuery = new KeyValueQuery<>("'content' == 'asd'");

        myTypes = keyValueTemplate.find(keyValueQuery, TestType.class);
        assertThat(!myTypes.iterator().hasNext()).isTrue();
    }

    @Test
    public void testInsertMoreThan10AndQueryDaprKeyValueTemplate() {
        int count = 10;
        List<TestType> items = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            items.add(keyValueTemplate.insert(new TestType(i, "test")));
        }

        KeyValueQuery<String> keyValueQuery = new KeyValueQuery<>("'content' == 'test'");
        keyValueQuery.setRows(100);
        keyValueQuery.setOffset(0);

        Iterable<TestType> foundItems = keyValueTemplate.find(keyValueQuery, TestType.class);
        assertThat(foundItems.iterator().hasNext()).isTrue();

        int index = 0;

        for(TestType foundItem : foundItems){
            TestType item = items.get(index);

            assertEquals(item.getId(), foundItem.getId());
            assertEquals(item.getContent(), foundItem.getContent());

            index++;
        }

        assertEquals(index, items.size());
    }

    @Test
    public void testUpdateDaprKeyValueTemplate() {
        int itemId = 2;
        TestType insertedType = keyValueTemplate.insert(new TestType(itemId, "test"));
        assertThat(insertedType).isNotNull();

        TestType updatedType = keyValueTemplate.update(new TestType(itemId, "test2"));
        assertThat(updatedType).isNotNull();
    }

    @Test
    public void testDeleteAllOfDaprKeyValueTemplate() {
        int itemId = 1;
        TestType insertedType = keyValueTemplate.insert(new TestType(itemId, "test"));
        assertThat(insertedType).isNotNull();

        keyValueTemplate.delete(TestType.class);

        Optional<TestType> result = keyValueTemplate.findById(itemId, TestType.class);

        assertThat(result).isEmpty();
    }

    @Test
    public void testGetAllOfDaprKeyValueTemplate() {
        int itemId = 1;
        TestType insertedType = keyValueTemplate.insert(new TestType(itemId, "test"));
        assertThat(insertedType).isNotNull();

        Iterable<TestType> result = keyValueTemplate.findAll(TestType.class);

        assertThat(result.iterator().hasNext()).isTrue();
    }

    @Test
    public void testCountDaprKeyValueTemplate() {
        int itemId = 1;
        TestType insertedType = keyValueTemplate.insert(new TestType(itemId, "test"));
        assertThat(insertedType).isNotNull();

        long result = keyValueTemplate.count(TestType.class);

        assertThat(result).isEqualTo(1);
    }

    @Test
    public void testCountWithQueryDaprKeyValueTemplate() {
        int itemId = 1;
        TestType insertedType = keyValueTemplate.insert(new TestType(itemId, "test"));
        assertThat(insertedType).isNotNull();

        KeyValueQuery<String> keyValueQuery = new KeyValueQuery<>("'content' == 'test'");
        keyValueQuery.setRows(100);
        keyValueQuery.setOffset(0);

        long result = keyValueTemplate.count(keyValueQuery, TestType.class);

        assertThat(result).isEqualTo(1);
    }

}
