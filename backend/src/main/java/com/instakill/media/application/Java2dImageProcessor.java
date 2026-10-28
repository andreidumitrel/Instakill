package com.instakill.media.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Scales images down to a maximum width (keeping aspect ratio, never upscaling) and stamps a
 * watermark in the bottom-right corner. Uses only the JDK's ImageIO/Java2D.
 * Animated GIFs are reduced to their first frame.
 */
@Component
public class Java2dImageProcessor implements ImageProcessor {

    private static final Set<String> SUPPORTED = Set.of("jpg", "jpeg", "png", "gif");
    private static final int MARGIN = 6;

    private final int maxWidth;
    private final String watermark;

    public Java2dImageProcessor(@Value("${media.image.max-width:300}") int maxWidth,
                                @Value("${media.image.watermark:Instakill Demo}") String watermark) {
        this.maxWidth = maxWidth;
        this.watermark = watermark;
    }

    @Override
    public boolean supports(String extension) {
        return extension != null && SUPPORTED.contains(extension.toLowerCase());
    }

    @Override
    public byte[] process(byte[] original, String extension) throws IOException {
        System.setProperty("java.awt.headless", "true");
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(original));
        if (source == null) {
            throw new InvalidImageException("File is not a valid image");
        }

        boolean jpeg = extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg");
        BufferedImage result = scale(source, jpeg);
        drawWatermark(result);

        String format = jpeg ? "jpg" : extension.toLowerCase();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!ImageIO.write(result, format, out)) {
            throw new IOException("No image writer available for format " + format);
        }
        return out.toByteArray();
    }

    private BufferedImage scale(BufferedImage source, boolean opaque) {
        int width = Math.min(source.getWidth(), maxWidth);
        int height = Math.max(1, Math.round((float) source.getHeight() * width / source.getWidth()));
        BufferedImage target = new BufferedImage(width, height,
                opaque ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = target.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            if (opaque) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, width, height);
            }
            g.drawImage(source, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return target;
    }

    private void drawWatermark(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int fontSize = Math.max(10, image.getWidth() / 20);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
            FontMetrics fm = g.getFontMetrics();
            int x = Math.max(0, image.getWidth() - fm.stringWidth(watermark) - MARGIN);
            int y = Math.max(fm.getAscent(), image.getHeight() - fm.getDescent() - MARGIN);

            g.setComposite(AlphaComposite.SrcOver);
            g.setColor(new Color(0, 0, 0, 140));
            g.drawString(watermark, x + 1, y + 1);
            g.setColor(new Color(255, 255, 255, 220));
            g.drawString(watermark, x, y);
        } finally {
            g.dispose();
        }
    }
}
