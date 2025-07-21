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
import java.util.regex.Pattern;

@Component
public class MessageHandler {

    private static final int MAX_MESSAGE_LENGTH = 4096;
    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");
    private static final Pattern GOSNOMER_PATTERN = Pattern.compile("^[А-Я]\\d{3}[А-Я]{2}\\d{2,3}$");
    private static final Pattern CADASTRAL_PATTERN = Pattern.compile("^\\d{2}:\\d{2}:\\d{6,7}:\\d+$");

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
            sendResponse(chatId, "Пожалуйста, введите госномер (например, А123БВ45) или VIN (17 буквенно-цифровых символов):", bot);
            chatState.put(chatId, "MOVABLE");
        } else if (messageText.equals("Поиск недвижимости")) {
            sendResponse(chatId, "Пожалуйста, введите кадастровый номер (например, 12:34:567890:123):", bot);
            chatState.put(chatId, "REALTY");
        } else if (chatState.containsKey(chatId)) {
            String state = chatState.get(chatId);
            if (validateInput(messageText, state)) {
                searchService.searchAndExport(chatId, messageText, state, bot);
                chatState.remove(chatId);
            } else {
                String errorMessage = state.equals("MOVABLE")
                        ? "Неверный формат! Введите госномер (например, А123БВ45) или VIN (17 буквенно-цифровых символов)."
                        : "Неверный формат! Введите кадастровый номер (например, 12:34:567890:123).";
                sendResponse(chatId, errorMessage, bot);
            }
        } else {
            sendResponse(chatId, "Пожалуйста, используйте кнопки ниже для выбора типа поиска.", bot);
        }
    }

    private boolean validateInput(String input, String state) {
        if (state.equals("MOVABLE")) {
            return VIN_PATTERN.matcher(input).matches() || GOSNOMER_PATTERN.matcher(input).matches();
        } else if (state.equals("REALTY")) {
            return CADASTRAL_PATTERN.matcher(input).matches();
        }
        return false;
    }

    private void sendWelcomeMessage(long chatId, TorgiBot bot) throws TelegramApiException {
        String welcomeText = "Добро пожаловать в SberTorgiBot! 🎉\n" +
                "Этот бот помогает искать движимое и недвижимое имущество на torgi.gov.ru.\n" +
                "Используйте кнопки ниже для начала поиска:\n" +
                "- Поиск движимого имущества: введите госномер (например, А123БВ45) или VIN (17 символов).\n" +
                "- Поиск недвижимости: введите кадастровый номер (например, 12:34:567890:123).";
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