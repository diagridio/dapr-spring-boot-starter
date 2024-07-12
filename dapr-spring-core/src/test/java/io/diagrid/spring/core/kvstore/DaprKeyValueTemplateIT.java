package io.diagrid.spring.core.kvstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.diagrid.BaseIntegrationTest;
import io.diagrid.spring.core.keyvalue.DaprKeyValueAdapter;
import io.diagrid.spring.core.keyvalue.DaprKeyValueTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Integration tests for {@link DaprKeyValueTemplateIT}.
 */
public class DaprKeyValueTemplateIT extends BaseIntegrationTest {
    private final DaprClient daprClient = new DaprClientBuilder().build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final DaprKeyValueAdapter daprKeyValueAdapter = new DaprKeyValueAdapter(
            daprClient,
            mapper,
            "kvstore",
            "kvbinding"
    );
    private final DaprKeyValueTemplate keyValueTemplate = new DaprKeyValueTemplate(daprKeyValueAdapter);

    @AfterEach
    public void tearDown() {
        var meta = Map.of("sql", "delete from state");

        daprClient.invokeBinding("kvbinding", "exec", null, meta).block();
    }

    @Test
    public void testInsertAndQueryDaprKeyValueTemplate() {
        var itemId = 3;
        var savedType = keyValueTemplate.insert(new TestType(itemId, "test"));
        assertThat(savedType).isNotNull();

        var findById = keyValueTemplate.findById(itemId, TestType.class).get();
        assertThat(findById).isNotNull();
        assertThat(findById).isEqualTo(savedType);

        KeyValueQuery<String> keyValueQuery = new KeyValueQuery<String>("'content' == 'test'");

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

        var index = 0;

        for(TestType foundItem : foundItems){
            var item = items.get(index);

            assertEquals(item.getId(), foundItem.getId());
            assertEquals(item.getContent(), foundItem.getContent());

            index++;
        }

        assertEquals(index, items.size());
    }

    @Test
    public void testUpdateDaprKeyValueTemplate() {
        var itemId = 2;
        var insertedType = keyValueTemplate.insert(new TestType(itemId, "test"));
        assertThat(insertedType).isNotNull();

        var updatedType = keyValueTemplate.update(new TestType(itemId, "test2"));
        assertThat(updatedType).isNotNull();
    }

}
