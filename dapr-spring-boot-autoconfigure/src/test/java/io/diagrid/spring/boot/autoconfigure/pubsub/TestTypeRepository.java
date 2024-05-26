package io.diagrid.spring.boot.autoconfigure.pubsub;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestTypeRepository extends CrudRepository<TestType, Integer> {
}
