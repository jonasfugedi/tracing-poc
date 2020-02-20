package com.tajjm.demo.lights.server.b;

import com.tajjm.demo.lights.Utils;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Proxy {
    private static final Logger logger = LoggerFactory.getLogger(Proxy.class);

    private Server server;

    private void start() throws IOException {
        int port = 6791 + 1;
        server = ServerBuilder
                .forPort(port)
                .addService(new LightProxyService())
                .build();
        logger.info("Starting server on {}", port);
        server.start();
        logger.info("Started server on {}", port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    Proxy.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        logger.info("Wait for shutdown");
        if (server != null) {
            server.awaitTermination();
        }
        logger.info("Shutdown signalled");
    }

    public static void main(String[] args) {
        logger.info("Initializing server");

        Utils.initializeTracing("proxy");

        Proxy proxy = new Proxy();
        try {
            proxy.start();
            proxy.blockUntilShutdown();
        } catch (Exception e) {
            logger.error("Error", e);
        }

    }
}
