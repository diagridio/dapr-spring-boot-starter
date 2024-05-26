package io.diagrid.dapr.testcontainers.module;

import io.dapr.workflows.runtime.WorkflowActivity;
import io.dapr.workflows.runtime.WorkflowActivityContext;

public class FirstActivity implements WorkflowActivity {

    @Override
    public Object run(WorkflowActivityContext ctx) {
        TestWorkflowPayload workflowPayload = ctx.getInput(TestWorkflowPayload.class);
        workflowPayload.getPayloads().add("First Activity");
        return workflowPayload;
    }

}
