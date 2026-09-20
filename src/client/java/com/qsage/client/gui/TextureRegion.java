package com.qsage.client.gui;

public record TextureRegion(
        int u,
        int v,
        int width,
        int height
) {

        public TextureRegion offsetU(int offset) {
                return new TextureRegion(
                        u + offset,
                        v,
                        width,
                        height
                );
        }

        public TextureRegion offsetV(int offset) {
                return new TextureRegion(
                        u,
                        v + offset,
                        width,
                        height
                );
        }
}