package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts the target web application used by Strix.
 *
 * This application provides the normal, admin and authentication endpoints that
 * the traffic generator sends requests to. Its request filter forwards those
 * events to the IDS for analysis.
 */
@SpringBootApplication
public class StrixAppScratch {

    public static void main(String[] args) {
        SpringApplication.run(StrixAppScratch.class, args);
    }
}