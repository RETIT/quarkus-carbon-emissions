package io.retit.quarkus.carbon;

import io.opentelemetry.api.common.Attributes;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/test-rest-endpoint")
public class TestRESTEndpoint {

    @Inject
    private ResourceDemandMeasurementService resourceDemandMeasurementService;

    @Inject
    private TestService testService;

    @GET
    public String getData() throws InterruptedException {
        Attributes attributes = callBusinessFunctionAndPublishMetric("GET", "getData");
        return "Published Metric with Attributes: " + attributes;
    }

    @POST
    public String postData() throws InterruptedException {
        Attributes attributes = callBusinessFunctionAndPublishMetric("POST", "postData");
        return "Published Metric with Attributes: " + attributes;
    }

    @DELETE
    public String deleteData() throws InterruptedException {
        Attributes attributes = callBusinessFunctionAndPublishMetric("DELETE", "deleteData");
        return "Published Metric with Attributes: " + attributes;
    }

    private Attributes callBusinessFunctionAndPublishMetric(String httpMethod, String apiCall) throws InterruptedException {

        ResourceDemandMeasurementService.Measurement startMeasurements = resourceDemandMeasurementService.measure();

        testService.veryComplexBusinessFunction();

        return resourceDemandMeasurementService.measureAndPublishMetrics(startMeasurements, httpMethod, apiCall);
    }

}
