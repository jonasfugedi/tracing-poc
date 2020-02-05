package com.tajjm.demo.lights;

import io.opencensus.exporter.trace.jaeger.JaegerExporterConfiguration;
import io.opencensus.exporter.trace.jaeger.JaegerTraceExporter;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.samplers.Samplers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.jaegertracing.Configuration.*;
import static io.jaegertracing.Configuration.JAEGER_ENDPOINT;

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static void initializeTracing(String serviceName) {
        logger.info("Initializing tracing for service name {}", serviceName);
        System.setProperty(JAEGER_SERVICE_NAME, serviceName);
        System.setProperty(JAEGER_SAMPLER_TYPE, "const");
        System.setProperty(JAEGER_SAMPLER_PARAM, "1");
        System.setProperty(JAEGER_ENDPOINT, "http://127.0.0.1:14268/api/traces");

        // TracerResolver.resolveTracer();
        // Configuration configuration = Configuration.fromEnv();

        JaegerExporterConfiguration config = JaegerExporterConfiguration.builder()
                .setServiceName(serviceName)
                .setThriftEndpoint("http://127.0.0.1:14268/api/traces")
                .build();
        JaegerTraceExporter.createAndRegister(config);

        TraceConfig traceConfig = Tracing.getTraceConfig();
        traceConfig.updateActiveTraceParams(
                traceConfig.getActiveTraceParams()
                        .toBuilder()
                        .setSampler(Samplers.alwaysSample())
                        .build());
    }
}
