package di.unipi.socc.chaosecho;

import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/echo")
public class EchoServiceController {

    private static Logger log = LoggerFactory.getLogger(EchoServiceController.class);

    @Autowired
    private ApplicationContext appContext;

    @Value("${DEPENDS_ON:#{null}}") // default to null, if no backend service is listed
    private String backendServices;

    @Value("${TIMEOUT:#{10000}}") // default to 10000, if not specified
    private int timeout;

    @Value("${P_INVOKE:#{100}}") // default to 1, if not specified
    private int invokeProbability;

    @Value("${P_FAIL:#{10}}") // default to 0.1, if not specified
    private int failProbability;

    @Value("${P_CRASH:#{50}}") // default to 0.5, if not specified
    private int crashProbability;

    @PostMapping(
        consumes={MediaType.APPLICATION_JSON_VALUE},
        produces={MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<EchoMessage> echo(@RequestBody EchoMessage message) {
        // Random number generator (utility)
        Random rand = new Random();
        
        // If frontend, generate message to propagate to backend services
        // (using the same message to provide "ground-thruth" for analysing failure cascades)
        EchoMessage messageForBackend = message;
        while(messageForBackend.getContent().toUpperCase().contains("FRONTEND"))
            messageForBackend = EchoMessage.random();
        if(!messageForBackend.equals(message))
            log.info("Message [ " + messageForBackend + " ] created");

        // Reply message
        ResponseEntity<EchoMessage> reply = new ResponseEntity<EchoMessage>(message, HttpStatus.OK);

        // If there are backend services 
        if(backendServices != null) {
            // Rest client setup
            SimpleClientHttpRequestFactory client = new SimpleClientHttpRequestFactory();
            client.setConnectTimeout(timeout);
            client.setReadTimeout(timeout);
            RestTemplate rt = new RestTemplate(client);

            // If there exists backend services to invoke
            if(backendServices != null) {
                // Send random messages to (a random subset of) backend services to emulate message processing
                for (String service : backendServices.split(":")) {
                    int invokeValue = rand.nextInt(100);
                    if(invokeValue < invokeProbability) {   
                        // Message preparation
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON); // Declaring content type
                        headers.set("X-Request-ID", UUID.randomUUID().toString()); // Assigning unique id to requests with standard HTTP header
                        HttpEntity<EchoMessage> request = new HttpEntity<EchoMessage>(messageForBackend, headers);
                        
                        // Sending request message and waiting for response
                        log.info("Sending message to " + service + " (request_id: " + headers.get("X-Request-ID") + ")");
                        log.info("Sent message: " + messageForBackend + " (request_id: " + headers.get("X-Request-ID") + ")");
                        try {
                            String endpoint = "http://" + service + "/echo";
                            rt.postForEntity(endpoint, request, EchoMessage.class);
                            log.info("Receiving answer from " + service + " (request_id: " + headers.get("X-Request-ID") + ")");
                        } catch (HttpServerErrorException e) {
                            log.error("Error response (code: " + e.getRawStatusCode() + ") received from " + service  + " (request_id: " + headers.get("X-Request-ID") + ")");
                                reply = new ResponseEntity<EchoMessage>(EchoMessage.fail("Failing to contact backend services"), HttpStatus.INTERNAL_SERVER_ERROR);
                        } catch (Exception e) {
                            log.error("Failing to contact " + service + " (request_id: " + headers.get("X-Request-ID") + "). Root cause: " + e);
                            reply = new ResponseEntity<EchoMessage>(EchoMessage.fail("Failing to contact backend services"), HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    } 
                }
            }
        }
        
        // Checks whether it (randomly) failed, independently from backend services 
        int failValue = rand.nextInt(100);
        if (failValue < failProbability) {
            // Case: Service unexpectedly crashing (sometimes logging, sometimes not)
            int crashValue = rand.nextInt(100);
            if(crashValue < crashProbability) {
                log.debug("Crashing");
                boolean crashLogged = rand.nextBoolean();
                if(crashLogged) {
                    log.error("Crashing due to unrecoverable error");
                }
                SpringApplication.exit(appContext, () -> -1);
            }
            // Case: Internal, unexpected failure (recognised and logged)
            else {
                log.error("Failing on my own");
                reply = new ResponseEntity<EchoMessage>(EchoMessage.fail("Unexpected error"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return reply;
    }
    
}
