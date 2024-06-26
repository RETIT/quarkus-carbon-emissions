# quarkus-carbon-emissions
Demo on how to publish carbon emissions for quarkus microservices using a simple Service that just provides three endpoints called getData (HTTP GET), postData (HTTP GET) and deleteData (HTTP DELETE).

In order to use the service you need to start an OpenTelemetry Collector, a Prometheus instance and Grafana using the following docker command from the root directory of the project:  

    docker compose -f ./docker/docker-compose.yml up -d

Furthermore, you need to have a climatiq api key with "Cloud Computing" permissions you can acquire here: http://climatiq.io/

Once the backend service are started, you can start the actual quarkus service using the following commands from the root directory of the project. Windows command prompt:

    set CLIMATIQ_API_KEY=<your_climatiq_api_key>
    mvnw quarkus:dev

Linux shell:

    export CLIMATIQ_API_KEY=<your_climatiq_api_key>
    ./mvnw quarkus:dev

In order to visualize the data you need to import the dashboard defined in the following file in your grafana instance following the documentation provided here https://grafana.com/docs/grafana/latest/dashboards/build-dashboards/import-dashboards/. You can reach the Grafana UI using http://localhost:3000/grafana:
    
    ./grafana/dashboard.json

Once the service is started, you can issue requests to the following endpoints:

    curl --request GET --url http://localhost:8080/test-rest-endpoint
    curl --request POST --url http://localhost:8080/test-rest-endpoint
    curl --request DELETE --url http://localhost:8080/test-rest-endpoint

Once you have issued a few requests to one or more of these endpoints you will see the CO2 emissions for each endpoint in Grafana:

![Screenshot of the Grafana Dashboard](grafana/dashboard_screenshot.png)