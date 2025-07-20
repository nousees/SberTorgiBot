package ru.sber.torgi.SberTorgiGovRu;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TorgiBot extends TelegramLongPollingBot {

    private final BotConfiguration config;
    private final MessageHandler messageHandler;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public TorgiBot(BotConfiguration config, MessageHandler messageHandler) {
        this.config = config;
        this.messageHandler = messageHandler;
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            executorService.submit(() -> {
                try {
                    messageHandler.handleUpdate(update, this);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void onClose() {
        executorService.shutdown();
    }
}