package com.tajjm.demo.lights.server.b;

import com.google.protobuf.Empty;
import com.tajjm.demo.lights.proto.LightServiceGrpc;
import com.tajjm.demo.lights.proto.PongResponse;
import io.grpc.ManagedChannelBuilder;
import io.opencensus.common.Scope;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class LightServiceClient {
    private static final Tracer tracer = Tracing.getTracer();
    private LightServiceGrpc.LightServiceBlockingStub stub;
    private static final Logger logger = LoggerFactory.getLogger(LightServiceClient.class);

    public LightServiceClient(String host, int port) {
        var channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        stub = LightServiceGrpc.newBlockingStub(channel);
    }

    public CompletionStage<PongResponse> ping() {
        final Span parentSpan = tracer.getCurrentSpan();
        return CompletableFuture.supplyAsync(() -> {
            try (Scope ignore = tracer.spanBuilderWithExplicitParent("LightServiceClient", parentSpan).startScopedSpan();) {
                Span childSpan = tracer.getCurrentSpan();
                MDC.put("trace", childSpan.getContext().getTraceId().toLowerBase16());
                MDC.put("spanId", childSpan.getContext().getSpanId().toLowerBase16());
                logger.info("Calling service-a");
                return stub.ping(Empty.newBuilder().build());
            } finally {
                MDC.clear();
            }
        });
    }
}
