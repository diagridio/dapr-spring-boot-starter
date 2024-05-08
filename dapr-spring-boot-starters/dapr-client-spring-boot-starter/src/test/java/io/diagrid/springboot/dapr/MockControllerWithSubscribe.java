package io.diagrid.springboot.dapr;

import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockControllerWithSubscribe {
    public static final String pubSubName = "pubsub";
    public static final String topicName = "mockTopic";

    private static final Logger LOG = LoggerFactory.getLogger(MockControllerWithSubscribe.class);
  
    private List<CloudEvent> events = new ArrayList<>();

    public MockControllerWithSubscribe() {
        LOG.info("Subscriber started");
    }

    @Topic(name = topicName, pubsubName = pubSubName)
    @PostMapping("subscribe")
    public void handleMessages(@RequestBody CloudEvent<String> event) {
        LOG.info("++++++CONSUME {}------", event);
        events.add(event);
    }

    public List<CloudEvent> getEvents() {
        return events;
    }


  }
