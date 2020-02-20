package com.tajjm.demo.lights.server.a;

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

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LightService extends LightServiceGrpc.LightServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(LightService.class);

    private static final Tracer tracer = Tracing.getTracer();

    @Override
    public void ping(Empty request, StreamObserver<PongResponse> responseObserver) {
        Span span = tracer.getCurrentSpan();

        MDC.put("trace", span.getContext().getTraceId().toLowerBase16());
        MDC.put("spanId", span.getContext().getSpanId().toLowerBase16());
        try (Scope ignore = tracer.withSpan(span)) {
            logger.info("Ping response");
            long millis = System.currentTimeMillis();
            Timestamp timestamp = Timestamp.newBuilder().setSeconds(millis / 1000)
                    .setNanos((int) ((millis % 1000) * 1000000)).build();
            responseObserver.onNext(
                    PongResponse.newBuilder()
                            .setMessage("Pong")
                            .setTimestamp(timestamp)
                            .build());
            responseObserver.onCompleted();
        } finally {
            span.end();
            MDC.clear();
        }
    }
}
