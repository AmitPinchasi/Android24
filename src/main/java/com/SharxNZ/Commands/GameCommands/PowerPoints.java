package com.SharxNZ.Commands.GameCommands;


import com.SharxNZ.Android24;
import com.SharxNZ.Game.Being;
import com.SharxNZ.Utilities.Point;
import com.SharxNZ.Utilities.Stat;
import com.SharxNZ.Utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class PowerPoints extends Being {

    private static final HashMap<Long, PowerPoints> ppoints = new HashMap<>();

    static {
        Utils.garbageCollector(ppoints);
    }

    protected Stat<Integer> health = new Stat<>();
    protected Stat<Integer> ki = new Stat<>();
    protected Stat<Integer> strikeAttack = new Stat<>();
    protected Stat<Integer> kiAttack = new Stat<>();
    protected Stat<Integer> defence = new Stat<>();
    protected Stat<Integer> speed = new Stat<>();
    protected List<Stat<Integer>> powerStats = List.of(health, ki, strikeAttack,
            kiAttack, defence, speed);
    private byte pointer;

    private PowerPoints(long userID) {
        super(userID);
        for (int i = 0; i < 6; i++) {
            this.powerStats.get(i).set(0);
        }
    }

    public static @NotNull PowerPoints getPowerPoints(long userID) {
        PowerPoints powerPoints;
        if (ppoints.containsKey(userID)) {
            powerPoints = ppoints.get(userID);
            powerPoints.setInUse(true);
        } else {
            powerPoints = new PowerPoints(userID);
            ppoints.put(userID, powerPoints);
        }
        return powerPoints;
    }

    public static HashMap<Long, PowerPoints> getPPoints() {
        return ppoints;
    }

    public byte[] statsImage() {
        final int r = 200;
        final int frame = (int) ((2 * r) * 1.2);
        final int offset = frame / 2 - r;
        final double square3 = 1.732d;

        double[] parameters = new double[]{
                this.getHealth(),
                this.getKi(),
                this.getStrikeAttack(),
                this.getKiAttack(),
                this.getDefence(),
                this.getSpeed()
        };

        double[] percents = new double[6];

        double max = 0;
        for (double parameter : parameters) {
            max = Math.max(max, parameter);
        }
        // If I want to do like Xenoverse
        //max = 20;
        for (int i = 0; i < percents.length; i++) {
            percents[i] = parameters[i] / max;
        }

        Point health = new Point(r, r - percents[0] * r);
        Point ki = new Point(r + (percents[1] * square3 / 2 * r), r - (percents[1] * 1 / 2 * r));
        Point strikeAttack = new Point(r + (percents[2] * square3 / 2 * r), r + (percents[2] * 1 / 2 * r));
        Point kiAttack = new Point(r, r + percents[3] * r);
        Point defence = new Point(r - (percents[4] * square3 / 2 * r), r + (percents[4] * 1 / 2 * r));
        Point speed = new Point(r - (percents[5] * square3 / 2 * r), r - (percents[5] * 1 / 2 * r));

        Point hex1 = new Point(r, 0);
        Point hex2 = new Point(r + (square3 / 2 * r), r / 2);
        Point hex3 = new Point(r + (square3 / 2 * r), r + r / 2);
        Point hex4 = new Point(r, 2 * r);
        Point hex5 = new Point(r - (square3 / 2 * r), r + r / 2);
        Point hex6 = new Point(r - (square3 / 2 * r), r / 2);

        Point.setOffset(offset,
                ki, health, strikeAttack, kiAttack, defence, speed,
                hex1, hex2, hex3, hex4, hex5, hex6);

        // Draw the BufferedImage
        BufferedImage bufferedImage = new BufferedImage(frame, frame, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = bufferedImage.createGraphics();

        // Rendering settings
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);



        /* Start drawing */
        graphics2D.setStroke(new BasicStroke(8));

        // The circle around
        graphics2D.setColor(new Color(172, 171, 171, 255));
        graphics2D.drawOval(offset, offset, 2 * r, 2 * r);
        graphics2D.setStroke(new BasicStroke(3));

        graphics2D.setColor(new Color(255, 197, 127, 10));
        graphics2D.fillPolygon(new int[]{hex1.x, hex2.x, hex3.x, hex4.x, hex5.x, hex6.x},
                new int[]{hex1.y, hex2.y, hex3.y, hex4.y, hex5.y, hex6.y}, 6);

        graphics2D.setColor(new Color(23, 162, 215, 255));
        graphics2D.fillPolygon(new int[]{health.x, ki.x, strikeAttack.x, kiAttack.x, defence.x, speed.x},
                new int[]{health.y, ki.y, strikeAttack.y, kiAttack.y, defence.y, speed.y}, 6);

        graphics2D.setColor(new Color(2, 217, 251, 255));
        graphics2D.drawPolygon(new int[]{health.x, ki.x, strikeAttack.x, kiAttack.x, defence.x, speed.x},
                new int[]{health.y, ki.y, strikeAttack.y, kiAttack.y, defence.y, speed.y}, 6);

        graphics2D.setColor(new Color(255, 206, 127, 92));
        graphics2D.drawLine(hex1.x, hex1.y, hex4.x, hex4.y);
        graphics2D.drawLine(hex2.x, hex2.y, hex5.x, hex5.y);
        graphics2D.drawLine(hex3.x, hex3.y, hex6.x, hex6.y);

        graphics2D.setPaint(new GradientPaint(offset, offset, new Color(0x2ED4EAEA, true),
                2 * r + offset, 2 * r + offset, new Color(50, true)));
        graphics2D.fillOval(offset, offset, 2 * r, 2 * r);


        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", output);
        } catch (IOException e) {
            Android24.logError(e);
        }
        return output.toByteArray();
    }

    public @NotNull MessageEmbed getPowerPointsEmbed() {

        char[] charPointer = new char[6];
        charPointer[this.getPointer()] = '↘';

        String[] changeHighlight = new String[6];
        for (int i = 0; i < changeHighlight.length; i++) {
            if (this.powerStats.get(i).get() > 0) {
                changeHighlight[i] = "```yaml\n";
            } else {
                changeHighlight[i] = "```\n";
            }
        }

        String[] strings = new String[]{
                changeHighlight[0] + "Health: " + this.getHealth() + "```",
                changeHighlight[1] + "Ki: " + this.getKi() + "```",
                changeHighlight[2] + "Strike Attack: " + this.getStrikeAttack() + "```",
                changeHighlight[3] + "Ki Attack: " + this.getKiAttack() + "```",
                changeHighlight[4] + "Defence: " + this.getDefence() + "```",
                changeHighlight[5] + "Speed: " + this.getSpeed() + "```",
        };


        EmbedBuilder ppEmbed = new EmbedBuilder();
        ppEmbed.setTitle("Your power points:");
        ppEmbed.setDescription("here you can edit your power points");
        ppEmbed.addBlankField(true);
        ppEmbed.addField("Available Power Points: " + this.getPowerPoints(), "", false);
        ppEmbed.addField(String.valueOf(charPointer[0]), strings[0], true);
        ppEmbed.addField(String.valueOf(charPointer[1]), strings[1], true);
        ppEmbed.addField(String.valueOf(charPointer[2]), strings[2], true);
        ppEmbed.addField(String.valueOf(charPointer[3]), strings[3], true);
        ppEmbed.addField(String.valueOf(charPointer[4]), strings[4], true);
        ppEmbed.addField(String.valueOf(charPointer[5]), strings[5], true);
        try {
            User user = Android24.jda.retrieveUserById(this.getUserID()).submit().get();
            ppEmbed.setFooter("The stats of: " + user.getName(), user.getAvatarUrl());

        } catch (Exception e) {
            Android24.logError(e);
        }
        ppEmbed.setImage("attachment://png.png");
        return ppEmbed.build();
    }

    public @NotNull MessageEmbed getWarningEmbed() {

        EmbedBuilder ppEmbed = new EmbedBuilder();
        ppEmbed.setColor(Color.red);
        ppEmbed.setTitle("Your power points:");
        ppEmbed.setDescription("here you can edit your power points");
        ppEmbed.addField("", """
                ```diff
                -You sure you want to save?
                -You will not be able to change the stats after that.
                -If you wish to cancel you can just ignore this message,
                -or call the function again

                (Press save again to save)```""", false);
        ppEmbed.addBlankField(true);
        ppEmbed.addField("Available Power Points: " + this.getPowerPoints(), "", false);
        ppEmbed.addField("", "```Health: " + this.getHealth() + "```", true);
        ppEmbed.addField("", "```Ki: " + this.getKi() + "```", true);
        ppEmbed.addField("", "```Strike Attack: " + this.getStrikeAttack() + "```", true);
        ppEmbed.addField("", "```Ki Attack: " + this.getKiAttack() + "```", true);
        ppEmbed.addField("", "```Defence: " + this.getDefence() + "```", true);
        ppEmbed.addField("", "```Speed: " + this.getSpeed() + "```", true);
        try {
            User user = Android24.jda.retrieveUserById(this.getUserID()).submit().get();
            ppEmbed.setFooter("The stats of: " + user.getName(), user.getAvatarUrl());
        } catch (Exception e) {
            Android24.logError(e);
        }

        ppEmbed.setImage("attachment://png.png");
        return ppEmbed.build();
    }

    public void nextValue() {
        pointer++;
        if (pointer > 5) {
            pointer = 0;
        }
    }

    public void previousValue() {
        pointer--;
        if (pointer < 0) {
            pointer = 5;
        }
    }

    public void addValue() {
        if (powerPoints > 0) {
            powerPoints--;
            powerStats.get(pointer).set(powerStats.get(pointer).get() + 1);
        }
    }

    public void subtractValue() {
        if (this.powerStats.get(pointer).get() > 0) {
            powerStats.get(pointer).set(powerStats.get(pointer).get() - 1);
            powerPoints++;
        }
    }

    public Being toBeing() {
        super.health = this.getHealth();
        super.ki = this.getKi();
        super.strikeAttack = this.getStrikeAttack();
        super.kiAttack = this.getKiAttack();
        super.defence = this.getDefence();
        super.speed = this.getSpeed();

        return this;
    }

    @Override
    public void save() {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement saveBeingStatement = con.prepareStatement(saveBeingStatementSql)
        ) {

            saveBeingStatement.setLong(1, getPowerPoints());
            saveBeingStatement.setLong(2, getHealth());
            saveBeingStatement.setLong(3, getKi());
            saveBeingStatement.setLong(4, getStrikeAttack());
            saveBeingStatement.setLong(5, getKiAttack());
            saveBeingStatement.setLong(6, getDefence());
            saveBeingStatement.setLong(7, getSpeed());
            saveBeingStatement.setLong(8, getUserID());

            saveBeingStatement.executeUpdate();

        } catch (SQLException throwables) {
            Android24.logError(throwables);
        }
    }

    public int getPointer() {
        return pointer;
    }

    @Override
    public long getHealth() {
        return this.health.get() + super.getHealth();
    }

    @Override
    public long getKi() {
        return this.ki.get() + super.getKi();
    }

    @Override
    public long getStrikeAttack() {
        return this.strikeAttack.get() + super.getStrikeAttack();
    }

    @Override
    public long getKiAttack() {
        return this.kiAttack.get() + super.getKiAttack();
    }

    @Override
    public long getDefence() {
        return this.defence.get() + super.getDefence();
    }

    @Override
    public long getSpeed() {
        return this.speed.get() + super.getSpeed();
    }

}
