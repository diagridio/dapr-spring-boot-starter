package io.diagrid.springboot.dapr;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.dapr.client.domain.CloudEvent;
import io.diagrid.springboot.dapr.core.DaprMessagingTemplate;

@SpringBootTest(classes={DaprTestConfig.class}, webEnvironment = WebEnvironment.DEFINED_PORT)
public class DaprMessagingTemplateTest {

    private static final Logger LOG = LoggerFactory.getLogger(DaprMessagingTemplateTest.class);

	private static final String TOPIC = "mockTopic";

	@Autowired
	private AppRestController appRestController;

	@Autowired
	@Qualifier("messagingTemplate")
	private DaprMessagingTemplate<String> messagingTemplate;
	
	@Test
	public void testDaprMessagingTemplate() throws InterruptedException {


		for (int i = 0; i < 10; i++) {
			var msg = "ProduceAndReadWithPrimitiveMessageType:" + i;
			messagingTemplate.send(TOPIC, msg);
			LOG.info("++++++PRODUCE {}------", msg);
		}

		// Wait for the messages to arrive
		Thread.sleep(1000);
		
		@SuppressWarnings("rawtypes")
		List<CloudEvent> events = appRestController.getEvents();
		assertEquals(10, events.size());
	

	
	}
}
