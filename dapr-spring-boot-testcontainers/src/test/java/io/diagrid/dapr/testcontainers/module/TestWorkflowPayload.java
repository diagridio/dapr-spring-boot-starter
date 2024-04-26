package io.diagrid.dapr.testcontainers.module;

import java.util.List;

public class TestWorkflowPayload {
    private List<String> payloads;
    private String workflowId;

    
    public TestWorkflowPayload() {
    }

    
    public TestWorkflowPayload(List<String> payloads, String workflowId) {
        this.payloads = payloads;
        this.workflowId = workflowId;
    }


    public TestWorkflowPayload(List<String> payloads) {
        this.payloads = payloads;
    }

    public List<String> getPayloads() {
        return payloads;
    }

    public void setPayloads(List<String> payloads) {
        this.payloads = payloads;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    } 

    
    
}
