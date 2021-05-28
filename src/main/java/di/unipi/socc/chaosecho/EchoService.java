package di.unipi.socc.chaosecho;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class EchoService {

	public static void main(String[] args) {
		SpringApplication.run(EchoService.class, args);
	}

}
