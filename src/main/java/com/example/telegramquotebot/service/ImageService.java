package com.example.telegramquotebot.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

@Service
public class ImageService {

    public byte[] buildQuotePng(String text) throws IOException {
        if (text == null || text.isBlank()) {
            text = "Нужно отправлять в ответ на сообщение";
        }
        // Canvas params
        int width = 1200;
        int height = 628; // social-friendly ratio
        int padding = 60;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            // Smooth drawing
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            // Background minimalistic
            g.setPaint(new GradientPaint(0, 0, new Color(245, 245, 245), width, height, new Color(230, 230, 230)));
            g.fillRect(0, 0, width, height);

            // Card
            int cardX = padding;
            int cardY = padding;
            int cardW = width - padding * 2;
            int cardH = height - padding * 2;
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Float(cardX, cardY, cardW, cardH, 32, 32));

            // Quote mark
            g.setColor(new Color(0, 0, 0, 40));
            g.setFont(new Font("Serif", Font.BOLD, 180));
            g.drawString("“", cardX + 20, cardY + 180);

            // Text
            g.setColor(Color.BLACK);
            Font textFont = new Font("SansSerif", Font.PLAIN, 42);
            g.setFont(textFont);

            int textAreaX = cardX + 60;
            int textAreaY = cardY + 80;
            int textAreaW = cardW - 120;
            int textAreaH = cardH - 160;

            AttributedString attStr = new AttributedString(text);
            attStr.addAttribute(java.awt.font.TextAttribute.FONT, textFont);
            AttributedCharacterIterator it = attStr.getIterator();
            FontRenderContext frc = g.getFontRenderContext();
            LineBreakMeasurer measurer = new LineBreakMeasurer(it, frc);

            float wrapWidth = textAreaW;
            float x = textAreaX;
            float y = textAreaY;

            while (measurer.getPosition() < it.getEndIndex()) {
                TextLayout layout = measurer.nextLayout(wrapWidth);
                y += layout.getAscent();
                if (y > textAreaY + textAreaH) {
                    // truncate with ellipsis
                    String ell = "...";
                    g.drawString(ell, (int)(textAreaX + textAreaW - g.getFontMetrics().stringWidth(ell)), (int)y);
                    break;
                }
                layout.draw(g, x, y);
                y += layout.getDescent() + layout.getLeading();
            }

            // Footer small
            g.setFont(new Font("SansSerif", Font.PLAIN, 18));
            g.setColor(new Color(0,0,0,120));
            String footer = "echoTelegram1991bot";
            int fw = g.getFontMetrics().stringWidth(footer);
            g.drawString(footer, cardX + cardW - fw - 20, cardY + cardH - 20);

        } finally {
            g.dispose();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}
