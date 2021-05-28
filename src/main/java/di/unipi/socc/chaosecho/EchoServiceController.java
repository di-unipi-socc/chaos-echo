package di.unipi.socc.chaosecho;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/echo")
public class EchoServiceController {

    @GetMapping
    String echo() {
        return "Ciao Pino!";
    }
    
}
