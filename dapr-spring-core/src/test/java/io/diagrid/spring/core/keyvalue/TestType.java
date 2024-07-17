package io.diagrid.spring.core.keyvalue;

import org.springframework.data.annotation.Id;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestType testType = (TestType) o;
        return Objects.equals(id, testType.id) && Objects.equals(content, testType.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content);
    }
}
