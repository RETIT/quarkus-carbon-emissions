# quarkus-carbon-emissions
Demo on how to publish carbon emissions for quarkus microservices.

In order to use the service you need to start an OpenTelemetry Collector, a Prometheus instance and Grafana using the following docker command from the root directory of the project:  

    docker compose -f ./docker/docker-compose.yml up -d

Furthermore, you need to have a climatiq api key with "Cloud Computing" permissions you can acquire here: http://climatiq.io/

Once the backend service are started, you can start the actual quarkus service using the following commands from the root directory of the project. Windows command prompt:

    set CLIMATIQ_API_KEY=<your_climatiq_api_key>
    mvnw quarkus:dev

Linux shell:

    export CLIMATIQ_API_KEY=<your_climatiq_api_key>
    ./mvnw quarkus:dev

In order to visualize the data you need to import the dashboard defined in the following file in your grafana instance following the documentation provided here https://grafana.com/docs/grafana/latest/dashboards/build-dashboards/import-dashboards/:
    
    ./grafana/dashboard.json

