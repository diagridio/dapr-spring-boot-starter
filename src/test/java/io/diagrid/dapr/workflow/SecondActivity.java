package io.diagrid.dapr.workflow;

import io.dapr.workflows.runtime.WorkflowActivity;
import io.dapr.workflows.runtime.WorkflowActivityContext;

public class SecondActivity implements WorkflowActivity{
    @Override
    public Object run(WorkflowActivityContext ctx) {
        TestWorkflowPayload workflowPayload = ctx.getInput(TestWorkflowPayload.class);
        workflowPayload.getPayloads().add("Second Activity");
        return workflowPayload;
    }
}
