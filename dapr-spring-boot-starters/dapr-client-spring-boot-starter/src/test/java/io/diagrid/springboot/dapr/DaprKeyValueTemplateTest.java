package io.diagrid.springboot.dapr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import io.diagrid.springboot.dapr.core.DaprKeyValueTemplate;

@SpringBootTest(classes={DaprConfig.class})
@DependsOn("keyValueTemplate")
public class DaprKeyValueTemplateTest {

    private static final Logger LOG = LoggerFactory.getLogger(DaprKeyValueTemplateTest.class);

	@Autowired
	@Qualifier("keyValueTemplate")
	DaprKeyValueTemplate keyValueTemplate;


	@Test
	public void testDaprKeyValueTemplate() {


		MyType savedType = keyValueTemplate.insert(new MyType(3, "test"));
		assertNotNull(savedType);

	
		// MyType findById = keyValueTemplate.findById(3, MyType.class).get();
		// assertNotNull(findById);
		// assertEquals(findById, savedType);

		KeyValueQuery<String> keyValueQuery = new KeyValueQuery<String>("'content' == 'test'");
		
		Iterable<MyType> myTypes = keyValueTemplate.find(keyValueQuery, MyType.class);
		assertTrue(myTypes.iterator().hasNext());

		MyType item = myTypes.iterator().next();
		assertEquals(Integer.valueOf(3), item.getId());
		assertEquals(item.getContent(), "test");
		

		keyValueQuery = new KeyValueQuery<String>("'content' == 'asd'");
		
		myTypes = keyValueTemplate.find(keyValueQuery, MyType.class);
		assertTrue(!myTypes.iterator().hasNext());

	}
}
