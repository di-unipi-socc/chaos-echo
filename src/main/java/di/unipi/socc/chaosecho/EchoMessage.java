package di.unipi.socc.chaosecho;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EchoMessage {

    @JsonProperty
    private int hash;

    @JsonProperty
    private String content;

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toString() {
        return "{ \"hash\": \"" + hash + "\", \"content\": \"" + content + "\" }"; 
    }

    public static EchoMessage random() {
        String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        EchoMessage em = new EchoMessage();

        Random rand = new Random();

        // Generate random content
        int contentSize = rand.nextInt(500);
        StringBuilder contentBuffer = new StringBuilder(contentSize);
        for(int i=0; i<contentSize; i++) {
            contentBuffer.append(alphabet.charAt(rand.nextInt(alphabet.length())));
        }
        em.setContent(contentBuffer.toString());

        // Hash content
        em.setHash(em.getContent().hashCode());

        return em;
    } 

    public static EchoMessage fail(String content) {
        EchoMessage em = new EchoMessage();
        em.setContent(content);
        em.setHash(content.hashCode());
        return em;
    }
    
}
