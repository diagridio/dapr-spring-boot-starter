package io.diagrid.spring.core.kvstore;

import org.springframework.data.annotation.Id;


public class TestType {
    
    @Id
    private Integer id;
    private String content;

    public TestType() {
    }
    public TestType(Integer id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getContent() {
        return content;
    }
    public Integer getId() {
        return id;
    }
    
}
