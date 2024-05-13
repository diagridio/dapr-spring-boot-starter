package io.diagrid.springboot.dapr;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyTypeRepository extends CrudRepository<MyType, Integer> {
    
}
