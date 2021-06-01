# Chaos Echo

## Running Examples 

1. Go in example folder
2. Run `docker stack deploy -c docker-compose.yml echo`
3. (If Kibana deployed) connect to `http://<ipAddress>:5601` and browse service logs 

TODOs: 

* process logs to extract interesting fields
* develop docker compose generator for creating docker-compose.yml given users' desired topology
* !!! check if distributed tracing is needed: how to understand if an answer is for the same question? (perhaps enough to adapt `echo` logging to log before after same service invocation?) !!!