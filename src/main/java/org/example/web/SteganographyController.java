package org.example.web;

import org.example.steganography.SteganographyService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;

@Path("/steganography")
public class SteganographyController {

    private static final Logger logger = LogManager.getLogger(SteganographyController.class);

    @Inject // Use dependency injection
    private SteganographyService steganographyService;

    // Define a temporary directory for file uploads
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/steganography-uploads/";

    static {
        // Ensure the temporary directory exists
        new File(TEMP_DIR).mkdirs();
        logger.info("Temporary upload directory: " + TEMP_DIR);
    }

    // Test endpoint to verify the application is working
    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        logger.info("Test endpoint accessed.");
        return "Application is working!";
    }

    @POST
    @Path("/encode")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/png")
    public Response encode(
            @FormDataParam("image") InputStream imageStream,
            @FormDataParam("message") String message) {
        try {
            // Validate inputs
            if (imageStream == null) {
                logger.error("No image file provided.");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("No image file provided").build();
            }
            if (message == null || message.trim().isEmpty()) {
                logger.error("No message provided.");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("No message provided").build();
            }

            // Save the uploaded image to a temporary file
            String tempFileName = TEMP_DIR + "uploaded-image-" + System.currentTimeMillis() + ".png";
            File tempFile = new File(tempFileName);
            Files.copy(imageStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Uploaded image saved to: " + tempFile.getAbsolutePath());

            // Read the image from the temporary file
            BufferedImage image = ImageIO.read(tempFile);
            if (image == null) {
                logger.error("Invalid image file provided.");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid image file").build();
            }

            // Encode the message into the image
            BufferedImage encodedImage = steganographyService.encodeMessage(image, message);

            // Convert the encoded image to a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(encodedImage, "png", outputStream);

            // Delete the temporary file
            tempFile.delete();
            logger.info("Temporary file deleted: " + tempFile.getAbsolutePath());

            // Return the encoded image as a response
            return Response.ok(outputStream.toByteArray()).build();
        } catch (IOException e) {
            logger.error("Error processing image: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing image: " + e.getMessage()).build();
        }
    }

    @POST
    @Path("/decode")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response decode(@FormDataParam("encodedImage") InputStream encodedImageStream) {
        try {
            // Validate input
            if (encodedImageStream == null) {
                logger.error("No encoded image file provided.");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("No encoded image file provided").build();
            }

            // Save the uploaded image to a temporary file
            String tempFileName = TEMP_DIR + "encoded-image-" + System.currentTimeMillis() + ".png";
            File tempFile = new File(tempFileName);
            Files.copy(encodedImageStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Encoded image saved to: " + tempFile.getAbsolutePath());

            // Read the encoded image from the temporary file
            BufferedImage encodedImage = ImageIO.read(tempFile);
            if (encodedImage == null) {
                logger.error("Invalid encoded image file provided.");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid encoded image file").build();
            }

            // Decode the message from the image
            String message = steganographyService.decodeMessage(encodedImage);

            // Delete the temporary file
            tempFile.delete();
            logger.info("Temporary file deleted: " + tempFile.getAbsolutePath());

            // Return the decoded message as a response
            return Response.ok(message).build();
        } catch (IOException e) {
            logger.error("Error decoding image: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error decoding image: " + e.getMessage()).build();
        }
    }
}