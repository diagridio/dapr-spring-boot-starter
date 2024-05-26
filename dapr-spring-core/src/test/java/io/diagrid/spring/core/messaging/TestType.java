package io.diagrid.spring.core.messaging;

import org.springframework.data.annotation.Id;

public record TestType(@Id Integer id, String content) {
}
