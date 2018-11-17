package com.owl.thingsbot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.Objects;

@SpringBootApplication
@Slf4j
@EnableScheduling
public class ThingsBotApplication {

    @Autowired
    private Bot bot;
    private TelegramBotsApi telegramBotsApi;

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(ThingsBotApplication.class, args);
    }

    @Scheduled(fixedDelay = 60000)
    private void createBot() {
        if (Objects.isNull(telegramBotsApi)) {
            telegramBotsApi = new TelegramBotsApi();
            try {
                telegramBotsApi.registerBot(bot);
            } catch (TelegramApiRequestException e) {
                log.error("Failed to register bot", e);
            }
        }
    }
}
