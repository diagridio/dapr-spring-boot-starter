package io.diagrid.dapr.profiles.basic;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.dapr.client.domain.CloudEvent;

@RestController
public class SubscriptionsRestController {
    private List<CloudEvent> events = new ArrayList<>();

    @PostMapping(path= "/events", consumes = "application/cloudevents+json")
    public void receiveEvents(@RequestBody CloudEvent event){
        events.add(event);
    }

    @GetMapping(path= "/events",produces = "application/cloudevents+json")
    public List<CloudEvent> getAllEvents(){
        return events;
    }
}
