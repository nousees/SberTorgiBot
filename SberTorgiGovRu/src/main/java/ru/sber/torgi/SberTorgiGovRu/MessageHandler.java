package ru.sber.torgi.SberTorgiGovRu;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MessageHandler {

    private static final int MAX_MESSAGE_LENGTH = 4096;
    private final Map<Long, String> chatState = new ConcurrentHashMap<>();
    private final SearchService searchService;

    public MessageHandler(SearchService searchService) {
        this.searchService = searchService;
    }

    public void handleUpdate(Update update, TorgiBot bot) throws TelegramApiException {
        String messageText = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();

        if (messageText.equals("/start")) {
            sendWelcomeMessage(chatId, bot);
            chatState.remove(chatId);
        } else if (messageText.equals("Поиск движимого имущества")) {
            sendResponse(chatId, "Пожалуйста, введите госномер или VIN для поиска движимого имущества:", bot);
            chatState.put(chatId, "MOVABLE");
        } else if (messageText.equals("Поиск недвижимости")) {
            sendResponse(chatId, "Пожалуйста, введите адрес или кадастровый номер для поиска недвижимости:", bot);
            chatState.put(chatId, "REALTY");
        } else if (chatState.containsKey(chatId)) {
            searchService.searchAndExport(chatId, messageText, chatState.get(chatId), bot);
            chatState.remove(chatId);
        } else {
            sendResponse(chatId, "Пожалуйста, используйте кнопки ниже для выбора типа поиска.", bot);
        }
    }

    private void sendWelcomeMessage(long chatId, TorgiBot bot) throws TelegramApiException {
        String welcomeText = "Добро пожаловать в SberTorgiBot! 🎉\n" +
                "Этот бот помогает искать движимое и недвижимое имущество на torgi.gov.ru.\n" +
                "Используйте кнопки ниже для начала поиска:\n" +
                "- Поиск движимого имущества: введите госномер или VIN.\n" +
                "- Поиск недвижимости: введите адрес или кадастровый номер.";
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(welcomeText);
        message.setReplyMarkup(createKeyboard());
        bot.execute(message);
    }

    private void sendResponse(long chatId, String text, TorgiBot bot) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text.length() > MAX_MESSAGE_LENGTH ? text.substring(0, MAX_MESSAGE_LENGTH) : text);
        message.setReplyMarkup(createKeyboard());
        bot.execute(message);
        if (text.length() > MAX_MESSAGE_LENGTH) {
            sendResponse(chatId, text.substring(MAX_MESSAGE_LENGTH), bot);
        }
    }

    private ReplyKeyboardMarkup createKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Поиск движимого имущества");
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Поиск недвижимости");
        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}