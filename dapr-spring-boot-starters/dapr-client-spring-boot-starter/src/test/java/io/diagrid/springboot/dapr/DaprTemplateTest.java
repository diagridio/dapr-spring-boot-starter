package io.diagrid.springboot.dapr;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.diagrid.dapr.DaprContainer;
import io.diagrid.springboot.dapr.core.DaprTemplate;

@SpringBootTest(classes=DaprConfig.class, webEnvironment = WebEnvironment.NONE)
public class DaprTemplateTest {

    private static final Logger LOG = LoggerFactory.getLogger(DaprTemplateTest.class);

	private static final String TOPIC = "produce-read-primitive";

	

	@Autowired
	private DaprContainer dapr;

	
	@Test
	public void testDaprTemplate() {
		System.setProperty("dapr.grpc.port", Integer.toString(dapr.getGRPCPort()));
        System.setProperty("dapr.http.port", Integer.toString(dapr.getHTTPPort()));
		
		DaprTemplate<String> template = new DaprTemplate<String>();

		for (int i = 0; i < 10; i++) {
			var msg = "ProduceAndReadWithPrimitiveMessageType:" + i;
			template.send(TOPIC, msg);
			LOG.info("++++++PRODUCE {}------", msg);
		}

		// @PulsarReader(topics = TOPIC, startMessageId = "earliest")
		// void readPrimitiveMessagesFromPulsarTopic(String msg) {
		// 	LOG.info("++++++READ {}------", msg);
		// }

	}
}
