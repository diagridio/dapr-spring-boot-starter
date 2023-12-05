package io.diagrid.dapr.profiles.full;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import io.dapr.workflows.client.DaprWorkflowClient;
import io.dapr.workflows.client.WorkflowInstanceStatus;
import io.dapr.workflows.runtime.WorkflowRuntime;
import io.dapr.workflows.runtime.WorkflowRuntimeBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Duration;

import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = MyTestWithWorkflowsApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
public class DaprFullProfileTests {

    @Autowired()
    private SubscriptionsRestController subscriptionsRestController;

    private DaprWorkflowClient workflowClient;

    @BeforeEach
    public void init() {
        WorkflowRuntimeBuilder builder = new WorkflowRuntimeBuilder().registerWorkflow(TestWorkflow.class);
        builder.registerActivity(FirstActivity.class);
        builder.registerActivity(SecondActivity.class);

        try (WorkflowRuntime runtime = builder.build()) {
            System.out.println("Start workflow runtime");
            runtime.start(false);
        }
    }

    @Test
    public void myWorkflowTest() throws Exception {
        workflowClient = new DaprWorkflowClient();

        TestWorkflowPayload payload = new TestWorkflowPayload(new ArrayList());
        String instanceId = workflowClient.scheduleNewWorkflow(TestWorkflow.class, payload);
        
        workflowClient.waitForInstanceStart(instanceId, Duration.ofSeconds(10), false);
    
        workflowClient.raiseEvent(instanceId, "MoveForward", payload);
        
        WorkflowInstanceStatus workflowStatus = workflowClient.waitForInstanceCompletion(instanceId,
                    Duration.ofSeconds(10),
                    true);

        // The workflow completed before 10 seconds
        assertNotNull(workflowStatus);

        String workflowPlayloadJson = workflowStatus.getSerializedOutput();
        
        ObjectMapper mapper = new ObjectMapper();
        TestWorkflowPayload workflowOutput = mapper.readValue(workflowPlayloadJson, TestWorkflowPayload.class);

        assertEquals(2, workflowOutput.getPayloads().size());
        assertEquals("First Activity", workflowOutput.getPayloads().get(0));
        assertEquals("Second Activity", workflowOutput.getPayloads().get(1));
        assertEquals(instanceId, workflowOutput.getWorkflowId());
        
        
       

    }

}
