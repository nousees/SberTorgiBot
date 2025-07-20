package ru.sber.torgi.SberTorgiGovRu;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class BotConfiguration {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BotConfiguration() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
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