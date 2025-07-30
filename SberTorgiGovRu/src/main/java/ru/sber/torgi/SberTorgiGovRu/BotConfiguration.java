package ru.sber.torgi.SberTorgiGovRu;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class BotConfiguration {

    private String botToken;

    private String botUsername;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BotConfiguration(    
        @Value("${telegram.bot.username}") String botUsername, 
        @Value("${telegram.bot.token}") String botToken
        ) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.botToken = botToken;
        this.botUsername = botUsername;
        System.out.println("Bot username: " + botUsername);
        System.out.println("Bot token: " + botToken);
        Logger.getLogger("org.apache.http").setLevel(Level.SEVERE);
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}