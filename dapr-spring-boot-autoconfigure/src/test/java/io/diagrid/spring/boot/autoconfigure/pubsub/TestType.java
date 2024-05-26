package io.diagrid.spring.boot.autoconfigure.pubsub;

import org.springframework.data.annotation.Id;

public record TestType(@Id Integer id, String content) {
}
