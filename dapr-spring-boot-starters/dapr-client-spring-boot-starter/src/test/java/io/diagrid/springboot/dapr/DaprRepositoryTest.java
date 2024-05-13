package io.diagrid.springboot.dapr;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest()
public class DaprRepositoryTest {

    private static final Logger LOG = LoggerFactory.getLogger(DaprRepositoryTest.class);

	

	@Autowired
	private MyTypeRepository repository;

	
	@Test
	public void testDaprRepositoryTest() {

		MyType myType = new MyType(1, "test");
		MyType savedMyType = repository.save(myType);

		assertNotNull(savedMyType);

		Optional<MyType> byId = repository.findById(1);

		assertTrue(byId.isPresent());

		
	}
}
