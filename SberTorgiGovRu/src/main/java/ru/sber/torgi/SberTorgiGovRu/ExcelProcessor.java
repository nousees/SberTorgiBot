package ru.sber.torgi.SberTorgiGovRu;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class ExcelProcessor {

    public void processExcelFile(long chatId, Workbook workbook, TorgiBot bot) throws TelegramApiException {
        Sheet sheet = workbook.getSheetAt(0);
        for (Row row : sheet) {
            if (row.getRowNum() < 2) continue;

            String noticeNumber = escapeMarkdown(getCellValue(row.getCell(3)));
            String lotNumber = escapeMarkdown(getCellValueAsInteger(row.getCell(16)));
            String startDate = escapeMarkdown(getCellValue(row.getCell(6)));
            String endDate = escapeMarkdown(getCellValue(row.getCell(7)));
            String auctionDate = escapeMarkdown(getCellValue(row.getCell(9)));
            String price = escapeMarkdown(getCellValue(row.getCell(26)));
            String description = escapeMarkdown(getCellValue(row.getCell(19)));
            String urlLot = escapeMarkdown(getCellValue(row.getCell(18)));

            String lotInfo = String.format(
                    "**_Извещение:_** %s\n" +
                            "**_Номер лота:_** %s\n" +
                            "**_Дата начала подачи заявок:_** %s\n" +
                            "**_Дата окончания подачи заявок:_** %s\n" +
                            "**_Дата проведения торгов:_** %s\n" +
                            "**_Начальная цена:_** %s рублей\n" +
                            "**_Описание:_** %s\n" +
                            "**_Ссылка на лот:_** %s\n",
                    noticeNumber, lotNumber, startDate, endDate, auctionDate, price, description, urlLot
            );

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.enableMarkdownV2(true);
            message.setText(lotInfo);
            bot.execute(message);
        }
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private String getCellValueAsInteger(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC) {
            double numericValue = cell.getNumericCellValue();
            return String.valueOf((int) numericValue);
        }
        return getCellValue(cell);
    }
}