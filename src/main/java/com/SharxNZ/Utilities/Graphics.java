package com.SharxNZ.Utilities;

import com.SharxNZ.Android24;
import com.SharxNZ.Commands.GameCommands.PowerPoints;
import com.SharxNZ.Commands.Level;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public abstract class Graphics {

//    public Graphics(){
//        super.name = "image";
//        super.aliases = new String[]{"im"};
//    }

    public static BufferedImage imageFromURL(String url) throws IOException {
        try {
            URL URL = new URL(url);
            return ImageIO.read(URL); // test for the saved info
        } catch (IOException e) {
            Android24.logError(e, url);
            return null;
        }
    }

    private static void frame3D(Graphics2D graphics2D, int stroke, int width, int height) {
        graphics2D.setColor(new Color(0, 0, 0, 70));
        graphics2D.drawLine(stroke, 0, width, 0);
        graphics2D.setColor(new Color(0, 0, 0, 50));
        graphics2D.drawLine(0, 0, 0, height);
        graphics2D.setColor(new Color(0, 0, 0, 130));
        graphics2D.drawLine(width, height, (int) (stroke / 2.0), height);
        graphics2D.setColor(new Color(0, 0, 0, 160));
        graphics2D.drawLine(width, height - stroke, width, (int) (stroke / 2.0));
    }

    public static void drawStringWithOutline(Graphics2D g, String text,
                                             int x, int y,
                                             Color inColor, Color outColor, float strokeWidth) {

        BasicStroke outlineStroke = new BasicStroke(strokeWidth);

        /* g instanceof Graphics2D */

        if (g != null) {
            Graphics2D g2 = (Graphics2D) g;

            // remember original settings
            Color originalColor = g2.getColor();
            Stroke originalStroke = g2.getStroke();
            RenderingHints originalHints = g2.getRenderingHints();

            // create a glyph vector from your text
            GlyphVector glyphVector = g2.getFont().createGlyphVector(g2.getFontRenderContext(), text);
            // get the shape object
            Shape textShape = glyphVector.getOutline(x, y);

            // activate anti aliasing for text rendering (if you want it to look nice)
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            g2.setColor(outColor);
            g2.setStroke(outlineStroke);
            g2.draw(textShape); // draw outline

            g2.setColor(inColor);
            g2.fill(textShape); // fill the shape

            // reset to original settings after painting
            g2.setColor(originalColor);
            g2.setStroke(originalStroke);
            //g2.setRenderingHints(originalHints);
        }
    }

    public static void drawStringWithOutline(Graphics2D g, String text,
                                             int x, int y) {

        BasicStroke outlineStroke = new BasicStroke(3.0f);

        if (g != null) {
            Graphics2D g2 = (Graphics2D) g;

            // remember original settings
            Color originalColor = g2.getColor();
            Stroke originalStroke = g2.getStroke();
            RenderingHints originalHints = g2.getRenderingHints();

            // create a glyph vector from your text
            GlyphVector glyphVector = g2.getFont().createGlyphVector(g2.getFontRenderContext(), text);
            // get the shape object
            Shape textShape = glyphVector.getOutline(x, y);

            // activate anti aliasing for text rendering (if you want it to look nice)
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            g2.setColor(Color.BLACK);
            g2.setStroke(outlineStroke);
            g2.draw(textShape); // draw outline

            g2.setColor(originalColor);
            g2.fill(textShape); // fill the shape

            // reset to original settings after painting
            g2.setColor(originalColor);
            g2.setStroke(originalStroke);
            //g2.setRenderingHints(originalHints);
        }
    }

    public static void setBar() {

    }

    public byte[] welcomeImage(CommandEvent commandEvent) throws IOException {

        BufferedImage background = imageFromURL("https://cdn.discordapp.com/attachments/866689902758068244/900332586411384892/digital-digital-art-artwork-fantasy-art-drawing-hd-wallpaper-thumb.jpg");
        BufferedImage userImage = imageFromURL(commandEvent.getAuthor().getAvatarUrl());

        int backgroundWidth = background.getWidth();
        int backgroundHeight = background.getHeight();
        int userImageWidth = userImage.getWidth();
        int userImageHeight = userImage.getHeight();

        background = background.getSubimage(0, 50, backgroundWidth, backgroundHeight - 50);
        Graphics2D background2D = (Graphics2D) background.getGraphics();


        background2D.setStroke(new BasicStroke(30));
        frame3D(background2D, 30, backgroundWidth, backgroundHeight);

        background2D.setColor(Color.CYAN);
        background2D.setFont(new Font("Arial Black", Font.BOLD, 25));
        background2D.drawString("Welcome to the server!", backgroundWidth / 2 - 80, backgroundHeight / 3);
        background2D.drawString(commandEvent.getAuthor().getName(), backgroundWidth / 2 - 80, backgroundHeight / 3 + 40);


        GradientPaint paint = new GradientPaint(0, 30, new Color(10, 117, 189, 196),
                userImageWidth + 10, 10, new Color(212, 58, 255, 220));
        background2D.setPaint(paint);
        background2D.fillOval(backgroundWidth / 11 - 7, backgroundHeight / 4 - 7,
                userImageWidth + 14, userImageHeight + 14);
        background2D.clip(new Ellipse2D.Float(backgroundWidth / 11, backgroundHeight / 4,
                userImageWidth, userImageHeight));

        background2D.drawImage(userImage, null, backgroundWidth / 11, backgroundHeight / 4);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(background, "png", output);
        return output.toByteArray();
    }

    public static byte[] welcomeImage(GuildMemberJoinEvent guildMemberJoinEvent) throws IOException {

        BufferedImage background = imageFromURL("https://cdn.discordapp.com/attachments/866689902758068244/900332586411384892/digital-digital-art-artwork-fantasy-art-drawing-hd-wallpaper-thumb.jpg");
        BufferedImage userImage;
        if (guildMemberJoinEvent.getUser().getAvatarUrl() == null)
            userImage = imageFromURL("https://yt3.ggpht.com/ytc/AKedOLQc1OCf9gztVmcVnmI_41uN9axrRP8wd4a-GflFRQ=s176-c-k-c0x00ffffff-no-rj");
        else
            userImage = imageFromURL(guildMemberJoinEvent.getUser().getAvatarUrl());
        background = background.getSubimage(0, 50, background.getWidth(), background.getHeight() - 50);
        Graphics2D background2D = (Graphics2D) background.getGraphics();


        int backgroundWidth = background.getWidth();
        int backgroundHeight = background.getHeight();
        int userImageWidth = userImage.getWidth();
        int userImageHeight = userImage.getHeight();


        background2D.setStroke(new BasicStroke(30));
        frame3D(background2D, 30, backgroundWidth, backgroundHeight);

        background2D.setColor(Color.CYAN);
        background2D.setFont(new Font("Arial Black", Font.BOLD, 25));
        //background2D.drawString("Welcome to the server!", backgroundWidth/2 - 80, backgroundHeight/3);
        drawStringWithOutline(background2D, "Welcome to the server!",
                backgroundWidth / 2 - 80, backgroundHeight / 3);
        background2D.setFont(new Font("Arial", Font.BOLD, 25));
        //background2D.drawString(guildMemberJoinEvent.getUser().getName(), backgroundWidth/2 - 80, backgroundHeight/3 + 40);
        drawStringWithOutline(background2D, guildMemberJoinEvent.getUser().getName(),
                backgroundWidth / 2 - 80, backgroundHeight / 3 + 40);

        GradientPaint paint = new GradientPaint(0, 30, new Color(10, 117, 189, 196),
                userImageWidth + 10, 10, new Color(212, 58, 255, 220));
        background2D.setPaint(paint);
        background2D.fillOval(backgroundWidth / 11 - 7, backgroundHeight / 4 - 7,
                userImageWidth + 14, userImageHeight + 14);
        background2D.clip(new Ellipse2D.Float(backgroundWidth / 11, backgroundHeight / 4,
                userImageWidth, userImageHeight));

        background2D.drawImage(userImage, null, backgroundWidth / 11, backgroundHeight / 4);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(background, "png", output);
        return output.toByteArray();
    }

    public static byte[] levelImage(String userURL, String guildName, long xp) throws IOException {

        int lvl = Level.calculateLevel(xp);

        //Starting
        BufferedImage background = imageFromURL("https://cdn.discordapp.com/attachments/866689902758068244/900820398118170624/dragon-ball-dragon-ball-z-nappa-dragon-ball-raditz-dragon-ball-wallpaper-preview.jpg");
        //BufferedImage background = imageFromURL("https://www.comingsoon.net/assets/uploads/2021/05/dragon-ball-618x362.jpg");
        BufferedImage userImage;
        userImage = imageFromURL(Objects.requireNonNullElse(userURL, "https://www.google.co.il/url?sa=i&url=https%3A%2F%2Fwww.youtube.com%2Fchannel%2FUCZ5XnGb-3t7jCkXdawN2tkA&psig=AOvVaw2XPWp7NOBO1CkFGJRunLPs&ust=1620733791474000&source=images&cd=vfe&ved=0CAIQjRxqFwoTCKji_ISGv_ACFQAAAAAdAAAAABAJ"));
        background = background.getSubimage(0, 70, background.getWidth(), background.getHeight() - 70);
        Graphics2D background2D = (Graphics2D) background.getGraphics();

        //Set the bar
        int backgroundWidth = background.getWidth();
        int backgroundHeight = background.getHeight();
        int userImageWidth = (int) (userImage.getWidth() * 1.3);
        int userImageHeight = (int) (userImage.getHeight() * 1.3);
        long nextXP = (long) Math.pow(lvl + 1, 1 / Android24.difficulty);
        long previousXP = (long) Math.pow(lvl, 1 / Android24.difficulty);

        background2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        background2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        background2D.setStroke(new BasicStroke(32));
        frame3D(background2D, 32, backgroundWidth, backgroundHeight);

        int minLineWidth = backgroundWidth / 7;
        int maxLineWidth = (backgroundWidth * 6 / 7);
        float present = (float) ((xp - previousXP) * 1.0 / (nextXP - previousXP));
        int lineWidth = (int) (present * (maxLineWidth - minLineWidth));
        int strokeWidth = 30;
        int outlineWidth = 8;

        //Bar outline
        int circleY1 = backgroundHeight * 4 / 5 - (strokeWidth / 2 + outlineWidth / 2);
        background2D.setColor(Color.BLACK);
        background2D.setStroke(new BasicStroke(strokeWidth + outlineWidth));
        background2D.fillOval(minLineWidth - (strokeWidth + outlineWidth / 2), circleY1, strokeWidth + outlineWidth, strokeWidth + outlineWidth);
        background2D.fillOval(maxLineWidth, circleY1, strokeWidth + outlineWidth, strokeWidth + outlineWidth);
        background2D.drawLine(minLineWidth, backgroundHeight * 4 / 5, maxLineWidth, backgroundHeight * 4 / 5);

        //Bat inline
        Color color = new Color(0xDAFFDD00);
        Color color2 = new Color(0xFFFF0000);
        GradientPaint barPaint = new GradientPaint(minLineWidth, backgroundHeight * 4 / 5, color,
                maxLineWidth, backgroundHeight * 4 / 5, color2);
        background2D.setStroke(new BasicStroke(strokeWidth));
        background2D.setPaint(barPaint);
        background2D.fillOval(minLineWidth - strokeWidth, backgroundHeight * 4 / 5 - strokeWidth / 2, strokeWidth, strokeWidth);
        if (present > 0.9)
            background2D.fillOval(lineWidth + minLineWidth, backgroundHeight * 4 / 5 - strokeWidth / 2, strokeWidth, strokeWidth);
        background2D.drawLine(minLineWidth, backgroundHeight * 4 / 5, lineWidth + minLineWidth, backgroundHeight * 4 / 5);

        //Draw the numbers
        background2D.setColor(Color.GREEN);
        background2D.setFont(new Font("GROBOLD", Font.BOLD, 25));
        background2D.drawString(Integer.toString(lvl), backgroundWidth / 23, backgroundHeight * 5 / 6);
        background2D.drawString(Short.toString((short) (lvl + 1)), backgroundWidth * 11 / 12, backgroundHeight * 5 / 6);


        //Write the text
        Color c1 = new Color(0x23B2FF);
        background2D.setFont(new Font("GROBOLD", Font.BOLD, 35));
        background2D.setColor(new Color(198, 30, 30));
        drawStringWithOutline(background2D, "On ",
                backgroundWidth * 5 / 12, backgroundHeight / 3,
                c1, Color.BLACK, 4.0f);
        background2D.drawString("Your level is:  " + lvl, backgroundWidth * 5 / 12, backgroundHeight / 2);
        drawStringWithOutline(background2D, "Your level is:  " + lvl,
                backgroundWidth * 5 / 12, backgroundHeight / 2,
                Color.RED, Color.BLACK, 4.0f);
        // Reverse hebrew strings
        char[] chars = guildName.toCharArray();
        for (char c : chars) {
            if (c >= 0x5D0 && c <= 0x6ff) {
                guildName = new StringBuilder(guildName).reverse().toString();
                break;
            }
        }
        background2D.setFont(new Font("Ariel", Font.BOLD, 35));
        drawStringWithOutline(background2D, "        " + guildName,
                backgroundWidth * 5 / 12, backgroundHeight / 3,
                c1, Color.BLACK, 4.0f);


        //Draw the outline circle
        GradientPaint UserIconPaint = new GradientPaint(0, 30, new Color(245, 33, 33, 196),
                userImageWidth + 10, 10, new Color(255, 207, 88, 220));
        background2D.setPaint(UserIconPaint);
        background2D.fillOval(backgroundWidth / 11 - 7, backgroundHeight / 7 - 7,
                userImageWidth + 14, userImageHeight + 14);

        //Cut the image
        background2D.clip(new Ellipse2D.Float(backgroundWidth / 11, backgroundHeight / 7,
                userImageWidth, userImageHeight));

        //Add the user image
        background2D.drawImage(userImage, backgroundWidth / 11, backgroundHeight / 7, userImageWidth, userImageHeight, null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(background, "jpg", output);

        //Display
        /*
        JFrame jFrame = new JFrame();
        jFrame.setLayout(new FlowLayout());
        jFrame.setSize(backgroundWidth,backgroundHeight);
        jFrame.setLocationRelativeTo(null);
        ImageIcon icon = new ImageIcon(background);
        JLabel label = new JLabel();
        label.setIcon(icon);
        jFrame.add(label);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);*/

        return output.toByteArray();
    }

}
