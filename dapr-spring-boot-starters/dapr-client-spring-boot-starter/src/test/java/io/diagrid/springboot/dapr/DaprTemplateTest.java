package io.diagrid.springboot.dapr;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import io.dapr.client.domain.CloudEvent;
import io.diagrid.springboot.dapr.core.DaprTemplate;

@SpringBootTest(classes={DaprConfig.class})
public class DaprTemplateTest {

    private static final Logger LOG = LoggerFactory.getLogger(DaprTemplateTest.class);

	private static final String TOPIC = "mockTopic";

	/* Check the subcriberController for subscription annotations */
	@Autowired
	private MockControllerWithSubscribe subscribeController;

	@Autowired
	private DaprTemplate<String> template;
	
	@Test
	public void testDaprTemplate() {


		for (int i = 0; i < 10; i++) {
			var msg = "ProduceAndReadWithPrimitiveMessageType:" + i;
			template.send(TOPIC, msg);
			LOG.info("++++++PRODUCE {}------", msg);
		}

		List<CloudEvent> events = subscribeController.getEvents();
		assertEquals(10, events.size());
	

	
	}
}
