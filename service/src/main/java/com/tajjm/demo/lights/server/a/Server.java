package com.tajjm.demo.lights.server.a;

import com.tajjm.demo.lights.Utils;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private io.grpc.Server server;

    private void start() throws IOException {
        int port = 6791;
        server = ServerBuilder
                .forPort(port)
                .addService(new LightService())
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
                    Server.this.stop();
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
        if (System.getProperty("FLUENTD_SERVER") == null || System.getProperty("FLUENTD_SERVER").trim().equals("")) {
            System.setProperty("FLUENTD_SERVER", "127.0.0.1");
            System.out.println("No system property set for logging to fluentd, default to localhost");
        } else {
            System.out.println("Using system property to configure logging to fluentd at " + System.getProperty("FLUENTD_SERVER"));
        }
        logger.info("Initializing server");

        Utils.initializeTracing("service");

        Server server = new Server();
        try {
            server.start();
            server.blockUntilShutdown();
        } catch (Exception e) {
            logger.error("Error", e);
        }

    }
}
