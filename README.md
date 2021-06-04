# Chaos Echo

This repository contains the source code of the **Chaos Echo Service**, which can be used to create applications to "chaos test" solutions for fault resiliency in multi-service applications (e.g., fault recovery, detection, or root cause analysis).

## How it works
Each instance of Chaos Echo Service emulates a service processing incoming requests by invoking backend services, if any.

### Failure behaviour
An instance of Chaos Echo Service can also fail in processing incoming requests, either
* since some backend service returns error messages once invoked, or
* since such instance fails on its own.

To emulate multiple possible behaviours for processing an incoming request, the backend services are invoked with a given (and customisable) probability, so that each incoming request is processed by invoking a random subset of the backend service.  

Similarly, an instance of the Chaos Echo Service fails while processing an incoming request with a given (and customisable) probability. 
Once failing, the instance of the Chaos Echo Service can either (i) return an error message and continue working or (ii) stop working without returning any answer to the service that send the request being processed. 
The events (i) and (ii) are equally probable. 

### Logging behaviour

Events happening on the application are logged with a severity level, viz.,
* `ERROR` -> error events, e.g., receiving an error message from some backend service or failing to process an incoming request (in the case of crashing, it is not ensured that the corresponding failure is logged by the service, to emulate the case of services unexpectedly failing without logging anything);
* `INFO` -> any other event (e.g., receiving or answering to an incoming request, or sending a message to a backend service).

To ensure that all notable events of the Chaos Echo Service are logged, and to provide the actually logged events with a ground thruth of what actually happended, each notable event is ensured to be logged with severity `DEBUG`. 

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

### Loading services
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

### Generating Docker Compose files
The [Chaos Echo Composer](https://github.com/di-unipi-socc/chaos-echo-composer) enables generating Docker Compose files for running multiple Chaos Echo Services to emulate multi-service applications.

## Running available examples 
Examples of Docker Compose files for running multiple interconnected Chaos Echo Services are available in a dedicated [folder](deploy/examples).

To run such examples, after cloning this repository on the machine/cluster where to run the examples, 
* move to the folder of the example to run and
* run the command `docker stack deploy -c docker-compose.yml echo`

This will deploy the specified application, whose frontend can be reached at
`http://<ipAddress>:8080`. 

Deployed applications can then be loaded by exploiting the script [generate_traffic.sh](generate_traffic.sh). This will produce logs that can be graphically browsed on the Kibana instance deployed alongside the application, which can be reached at `http://<ipAddress>:5601`. The produced logs are also stored in a logfile (viz., `echo-YYYY-MM-DD.log`), saved in the example folder.