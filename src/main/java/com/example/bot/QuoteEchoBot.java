package com.example.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.FontMetrics;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QuoteEchoBot extends TelegramLongPollingBot {

    private final Map<Long, String> lastMessages = new ConcurrentHashMap<>();

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String text = message.getText();

        if ("/q".equals(text.trim())) {
            String quote = lastMessages.get(chatId);
            if (quote != null && !quote.isEmpty()) {
                try {
                    File image = generateImageFromText(quote);
                    sendPhoto(chatId, image);
                    lastMessages.remove(chatId);
                    image.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            lastMessages.put(chatId, text);
        }
    }

    private void sendPhoto(Long chatId, File image) {
        try {
            SendPhoto photo = new SendPhoto();
            photo.setChatId(chatId.toString());
            photo.setPhoto(new InputFile(image));
            execute(photo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File generateImageFromText(String text) throws IOException {
        int width = 800;
        int height = 400;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 24));

        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();

        List<String> lines = wrapText(text, fm, width - 40);
        int y = 50;

        for (String line : lines) {
            g.drawString(line, 20, y);
            y += lineHeight;
        }

        g.dispose();

        File output = File.createTempFile("quote-", ".png");
        ImageIO.write(image, "png", output);
        return output;
    }

    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String testLine = line + (line.length() == 0 ? "" : " ") + word;
            if (fm.stringWidth(testLine) > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }
}
