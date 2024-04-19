package io.retit.quarkus.carbon;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class OtelService {


    @Startup
    public void init() {
        GlobalOpenTelemetry.resetForTest();
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "testservice")));

        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build())
                .setResource(resource)
                .build();

        OpenTelemetrySdk.builder()
                // .setTracerProvider(sdkTracerProvider)
                .setMeterProvider(sdkMeterProvider)
                // .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();

    }

    public void publishInstanceCO2ProductionForRegionAndProvider(double co2inmgramm, Attributes attributes) {

        Meter meter = GlobalOpenTelemetry.get().meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();

        meter
                .gaugeBuilder("instance_co2_production")
                .setDescription("CO2 Production of Instance")
                .setUnit("mg").buildWithCallback(measurement -> {
                    measurement.record(co2inmgramm, attributes);
                });
    }

    public void publishCpuCO2ProductionForRegionAndProvider(double co2inmgramm, Attributes attributes) {

        Meter meter = GlobalOpenTelemetry.get().meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();


        LongCounter counter = meter
                .counterBuilder("cpu_co2_production")
                .setDescription("CO2 Production of CPU")
                .setUnit("mg")
                .build();

        long mgValue = (long) co2inmgramm;

        log.info("Publish cpu CO2e mg value " + mgValue);
        // Record data
        counter.add(mgValue, attributes);
    }

    public void publishMemoryCO2ProductionForRegionAndProvider(double co2inmgramm, Attributes attributes) {

        Meter meter = GlobalOpenTelemetry.get().meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();


        LongCounter counter = meter
                .counterBuilder("memory_co2_production")
                .setDescription("CO2 Production of Memory")
                .setUnit("mg")
                .build();

        long mgValue = (long) co2inmgramm;

        log.info("Publish memory CO2e mg value " + mgValue);
        // Record data
        counter.add(mgValue, attributes);
    }

    public void publishCpuTimeMetric(long cpuTimeInMS, Attributes attributes) {

        // Gets or creates a named meter instance
        Meter meter = GlobalOpenTelemetry.get().meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();

        // Build counter e.g. LongCounter
        LongCounter counter = meter
                .counterBuilder("testservice_cpu_time")
                .setDescription("CPU Time Metric")
                .setUnit("ms")
                .build();


        log.info("Publish cpu demand for service invocation " + cpuTimeInMS + " ms");
        // Record data
        counter.add(cpuTimeInMS, attributes);
    }

    public void publishMemoryDemandMetric(long memoryDemandInKByte, Attributes attributes) {

        // Gets or creates a named meter instance
        Meter meter = GlobalOpenTelemetry.get().meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();

        // Build counter e.g. LongCounter
        LongCounter counter = meter
                .counterBuilder("testservice_memory_demand")
                .setDescription("Memory Demand Metric")
                .setUnit("kByte")
                .build();

        log.info("Publish memory demand for service invocation " + memoryDemandInKByte + " kByte");
        // Record data
        counter.add(memoryDemandInKByte, attributes);
    }

    public void publishResponseTimeMetric(long responseTime, Attributes attributes) {

        // Gets or creates a named meter instance
        Meter meter = GlobalOpenTelemetry.get().meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();

        LongCounter counter = meter
                .counterBuilder("testservice_call_count")
                .setDescription("Tracks the number of calls to the test service")
                .setUnit("1")
                .build();

        // Record data
        counter.add(1, attributes);

        /*meter
                .gaugeBuilder("testservice_response_time")
                .setDescription("Response Time Metric")
                .setUnit("ms").buildWithCallback(measurement -> {
                    measurement.record(responseTime, attributes);
                });*/
    }

}
