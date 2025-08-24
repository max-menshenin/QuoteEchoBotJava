package com.example.telegramquotebot;

import com.example.telegramquotebot.bot.QuoteBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class QuoteBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuoteBotApplication.class, args);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(QuoteBot bot) throws Exception {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        return api;
    }
}

