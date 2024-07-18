package io.diagrid.spring.core.keyvalue;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestTypeRepository extends CrudRepository<TestType, Integer>{
    public List<TestType> findByContent(String content);
}
