package io.retit.quarkus.carbon;

import io.opentelemetry.api.common.Attributes;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * This is an example REST service that provides three endpoints for HTTP GET / POST and DELETE.
 */
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

    /**
     * This is an example of a business functionality that is being used by the REST service endpoints.
     * <p>
     * Before and after the service operation the resource demands of the business function are being measured.
     *
     * @param httpMethod - the HTTP method (GET/POST/DELETE) of the endpoint calling the business function
     * @param apiCall    - the name of the API call (getData, postData, deleteData) calling the business function
     * @return - the OpenTelemetry Attributes published along with the resource demand metrics
     */
    private Attributes callBusinessFunctionAndPublishMetric(String httpMethod, String apiCall) {

        ResourceDemandMeasurementService.Measurement startMeasurements = resourceDemandMeasurementService.measure();

        testService.veryComplexBusinessFunction();

        return resourceDemandMeasurementService.measureAndPublishMetrics(startMeasurements, httpMethod, apiCall);
    }

}
