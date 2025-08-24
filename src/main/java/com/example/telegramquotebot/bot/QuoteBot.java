package com.example.telegramquotebot.bot;

import com.example.telegramquotebot.config.BotProperties;
import com.example.telegramquotebot.service.ImageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executor;

@Component
public class QuoteBot extends TelegramLongPollingBot {

    private final ImageService imageService;
    private final BotProperties props;
    private final Executor botExecutor;

    public QuoteBot(ImageService imageService, BotProperties props, @Qualifier("botExecutor") Executor botExecutor) {
        super(props.getToken());
        this.imageService = imageService;
        this.props = props;
        this.botExecutor = botExecutor;
    }

    @Override
    public String getBotUsername() {
        return props.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null || !update.hasMessage()) return;
        Message msg = update.getMessage();
        if (!msg.hasText()) return;

        String text = msg.getText().trim();
        if (!text.startsWith("/q")) {
            // Ignore all other messages/commands
            return;
        }
        // Process asynchronously
        botExecutor.execute(() -> handleQuote(msg));
    }

    private void handleQuote(Message commandMessage) {
        try {
            String quote;
            if (commandMessage.isReply() && commandMessage.getReplyToMessage() != null) {
                Message replied = commandMessage.getReplyToMessage();
                if (replied.hasText()) {
                    quote = replied.getText();
                } else if (replied.getCaption() != null && !replied.getCaption().isBlank()) {
                    quote = replied.getCaption();
                } else {
                    quote = "Нужно отправлять в ответ на сообщение";
                }
            } else {
                quote = "Нужно отправлять в ответ на сообщение";
            }

            byte[] png = imageService.buildQuotePng(quote);

            SendPhoto send = new SendPhoto();
            send.setChatId(commandMessage.getChatId());
            send.setPhoto(new InputFile(new ByteArrayInputStream(png), "quote.png"));
            // Reply to the command so it appears threaded
            send.setReplyToMessageId(commandMessage.getMessageId());

            execute(send);
        } catch (TelegramApiException | java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
