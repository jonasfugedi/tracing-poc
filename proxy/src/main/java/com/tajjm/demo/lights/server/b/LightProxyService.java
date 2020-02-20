package com.tajjm.demo.lights.server.b;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import com.tajjm.demo.lights.proto.*;
import io.grpc.stub.StreamObserver;
import io.opencensus.common.Scope;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.CompletableFuture;


public class LightProxyService extends LightProxyServiceGrpc.LightProxyServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(LightProxyService.class);

    private static final Tracer tracer = Tracing.getTracer();

    private LightServiceClient client;

    public LightProxyService() {
        this.client = new LightServiceClient("127.0.0.1", 6791);
    }

    @Override
    public void ping(Empty request, StreamObserver<PongResponse> responseObserver) {
        Span span = tracer.getCurrentSpan();
        MDC.put("trace", span.getContext().getTraceId().toLowerBase16());
        MDC.put("spanId", span.getContext().getSpanId().toLowerBase16());

        logger.info("Ping proxy call");
        try {
            client.ping().thenAccept(pongResponse -> {
                responseObserver.onNext(pongResponse);
                responseObserver.onCompleted();
            });
        } catch (Throwable t) {
            responseObserver.onError(t);
        } finally {
            MDC.clear();
        }
    }
}
