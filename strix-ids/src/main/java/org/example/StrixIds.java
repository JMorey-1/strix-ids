package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Starts the Strix IDS service. This is the main entry point for the IDS application. */
@SpringBootApplication
public class StrixIds {

  public static void main(String[] args) {
    SpringApplication.run(StrixIds.class, args);
  }
}
