package di.unipi.socc.chaosecho;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/echo")
public class EchoServiceController {

    private static Logger log = LoggerFactory.getLogger(EchoServiceController.class);

    @Value("${config.backendServices}")
    private String backendServices;

    @Value("${config.pickProbability}")
    private int pickProbability;

    @Value("${config.failProbability}")
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
            // Send random messages to (a random subset of) backend services to emulate message processing
            for (String service : backendServices.split(":")) {
                int pickValue = rand.nextInt(100);
                if(pickValue <= pickProbability) {
                    log.info("Sending message to " + service);
                    // TODO send message
                    log.info("Receiving answer from " + service);
                } 
            }
        }

        // TODO : Change reply's status code based on answers from "depends on" services
        
        // Checks whether it (randomly) failed, independently from backend services 
        int failValue = rand.nextInt(100);
        if (failValue <= failProbability) {
            log.error("Failing on my own");
            reply = new ResponseEntity<EchoMessage>(EchoMessage.fail("Unexpected error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // TODO : Add debug logging for "ground truth"

        // TODO : Adjust/standardise log messages

        return reply;
    }
    
}
