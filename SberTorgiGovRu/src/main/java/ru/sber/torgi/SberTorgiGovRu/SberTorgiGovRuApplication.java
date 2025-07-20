package ru.sber.torgi.SberTorgiGovRu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class SberTorgiGovRuApplication {
	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(SberTorgiGovRuApplication.class, args);
		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			TorgiBot torgiBot = context.getBean(TorgiBot.class);
			botsApi.registerBot(torgiBot);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}