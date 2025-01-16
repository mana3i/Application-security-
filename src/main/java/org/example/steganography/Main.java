import org.example.steganography.SteganographyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            // Load an image
            String inputImagePath = "C:\\Users\\chadi\\Pictures\\chedi.jpg";
            logger.info("Loading image from: " + inputImagePath);
            BufferedImage image = ImageIO.read(new File(inputImagePath));

            if (image == null) {
                logger.error("Failed to load the image. Please check the file path and format.");
                return;
            }

            // Create an instance of SteganographyService
            SteganographyService service = new SteganographyService();

            // Encode a message
            String message = "chedi is a hacker";
            logger.info("Encoding message: " + message);
            BufferedImage encodedImage = service.encodeMessage(image, message);

            // Save the encoded image
            String outputImagePath = "encoded.png";
            logger.info("Saving encoded image to: " + outputImagePath);
            ImageIO.write(encodedImage, "png", new File(outputImagePath));

            // Decode the message
            logger.info("Decoding message from the encoded image...");
            String decodedMessage = service.decodeMessage(encodedImage);
            logger.info("Decoded Message: " + decodedMessage);

            // Verify the decoded message matches the original
            if (message.equals(decodedMessage)) {
                logger.info("Success! The decoded message matches the original.");
            } else {
                logger.error("Error: The decoded message does not match the original.");
            }
        } catch (IOException e) {
            logger.error("An error occurred: ", e);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: ", e);
        }
    }
}