package com.instakill.media.application;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Java2dImageProcessorTest {

    private final Java2dImageProcessor processor = new Java2dImageProcessor(300, "Instakill Demo");

    private static byte[] image(int w, int h, String format) throws Exception {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        var g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, w, h);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, format, out);
        return out.toByteArray();
    }

    private static BufferedImage read(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    @Test
    void scalesDownToMaxWidthPreservingAspectRatio() throws Exception {
        BufferedImage result = read(processor.process(image(900, 600, "png"), "png"));
        assertThat(result.getWidth()).isEqualTo(300);
        assertThat(result.getHeight()).isEqualTo(200);
    }

    @Test
    void doesNotUpscaleSmallImages() throws Exception {
        BufferedImage result = read(processor.process(image(100, 80, "jpg"), "jpg"));
        assertThat(result.getWidth()).isEqualTo(100);
        assertThat(result.getHeight()).isEqualTo(80);
    }

    @Test
    void drawsWatermarkInBottomRightCorner() throws Exception {
        BufferedImage result = read(processor.process(image(600, 400, "png"), "png"));
        int w = result.getWidth();
        int h = result.getHeight();
        boolean cornerChanged = false;
        for (int x = w - 60; x < w; x++) {
            for (int y = h - 20; y < h; y++) {
                if ((result.getRGB(x, y) & 0xFFFFFF) != 0x0000FF) {
                    cornerChanged = true;
                }
            }
        }
        boolean topLeftUntouched = (result.getRGB(5, 5) & 0xFFFFFF) == 0x0000FF;
        assertThat(cornerChanged).isTrue();
        assertThat(topLeftUntouched).isTrue();
    }

    @Test
    void rejectsNonImageBytes() {
        assertThatThrownBy(() -> processor.process("not an image".getBytes(), "png"))
                .isInstanceOf(InvalidImageException.class);
    }

    @Test
    void supportsOnlyRasterFormats() {
        assertThat(processor.supports("PNG")).isTrue();
        assertThat(processor.supports("mp4")).isFalse();
    }
}
