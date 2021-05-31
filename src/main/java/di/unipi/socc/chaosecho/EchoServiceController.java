package di.unipi.socc.chaosecho;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/echo")
public class EchoServiceController {

    private static Logger log = LoggerFactory.getLogger(EchoServiceController.class);

    @Value("${BACKEND_SERVICES}")
    private String backendServices;

    @Value("${TIMEOUT}")
    private int timeout;

    @Value("${PICK_PERCENTAGE}")
    private int pickProbability;

    @Value("${FAIL_PERCENTAGE}")
    private int failProbability;

    @PostMapping(
        consumes={MediaType.APPLICATION_JSON_VALUE},
        produces={MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<EchoMessage> echo(@RequestBody EchoMessage message) {
        // Logging the processing of received message
        log.info("Processing message: " + message);

        // Random number generator (utility)
        Random rand = new Random();
        
        // Reply message
        ResponseEntity<EchoMessage> reply = new ResponseEntity<EchoMessage>(message, HttpStatus.OK);

        // If there are backend services 
        if(backendServices != null) {
            // Rest client setup
            SimpleClientHttpRequestFactory client = new SimpleClientHttpRequestFactory();
            client.setConnectTimeout(timeout);
            client.setReadTimeout(timeout);
            RestTemplate rt = new RestTemplate(client);

            // Send random messages to (a random subset of) backend services to emulate message processing
            for (String service : backendServices.split(":")) {
                int pickValue = rand.nextInt(100);
                if(pickValue <= pickProbability) {   
                    // Message preparation
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<String> request = new HttpEntity<String>(EchoMessage.random().toJSONString(), headers);
                    
                    // Sending request message and waiting for response
                    log.info("Sending message to " + service);
                    try {
                        String serviceURL = "http://" + service;
                        ResponseEntity<EchoMessage> response = rt.postForEntity(serviceURL, request, EchoMessage.class);
                        log.info("Receiving answer from " + service);

                        // Checking response's status code
                        if(!response.getStatusCode().equals(HttpStatus.OK)) {
                            log.error("Error response (code: " + response.getStatusCode() + ") received from " + service);
                            reply = new ResponseEntity<EchoMessage>(EchoMessage.fail("Failing to contact backend services"), HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    } catch (Exception e) {
                        log.error("Failing to contact " + service + ". Root cause: " + e);
                        reply = new ResponseEntity<EchoMessage>(EchoMessage.fail("Failing to contact backend services"), HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                } 
            }
        }
        
        // Checks whether it (randomly) failed, independently from backend services 
        int failValue = rand.nextInt(100);
        if (failValue <= failProbability) {
            boolean unresponsive = rand.nextBoolean();
            // Case: Service becoming unresponsive (internal failure not logged)
            if(unresponsive) {
                log.debug("Unresponsive");
                try {
                    Thread.sleep(10*timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
            // Case: Internal, unexpected failure (recognised and logged)
            else {
                log.error("Failing on my own");
                reply = new ResponseEntity<EchoMessage>(EchoMessage.fail("Unexpected error"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // TODO : Add debug logging for "ground truth"

        // TODO : Adjust/standardise log messages

        return reply;
    }
    
}
