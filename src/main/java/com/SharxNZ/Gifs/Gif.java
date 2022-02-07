package com.SharxNZ.Gifs;

import org.jetbrains.annotations.NotNull;

public interface Gif {

    static @NotNull Gif getGif(short length, String link) {
        return new Gif() {
            @Override
            public short getLength() {
                return length;
            }

            @Override
            public String getLink() {
                return link;
            }
        };
    }

    short getLength();

    String getLink();

}
