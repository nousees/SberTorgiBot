package ru.sber.torgi.SberTorgiGovRu;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class SearchService {

    private final BotConfiguration config;
    private final ExcelProcessor excelProcessor;

    public SearchService(BotConfiguration config, ExcelProcessor excelProcessor) {
        this.config = config;
        this.excelProcessor = excelProcessor;
    }

    public void searchAndExport(long chatId, String query, String searchType, TorgiBot bot) throws TelegramApiException {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://torgi.gov.ru/new/api/public/lotcards/export/excel?text=" + encodedQuery +
                    "&byFirstVersion=true&sort=firstVersionPublicationDate,desc";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .build();

            HttpResponse<byte[]> response = config.getHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                sendResponse(chatId, "Ошибка при загрузке файла: HTTP " + response.statusCode(), bot);
                return;
            }

            try (ByteArrayInputStream bis = new ByteArrayInputStream(response.body());
                 Workbook workbook = new XSSFWorkbook(bis)) {
                excelProcessor.processExcelFile(chatId, workbook, bot);
            }

            sendResponse(chatId, "Поиск завершен.", bot);
        } catch (Exception e) {
            sendResponse(chatId, "Ошибка при поиске: " + e.getMessage(), bot);
        }
    }

    private void sendResponse(long chatId, String text, TorgiBot bot) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        bot.execute(message);
    }
}