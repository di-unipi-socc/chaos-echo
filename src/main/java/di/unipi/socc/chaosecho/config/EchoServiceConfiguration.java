package di.unipi.socc.chaosecho.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "config")
public class EchoServiceConfiguration {

    private String backendServices;
    private int timeout;
    private int pickProbability;
    private int failProbability;

    public String getBackendServices() {
        return backendServices;
    }

    public void setBackendServices(String backendServices) {
        this.backendServices = backendServices;
    }
    
    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getPickProbability() {
        return pickProbability;
    }

    public void setPickProbability(int pickProbability) {
        this.pickProbability = pickProbability;
    }
    
    public int getFailProbability() {
        return failProbability;
    }

    public void setFailProbability(int failProbability) {
        this.failProbability = failProbability;
    }
}
