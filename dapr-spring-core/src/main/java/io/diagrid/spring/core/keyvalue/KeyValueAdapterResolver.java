package io.diagrid.spring.core.keyvalue;

import org.springframework.data.keyvalue.core.KeyValueAdapter;

public interface KeyValueAdapterResolver {
    KeyValueAdapter resolve();
}
