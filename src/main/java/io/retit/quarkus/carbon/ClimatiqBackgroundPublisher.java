package io.retit.quarkus.carbon;

import io.ApiClient;
import io.ApiException;
import io.climatiq.CloudComputingServiceApi;
import io.climatiq.model.*;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * This job publishes Climatiqe data continuously to Prometheus
 */
@Slf4j
@ApplicationScoped
public class ClimatiqBackgroundPublisher {

    @ConfigProperty(name = "io.retit.quarkus.carbon.cloud.provider")
    String cloudProvider;
    @ConfigProperty(name = "io.retit.quarkus.carbon.cloud.provider.region")
    String region;
    @ConfigProperty(name = "io.retit.quarkus.carbon.cloud.provider.instance.type")
    String instanceType;
    @ConfigProperty(name = "climatiq.api.key")
    String climatiqApiKey;

    @Inject
    private OtelService otelService;

    // @Scheduled(every = "60s", identity = "task-job")
    void schedule() throws ApiException {
        ApiClient climatiqApiClient = new ApiClient();
        climatiqApiClient.addDefaultHeader("Authorization", "Bearer " + climatiqApiKey);
        CloudComputingServiceApi cloudComputingServiceApi = new CloudComputingServiceApi(climatiqApiClient);

        CloudComputingInstanceRequest cloudComputingInstanceRequest = new CloudComputingInstanceRequest();

        cloudComputingInstanceRequest.region(region);
        cloudComputingInstanceRequest.instance(instanceType);
        //cloudComputingInstanceRequest.averageVcpuUtilization(ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage());
        cloudComputingInstanceRequest.duration(60.0);
        cloudComputingInstanceRequest.durationUnit("s");

        InstanceEstimationEstimateResponse estimationEstimateResponse = cloudComputingServiceApi.cloudComputingServiceInstance(cloudProvider, cloudComputingInstanceRequest);
        log.info("co2e: " + estimationEstimateResponse.getTotalCo2e() + " " + estimationEstimateResponse.getTotalCo2eUnit());

        Attributes attributes = Attributes.of(AttributeKey.stringKey("region"), region, AttributeKey.stringKey("instance-type"), instanceType, AttributeKey.stringKey("provider"), cloudProvider);
        otelService.publishInstanceCO2ProductionForRegionAndProvider((estimationEstimateResponse.getTotalCo2e() * 1000.0 * 1000.0), attributes);
    }

    @Scheduled(every = "60s", identity = "resource-publisher-job")
    void scheduleCO2OfResources() throws ApiException {

        ApiClient climatiqApiClient = new ApiClient();
        climatiqApiClient.addDefaultHeader("Authorization", "Bearer " + climatiqApiKey);
        CloudComputingServiceApi cloudComputingServiceApi = new CloudComputingServiceApi(climatiqApiClient);

        CloudComputingCpuRequest cloudComputingCpuRequest = new CloudComputingCpuRequest();

        cloudComputingCpuRequest.region(region);
        cloudComputingCpuRequest.cpuCount(1);
        cloudComputingCpuRequest.duration(1.0);
        cloudComputingCpuRequest.durationUnit("m");

        EstimationEstimateResponse cpuEstimateResponse = cloudComputingServiceApi.cloudComputingServiceCpu(cloudProvider, cloudComputingCpuRequest);
        double co2eInKg = cpuEstimateResponse.getCo2e();
        Attributes attributes = Attributes.of(AttributeKey.stringKey("region"), region, AttributeKey.stringKey("provider"), cloudProvider);

        otelService.publishCpuCO2ProductionForRegionAndProvider(co2eInKg * 1000 * 1000, attributes);

        CloudComputingMemoryRequest cloudComputingMemoryRequest = new CloudComputingMemoryRequest();

        cloudComputingMemoryRequest.region(region);
        cloudComputingMemoryRequest.data(1.0);
        cloudComputingMemoryRequest.dataUnit("GB");
        cloudComputingMemoryRequest.duration(1.0);
        cloudComputingMemoryRequest.durationUnit("m");

        EstimationEstimateResponse memoryEstimateResponse = cloudComputingServiceApi.cloudComputingServiceMemory(cloudProvider, cloudComputingMemoryRequest);
        double memCo2eInKg = memoryEstimateResponse.getCo2e();

        otelService.publishMemoryCO2ProductionForRegionAndProvider(memCo2eInKg * 1000 * 1000, attributes);
    }
}
