package io.diagrid.spring.core.kvstore;

import io.dapr.client.DaprClientBuilder;
import io.diagrid.BaseIntegrationTest;
import io.diagrid.spring.core.keyvalue.DaprKeyValueAdapter;
import io.diagrid.spring.core.keyvalue.DaprKeyValueTemplate;
import io.diagrid.spring.core.keyvalue.repository.EnableDaprRepositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for {@link DaprKeyValueRepositoryIT}.
 */

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class DaprKeyValueRepositoryIT extends BaseIntegrationTest {
    
    @Configuration
    @EnableDaprRepositories
    static class Config{

        @Bean
        public ObjectMapper mapper() {
            return new ObjectMapper();
        }

        @Bean
        public KeyValueAdapter daprKeyValueAdapter(DaprClientBuilder daprClientBuilder, ObjectMapper mapper) {
            return new DaprKeyValueAdapter(daprClientBuilder.build(), mapper, "kvstore", "kvbinding");
        }

        @Bean
        public DaprKeyValueTemplate daprKeyValueTemplate(DaprKeyValueAdapter daprKeyValueAdapter) {
            return new DaprKeyValueTemplate(daprKeyValueAdapter);
        }

        @Bean
        public DaprClientBuilder daprClientBuilder() {
            return new DaprClientBuilder();
        }

    
    }

    

    @Autowired
    private TestTypeRepository repo;

    @Test
    public void testInsertAndQueryDaprKeyValueTemplate() {

        TestType saved = repo.save(new TestType(3, "test"));

        TestType byId = repo.findById(3).get();

        assertEquals(saved, byId);

        boolean existsById = repo.existsById(3);
        assertTrue(existsById);

        boolean existsById2 = repo.existsById(4);
        assertTrue(!existsById2);



        //Iterable<TestType> all = repo.findAll();
        
    }

    

}
