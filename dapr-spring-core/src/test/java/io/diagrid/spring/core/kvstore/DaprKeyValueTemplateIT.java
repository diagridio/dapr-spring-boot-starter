package io.diagrid.spring.core.kvstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.diagrid.BaseIntegrationTest;
import io.diagrid.spring.core.keyvalue.DaprKeyValueAdapter;
import io.diagrid.spring.core.keyvalue.DaprKeyValueTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Integration tests for {@link DaprKeyValueTemplateIT}.
 */
public class DaprKeyValueTemplateIT extends BaseIntegrationTest {
    private final DaprClient daprClient = new DaprClientBuilder().build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final DaprKeyValueAdapter daprKeyValueAdapter = new DaprKeyValueAdapter(daprClient, mapper, "kvstore");
    private final DaprKeyValueTemplate keyValueTemplate = new DaprKeyValueTemplate(daprKeyValueAdapter);

    @Test
    public void testInsertAndQueryDaprKeyValueTemplate() {
        var savedType = keyValueTemplate.insert(new TestType(3, "test"));
        assertThat(savedType).isNotNull();

        var findById = keyValueTemplate.findById(3, TestType.class).get();
        assertThat(findById).isNotNull();
        assertThat(findById).isEqualTo(savedType);

        KeyValueQuery<String> keyValueQuery = new KeyValueQuery<String>("'content' == 'test'");

        Iterable<TestType> myTypes = keyValueTemplate.find(keyValueQuery, TestType.class);
        assertThat(myTypes.iterator().hasNext()).isTrue();

        TestType item = myTypes.iterator().next();
        assertThat(item.getId()).isEqualTo(Integer.valueOf(3));
        assertThat(item.getContent()).isEqualTo("test");

        keyValueQuery = new KeyValueQuery<>("'content' == 'asd'");

        myTypes = keyValueTemplate.find(keyValueQuery, TestType.class);
        assertThat(!myTypes.iterator().hasNext()).isTrue();
    }

    @Test
    public void testInsertMoreThan10AndQueryDaprKeyValueTemplate() {

        List<TestType> types = new ArrayList<>();

        types.add(keyValueTemplate.insert(new TestType(0, "test")));
        types.add(keyValueTemplate.insert(new TestType(1, "test")));
        types.add(keyValueTemplate.insert(new TestType(2, "test")));
        types.add(keyValueTemplate.insert(new TestType(3, "test")));
        types.add(keyValueTemplate.insert(new TestType(4, "test")));
        types.add(keyValueTemplate.insert(new TestType(5, "test")));
        types.add(keyValueTemplate.insert(new TestType(6, "test")));
        types.add(keyValueTemplate.insert(new TestType(7, "test")));
        types.add(keyValueTemplate.insert(new TestType(8, "test")));
        types.add(keyValueTemplate.insert(new TestType(9, "test")));
        types.add(keyValueTemplate.insert(new TestType(10, "test")));
        types.add(keyValueTemplate.insert(new TestType(11, "test")));

        KeyValueQuery<String> keyValueQuery = new KeyValueQuery<>("'content' == 'test'");
        keyValueQuery.setRows(100);
        keyValueQuery.setOffset(0);
        Iterable<TestType> myTypes = keyValueTemplate.find(keyValueQuery, TestType.class);
        assertThat(myTypes.iterator().hasNext()).isTrue();

        var index = 0;
        for(TestType tt : myTypes){
            System.out.println("Index " + index);
            assertEquals(types.get(index).getId(), tt.getId());
            assertEquals(types.get(index).getContent(), tt.getContent());
            index++;
        }
        assertEquals(index, types.size());

    }

    @Test
    public void testUpdateDaprKeyValueTemplate() {
        var insertedType = keyValueTemplate.insert(new TestType(2, "test"));
        assertThat(insertedType).isNotNull();

        var updatedType = keyValueTemplate.update(new TestType(2, "test2"));
        assertThat(updatedType).isNotNull();
    }

}
