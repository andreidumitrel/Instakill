package com.instakill.media.application;

import java.io.IOException;

/**
 * Processes raw uploaded image bytes (resize + watermark) into the bytes that get stored.
 * Self-contained so it can later be moved behind a message queue / background worker.
 */
public interface ImageProcessor {

    /** Extensions (lowercase, no dot) this processor can handle. */
    boolean supports(String extension);

    /**
     * @throws InvalidImageException if the bytes are not a decodable image
     */
    byte[] process(byte[] original, String extension) throws IOException;
}
