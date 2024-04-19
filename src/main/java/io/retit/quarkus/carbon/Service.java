package io.retit.quarkus.carbon;

import com.sun.management.ThreadMXBean;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import java.lang.management.ManagementFactory;
import java.util.Random;

@Path("/testservice")
public class Service {

    @Inject
    private OtelService otelService;

    @GET
    public String getData(@HeaderParam("customer_id") String customer_id, @HeaderParam("tenant_id") String tenant_id) throws InterruptedException {
        Attributes attributes = callBusinessFunctionAndPublishMetric("GET", customer_id, tenant_id);
        return "Published Metric with Attributes: " + attributes;
    }

    @POST
    public String postData(@HeaderParam("customer_id") String customer_id, @HeaderParam("tenant_id") String tenant_id) throws InterruptedException {
        Attributes attributes = callBusinessFunctionAndPublishMetric("POST", customer_id, tenant_id);
        return "Published Metric with Attributes: " + attributes;
    }

    @DELETE
    public String deleteData(@HeaderParam("customer_id") String customer_id, @HeaderParam("tenant_id") String tenant_id) throws InterruptedException {
        Attributes attributes = callBusinessFunctionAndPublishMetric("DELETE", customer_id, tenant_id);
        return "Published Metric with Attributes: " + attributes;
    }

    private Attributes callBusinessFunctionAndPublishMetric(String httpMethod, String customer_id, String tenant_id) throws InterruptedException {

        Attributes attributes = Attributes.of(AttributeKey.stringKey("httpmethod"), httpMethod, AttributeKey.stringKey("customer"), customer_id, AttributeKey.stringKey("tenant"), tenant_id);

        long startTimeBefore = System.currentTimeMillis();
        long cpuTimeBefore = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        long memoryBefore = ((ThreadMXBean) ManagementFactory.getThreadMXBean()).getCurrentThreadAllocatedBytes();
        veryComplexBusinessFunction();
        long cpuTimeAfter = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        long memoryAfter = ((ThreadMXBean) ManagementFactory.getThreadMXBean()).getCurrentThreadAllocatedBytes();
        long endTimeAfter = System.currentTimeMillis();

        otelService.publishCpuTimeMetric((cpuTimeAfter - cpuTimeBefore) / 1_000_000, attributes);
        otelService.publishMemoryDemandMetric((memoryAfter - memoryBefore) / 1_000, attributes);
        otelService.publishResponseTimeMetric((endTimeAfter - startTimeBefore), attributes);
        return attributes;
    }

    private void veryComplexBusinessFunction() throws InterruptedException {
        naiveSortingWithONSquareComplexity(generateRandomInputArray(30000));
    }

    private static int[] generateRandomInputArray(int size) {
        int array[] = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = new Random().nextInt(1000000);
        }

        return array;
    }

    // O(n²)
    private static int[] naiveSortingWithONSquareComplexity(int[] inputArray) {
        // Outer loop
        for (int i = 0; i < inputArray.length; i++) {

            // Inner nested loop pointing 1 index ahead
            for (int j = i + 1; j < inputArray.length; j++) {

                // Checking elements
                int temp = 0;
                if (inputArray[j] < inputArray[i]) {

                    // Swapping
                    temp = inputArray[i];
                    inputArray[i] = inputArray[j];
                    inputArray[j] = temp;
                }
            }

        }
        return inputArray;
    }
}
