# Chaos Echo

This repository contains the source code of the **Chaos Echo Service**, which can be used to create applications to "chaos test" solutions for fault resiliency in multi-service applications (e.g., fault recovery, detection, or root cause analysis).

## How it works
TBA

## Running Chaos Echo Services
The **Chaos Echo Service** comes as a [Docker container](https://hub.docker.com/r/diunipisocc/chaosecho) that can be run standalone or (suggested) included in Docker Compose files to deploy arbitrary multi-service application topologies.

### Service configuration
Each containerised service requires a set of environment variables to be specified to effectively run:
* `BACKEND_SERVICES` -> string containing the hostnames (separated by "`:`") of the Chaos Echo services it may invoke while emulating the processing of an incoming request;
* `TIMEOUT` -> value of the time interval the service would wait for an answer to be provided by a backend service (whenever the latter is invoked); 
* `PICK_PERCENTAGE` -> probability of invoking any of the backend services while processing an incoming request, expressed as a percentage, viz., as an integer between `0` and `100`;  
* `FAIL_PERCENTAGE` -> probability of failing while processing an incoming request, expressed as a percentage, viz., as an integer between `0` and `100`.

### Exposing services
Each deployed instance of the Chaos Echo Service can be made accessible by exploiting Docker's native [port mapping](https://docs.docker.com/config/containers/container-networking/). It indeed only requires mapping the container's internal port `80` to any of the ports available on the host where the container is deployed.

### Generating traffic
This repository includes a bash script (viz., [generate_traffic.sh](generate_traffic.sh)) enabling to load any exposed Chaos Echo Service. 
The script enables sending a given number of requests, each after the other, by waiting a given period between subsequent requests. 

The usage of the script is as follows:
```
./generate_traffic.sh \
    [-a|--address <address>:<port>] \
    [-n|--number <numberOfRequests>] \
    [-p|--period <timeIntervalInSeconds> ]
```
By default, the script invokes the service published at `localhost:8080` by sending it `1000` requests every millisecond (viz., every `0.001` seconds)


## Running available examples 

1. Go in example folder
2. Run `docker stack deploy -c docker-compose.yml echo`
3. (If Kibana deployed) connect to `http://<ipAddress>:5601` and browse service logs 

TODOs: 

* process logs to extract interesting fields
* develop docker compose generator for creating docker-compose.yml given users' desired topology
* !!! check if distributed tracing is needed: how to understand if an answer is for the same question? (perhaps enough to adapt `echo` logging to log before after same service invocation?) !!!