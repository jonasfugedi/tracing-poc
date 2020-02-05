package com.tajjm.demo.lights;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import com.tajjm.demo.lights.proto.LightProxyServiceGrpc;
import com.tajjm.demo.lights.proto.PongResponse;
import io.grpc.ManagedChannelBuilder;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        Utils.initializeTracing("client");

        String host = "127.0.0.1";
        int port = 6791 + 1;
        var channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        var blockingStub = LightProxyServiceGrpc.newBlockingStub(channel);

        // Create a scoped span, a scoped span will automatically end when closed.
        // It implements AutoClosable, so it'll be closed when the try block ends.
        Span span = Tracing.getTracer().spanBuilder("main").startSpan();
        try {
            logger.info("Calling service");
            for (int i = 0; i < 10; i++) {
                Span childSpan = Tracing.getTracer().spanBuilderWithExplicitParent("call", span).startSpan();
                try {
                    MDC.put("trace", childSpan.getContext().getTraceId().toLowerBase16());
                    MDC.put("spanId", childSpan.getContext().getSpanId().toLowerBase16());
                    PongResponse response = blockingStub.ping(Empty.newBuilder().build());
                    Timestamp ts = response.getTimestamp();
                    Instant instant = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
                    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
                    logger.info("Response message={} datetime={}", response.getMessage(), dateTime);
                    MDC.clear();
                } finally {
                    childSpan.end();
                }
            }
        } finally {
            span.end();
        }

        logger.info("Shutting down tracing");
        Tracing.getExportComponent().shutdown();
        logger.info("All done");
        System.exit(0);
    }
}
