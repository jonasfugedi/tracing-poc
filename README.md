# Demo Project for Distributed Tracing

This is a sample project to show how distributed tracing and log aggregation can be solved.

## Running the demo

### 1. Start jaeger 

    docker run -d --name jaeger \
      -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
      -p 5775:5775/udp \
      -p 6831:6831/udp \
      -p 6832:6832/udp \
      -p 5778:5778 \
      -p 16686:16686 \
      -p 14268:14268 \
      -p 14250:14250 \
      -p 9411:9411 \
      jaegertracing/all-in-one:1.16
      
### 2. Run service A
### 3. Run service B
### 4. Run client

## Development

### Build the project

    mvn install
    
### To check if something needs to be updated

    mvn versions:display-dependency-updates