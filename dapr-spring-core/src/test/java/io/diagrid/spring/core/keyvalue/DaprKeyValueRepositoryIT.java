package io.diagrid.spring.core.keyvalue;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.diagrid.AbstractPostgreSQLBaseIT;
import io.diagrid.spring.core.keyvalue.repository.EnableDaprRepositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for {@link DaprKeyValueRepositoryIT}.
 */

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class DaprKeyValueRepositoryIT extends AbstractPostgreSQLBaseIT {
    
    @Configuration
    @EnableDaprRepositories
    static class Config{

        @Bean
        public ObjectMapper mapper() {
            return new ObjectMapper();
        }

        @Bean
        public KeyValueAdapterResolver keyValueAdapterResolver(DaprClientBuilder daprClientBuilder, ObjectMapper mapper) {
            DaprClient daprClient = daprClientBuilder.build();

            return new DaprKeyValueAdapterResolver(daprClient, mapper, STATE_STORE_NAME, BINDING_NAME);
        }

        @Bean
        public DaprKeyValueTemplate daprKeyValueTemplate(KeyValueAdapterResolver keyValueAdapterResolver) {
            return new DaprKeyValueTemplate(keyValueAdapterResolver);
        }

        @Bean
        public DaprClientBuilder daprClientBuilder() {
            return new DaprClientBuilder();
        }
    
    }

    @Autowired
    private TestTypeRepository repo;

    /*
     * we should test that: 
     * - We can store multiple entities
     * - Count them and retrieve them using pagination from the CrudRepository
     * - Filter entities by properties values (we should be able to extend the Repository definition with custom queries)
     * - Delete entities by ID
     */
    @Test
    public void testInsertAndQueryDaprKeyValueTemplate() {
        TestType saved = repo.save(new TestType(3, "test"));
        TestType byId = repo.findById(3).get();

        assertEquals(saved, byId);

        boolean existsById = repo.existsById(3);
        assertTrue(existsById);

        boolean existsById2 = repo.existsById(4);
        assertTrue(!existsById2);

        TestType saved2 = repo.save(new TestType(4, "test2"));
        existsById2 = repo.existsById(4);
        assertTrue(existsById2);

        Iterable<TestType> all = repo.findAll();

        assertEquals(2, all.spliterator().getExactSizeIfKnown());

        // Filter using Repository extensions specific to this TestType object
        List<TestType> byContent = repo.findByContent("test2");

        assertEquals(1, byContent.size());

        repo.deleteById(byContent.get(0).getId());

        all = repo.findAll();

        assertEquals(1, all.spliterator().getExactSizeIfKnown());

    }

}
