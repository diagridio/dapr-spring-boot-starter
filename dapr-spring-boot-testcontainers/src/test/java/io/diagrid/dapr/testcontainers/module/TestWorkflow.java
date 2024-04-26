package io.diagrid.dapr.testcontainers.module;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;

import java.time.Duration;

import org.slf4j.Logger;

public class TestWorkflow extends Workflow {
  
  @Override
  public WorkflowStub create() {
    return ctx -> {
      Logger logger = ctx.getLogger();
      String instanceId = ctx.getInstanceId();
      logger.info("Starting Workflow: " + ctx.getName());
      logger.info("Instance ID: " + instanceId);
      logger.info("Current Orchestration Time: " + ctx.getCurrentInstant());

      TestWorkflowPayload workflowPayload = ctx.getInput(TestWorkflowPayload.class);
      workflowPayload.setWorkflowId(instanceId);

      TestWorkflowPayload payloadAfterFirst = ctx.callActivity(FirstActivity.class.getName(), workflowPayload, TestWorkflowPayload.class).await();


      ctx.waitForExternalEvent("MoveForward", Duration.ofSeconds(3), TestWorkflowPayload.class).await();
    
      TestWorkflowPayload payloadAfterSecond = ctx.callActivity(SecondActivity.class.getName(), payloadAfterFirst, TestWorkflowPayload.class).await();  
    
      ctx.complete(payloadAfterSecond);

    };
  }
}
