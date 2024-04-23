package io.retit.quarkus.carbon;

import io.ApiClient;
import io.ApiException;
import io.climatiq.CloudComputingServiceApi;
import io.climatiq.model.CloudComputingCpuRequest;
import io.climatiq.model.CloudComputingMemoryRequest;
import io.climatiq.model.EstimationEstimateResponse;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * This job publishes Climatiq emission data continuously as OpenTelemetry metrics.
 */
@Slf4j
@ApplicationScoped
public class ClimatiqBackgroundPublisher {

    @ConfigProperty(name = "io.retit.quarkus.carbon.cloud.provider")
    String cloudProvider;
    @ConfigProperty(name = "io.retit.quarkus.carbon.cloud.provider.region")
    String region;
    @ConfigProperty(name = "climatiq.api.key")
    String climatiqApiKey;

    @Inject
    private OpenTelemetryService otelService;

    /**
     * This method schedules the Climatiq background publisher.
     *
     * @throws ApiException - in case the call to Climatiq fails.
     */
    @Scheduled(every = "60s", identity = "resource-publisher-job")
    void scheduleCO2OfResources() throws ApiException {

        ApiClient climatiqApiClient = new ApiClient();
        climatiqApiClient.addDefaultHeader("Authorization", "Bearer " + climatiqApiKey);
        CloudComputingServiceApi cloudComputingServiceApi = new CloudComputingServiceApi(climatiqApiClient);

        Attributes attributes = Attributes.of(AttributeKey.stringKey("region"), region, AttributeKey.stringKey("provider"), cloudProvider);

        publishCpuCO2EmissionsForRegionAndProvider(cloudComputingServiceApi, attributes);

        publishMemoryCO2EmissionsForRegionAndProvider(cloudComputingServiceApi, attributes);
    }

    /**
     * This method publishes the CO2 emissions of one GB memory consumption for one minute in the configured
     * region and cloud provider. This data is later used for calculating the emissions of a single API call.
     *
     * @param cloudComputingServiceApi - an API instance to interact with the Climatiq API.
     * @param attributes               - the attributes published along with the emission data.
     * @throws ApiException - in case the call to Climatiq fails.
     */
    private void publishMemoryCO2EmissionsForRegionAndProvider(CloudComputingServiceApi cloudComputingServiceApi, Attributes attributes) throws ApiException {
        CloudComputingMemoryRequest cloudComputingMemoryRequest = new CloudComputingMemoryRequest();

        cloudComputingMemoryRequest.region(region);
        cloudComputingMemoryRequest.data(1.0);
        cloudComputingMemoryRequest.dataUnit("GB");
        cloudComputingMemoryRequest.duration(1.0);
        cloudComputingMemoryRequest.durationUnit("m");

        EstimationEstimateResponse memoryEstimateResponse = cloudComputingServiceApi.cloudComputingServiceMemory(cloudProvider, cloudComputingMemoryRequest);
        double memCo2eInKg = memoryEstimateResponse.getCo2e();

        otelService.publishMemoryCO2EmissionsForRegionAndProvider((long) (memCo2eInKg * 1000 * 1000), attributes);
    }

    /**
     * This method publishes the CO2 emissions of one CPU core for one minute in the configured
     * region and cloud provider. This data is later used for calculating the emissions of a single API call.
     *
     * @param cloudComputingServiceApi - an API instance to interact with the Climatiq API.
     * @param attributes               - the attributes published along with the emission data.
     * @throws ApiException - in case the call to Climatiq fails.
     */
    private void publishCpuCO2EmissionsForRegionAndProvider(final CloudComputingServiceApi cloudComputingServiceApi, final Attributes attributes) throws ApiException {
        CloudComputingCpuRequest cloudComputingCpuRequest = new CloudComputingCpuRequest();

        cloudComputingCpuRequest.region(region);
        cloudComputingCpuRequest.cpuCount(1);
        cloudComputingCpuRequest.duration(1.0);
        cloudComputingCpuRequest.durationUnit("m");

        EstimationEstimateResponse cpuEstimateResponse = cloudComputingServiceApi.cloudComputingServiceCpu(cloudProvider, cloudComputingCpuRequest);
        double co2eInKg = cpuEstimateResponse.getCo2e();

        otelService.publishCpuCO2EmissionsForRegionAndProvider((long) (co2eInKg * 1000 * 1000), attributes);
    }
}
