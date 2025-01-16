package org.example.web;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * The main application class for the Steganography Web App.
 * This class initializes the Jakarta EE application and defines the base path for REST endpoints.
 */
@ApplicationPath("/api")
public class SteganographyApplication extends Application {
    // No additional configuration is required here.
    // The @ApplicationPath annotation defines the base URI for all REST resources.
}