package com.SharxNZ.Gifs;

import com.SharxNZ.Android24;
import com.SharxNZ.Utilities.Embeds;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.gif.GifControlDirectory;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class AGif implements Gif {

    protected int id;
    protected String race;
    protected short length;
    protected String link;

    public static short getGifAnimatedTimeLengthFromUrl(String link) throws IOException, ImageProcessingException {
        try ( //This try is just for closing the resources
              InputStream in1 = new BufferedInputStream(new URL(link).openStream());
        ) {
            Metadata metadata;
            InputStream in2 = null;
            try {
                metadata = ImageMetadataReader.readMetadata(in1); //Trying to take the gif
            } catch (ImageProcessingException exception) { // Getting here if the file is an HTML
                Document doc = Jsoup.connect(link).get(); // It is faster to download the site, but this is cleaner.
                Elements elm = doc.select("img[src$=.gif]");
                if (!elm.isEmpty()) {
                    link = elm.get(0).attr("src"); // Taking the gif from the HTML
                    in2 = new BufferedInputStream(new URL(link).openStream());
                    metadata = ImageMetadataReader.readMetadata(in2);
                } else {
                    System.out.println("====================");
                    System.out.println(link);
                    System.out.println("====================");
                    System.out.println(doc);
                    throw new ImageProcessingException("The doc not supported");
                }
            } finally {
                if (in2 != null)
                    in2.close();
            }

            List<GifControlDirectory> gifControlDirectories =
                    (List<GifControlDirectory>) metadata.getDirectoriesOfType(GifControlDirectory.class);

            int timeLength = 0;
            if (gifControlDirectories.size() == 1) { // Do not read delay of static GIF files with single frame.
                throw new ImageProcessingException("This gif have only one frame");
            } else if (gifControlDirectories.size() > 1) {
                for (GifControlDirectory gifControlDirectory : gifControlDirectories) {
                    try {
                        if (gifControlDirectory.hasTagName(GifControlDirectory.TAG_DELAY)) {
                            timeLength += gifControlDirectory.getInt(GifControlDirectory.TAG_DELAY);
                        }
                    } catch (MetadataException e) {
                        Android24.logError(e);
                    }
                }
                // Unit of time is 10 milliseconds in GIF.
                //timeLength *= 10;
            }
            if (timeLength < 50)
                return 1;
            else
                return (short) Math.round(timeLength / 100.0);
        }
    }

    /**
     * Get time length of GIF file in milliseconds.
     *
     * @return time length of gif in ms.
     */
    protected void setGifAnimatedTimeLengthFromUrl(String link) throws IOException, ImageProcessingException {
        try ( //This try is just for closing the resources
              InputStream in1 = new BufferedInputStream(new URL(link).openStream());
        ) {
            Metadata metadata;
            InputStream in2 = null;
            try {
                metadata = ImageMetadataReader.readMetadata(in1); //Trying to take the gif
            } catch (ImageProcessingException exception) { // Getting here if the file is an HTML
                Document doc = Jsoup.connect(link).get(); // It is faster to download the site, but this is cleaner.
                Elements elm = doc.select("img[src$=.gif]");
                if (!elm.isEmpty()) {
                    link = elm.get(0).attr("src"); // Taking the gif from the HTML
                    in2 = new BufferedInputStream(new URL(link).openStream());
                    metadata = ImageMetadataReader.readMetadata(in2);
                } else {
                    System.out.println("====================");
                    System.out.println(link);
                    System.out.println("====================");
                    System.out.println(doc);
                    throw new ImageProcessingException("The doc not supported");
                }
            } finally {
                if (in2 != null)
                    in2.close();
            }

            List<GifControlDirectory> gifControlDirectories =
                    (List<GifControlDirectory>) metadata.getDirectoriesOfType(GifControlDirectory.class);

            int timeLength = 0;
            if (gifControlDirectories.size() == 1) { // Do not read delay of static GIF files with single frame.
                throw new ImageProcessingException("This gif have only one frame");
            } else if (gifControlDirectories.size() > 1) {
                for (GifControlDirectory gifControlDirectory : gifControlDirectories) {
                    try {
                        if (gifControlDirectory.hasTagName(GifControlDirectory.TAG_DELAY)) {
                            timeLength += gifControlDirectory.getInt(GifControlDirectory.TAG_DELAY);
                        }
                    } catch (MetadataException e) {
                        Android24.logError(e);
                    }
                }
                // Unit of time is 10 milliseconds in GIF.
                //timeLength *= 10;
            }
            this.link = link;
            if (timeLength < 50)
                this.length = 1;
            else
                this.length = (short) Math.round(timeLength / 100.0);
        }
    }

    protected void sendGifCheck(MessageEmbed embed, User user, String id) {
        CompletableFuture<Message> message =
                Android24.jda.getTextChannelById(890544910057480213L).sendMessageEmbeds(embed)
                        .setActionRow(Button.success("TGif#allow#" + id, "✅"), Button.danger("TGif#deny#" + id, "❌")).submit();
        Android24.eventWaiter.waitForEvent(ButtonClickEvent.class, bce -> bce.getComponentId().startsWith("TGif#")
                && bce.getComponentId().endsWith(id), bce -> {
            switch (bce.getComponentId().split("#")[1]) {
                case "allow" -> {
                    if (saveGif())
                        bce.editMessage(new MessageBuilder(Embeds.successEmbed("The gif hase been added!")).build()).queue(
                                interactionHook -> interactionHook.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
                    else
                        bce.editMessage(new MessageBuilder(Embeds.successEmbed("There was a problem in the gif!")).build()).queue(
                                interactionHook -> interactionHook.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
                }
                case "deny" -> {
                    user.openPrivateChannel().queue(channel -> {
                        channel.sendMessage("Your gif has not been approved 🙁. Talk to " + bce.getUser().getAsTag() + " to understand why...").queue(null, null);
                    });
                    bce.getMessage().delete().queue();
                }
            }
        }, 1, TimeUnit.DAYS, () -> {
            user.openPrivateChannel().queue(channel -> channel.sendMessage("Your gif has not been notice, try to send it again or to talk so a manager").queue(null, null));
            try {
                message.get().delete().queue();
            } catch (InterruptedException | ExecutionException e) {
                Android24.logError(e);
            }
        });
    }

    abstract boolean saveGif();

    public short getLength() {
        return length;
    }

    public void setLength(short length) {
        this.length = length;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
