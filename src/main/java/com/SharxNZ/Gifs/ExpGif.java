package com.SharxNZ.Gifs;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public abstract class ExpGif {
    private static final Random rand = new Random();
    private static final String[] gifs = new String[]{
            "https://c.tenor.com/9n0weQuYRQ8AAAAd/explosion-dragon-ball.gif",
            "https://c.tenor.com/Q61YIi3DGIcAAAAd/dragon-ball-super-ki.gif",
            "https://c.tenor.com/jm_lQlwnZeoAAAAd/explosion-dragon-ball-super.gif",
            "https://c.tenor.com/NyQ7nWkocu8AAAAC/explosion-anime.gif",
            "https://comicvine.gamespot.com/a/uploads/original/11137/111372416/6748916-frieza%20destroys%20earth%20short%20redone.gif",
            "https://i.makeagif.com/media/9-22-2015/vEb60V.gif",
            "https://c.tenor.com/nlFQyoe8rqMAAAAC/explosion-anime.gif",
            "https://i.makeagif.com/media/5-02-2018/lH70RT.gif",
            "https://animesher.com/orig/0/84/842/8428/animesher.com_gif-pink-explosion-842841.gif",
            "https://comic.systems/images/ToeiAnimation/DragonBallSuperBroly/DragonBallSuperBroly-images-8991",
            "https://qph.fs.quoracdn.net/main-qimg-cdc3042d2f74098a0a2f54840f59d452",
            "https://64.media.tumblr.com/fa8d0240b23f32f8e2e58ebbed567e7e/f87b91b4a9510de9-30/s500x750/95d937964d30b9cb9adbef32b64c9045a0dce8e5.gif",
            "https://64.media.tumblr.com/e4c912e3c36282f69c6a1fc50568741e/tumblr_pf22zoRzmD1ttia6ao5_400.gif"
    };
    @Contract(" -> new")
    public static @NotNull Gif getGif(){
        return Gif.getGif((short) 15, gifs[rand.nextInt(gifs.length)]);
    }
}
