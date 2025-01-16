package org.example.steganography;

import jakarta.enterprise.context.ApplicationScoped; // Add this import
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Service class for encoding and decoding messages using steganography.
 * This class is now a CDI-managed bean with application scope.
 */
@ApplicationScoped // Add this annotation to make it a CDI-managed bean
public class SteganographyService {

    private static final Logger logger = LogManager.getLogger(SteganographyService.class);

    /**
     * Encodes a message into an image using LSB steganography.
     *
     * @param image   The image to encode the message into.
     * @param message The message to encode.
     * @return The image with the encoded message.
     */
    public BufferedImage encodeMessage(BufferedImage image, String message) {
        logger.info("Starting message encoding process...");

        int textLength = message.length();
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int imageIndex = 0;

        // Embed the length of the text first (32 bits)
        for (int i = 0; i < 32; i++) {
            int x = imageIndex % imageWidth;
            int y = imageIndex / imageWidth;
            int rgb = image.getRGB(x, y);
            int bit = (textLength >> (31 - i)) & 1; // Extract the bit
            rgb = (rgb & 0xFFFFFFFE) | bit; // Set the LSB
            image.setRGB(x, y, rgb);
            imageIndex++;
        }

        // Embed the text (8 bits per character)
        for (char c : message.toCharArray()) {
            for (int i = 0; i < 8; i++) {
                int x = imageIndex % imageWidth;
                int y = imageIndex / imageWidth;
                int rgb = image.getRGB(x, y);
                int bit = (c >> (7 - i)) & 1; // Extract the bit
                rgb = (rgb & 0xFFFFFFFE) | bit; // Set the LSB
                image.setRGB(x, y, rgb);
                imageIndex++;
            }
        }

        logger.info("Message successfully encoded into the image.");
        return image;
    }

    /**
     * Decodes a message from an image using LSB steganography.
     *
     * @param image The image containing the encoded message.
     * @return The decoded message.
     */
    public String decodeMessage(BufferedImage image) {
        logger.info("Starting message decoding process...");

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int imageIndex = 0;
        int textLength = 0;

        // Extract the length of the text (first 32 bits)
        for (int i = 0; i < 32; i++) {
            int x = imageIndex % imageWidth;
            int y = imageIndex / imageWidth;
            int rgb = image.getRGB(x, y);
            int bit = rgb & 1; // Extract the LSB
            textLength = (textLength << 1) | bit;
            imageIndex++;
        }

        // Extract the text (8 bits per character)
        StringBuilder extractedText = new StringBuilder();
        for (int i = 0; i < textLength; i++) {
            char c = 0;
            for (int j = 0; j < 8; j++) {
                int x = imageIndex % imageWidth;
                int y = imageIndex / imageWidth;
                int rgb = image.getRGB(x, y);
                int bit = rgb & 1; // Extract the LSB
                c = (char) ((c << 1) | bit);
                imageIndex++;
            }
            extractedText.append(c);
        }

        logger.info("Message successfully decoded from the image.");
        return extractedText.toString();
    }
}