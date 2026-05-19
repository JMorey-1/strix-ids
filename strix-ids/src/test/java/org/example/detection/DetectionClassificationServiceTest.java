package org.example.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.example.idslog.IdsEventLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DetectionClassificationServiceTest {

  private DetectionClassificationService classificationService;

  @BeforeEach
  void setUp() {
    // Create service
    classificationService = new DetectionClassificationService();

    // Set test thresholds
    ReflectionTestUtils.setField(classificationService, "watchThreshold", 0.55);
    ReflectionTestUtils.setField(classificationService, "alertThreshold", 0.60);
    ReflectionTestUtils.setField(classificationService, "minEventsForAlert", 5);
  }

  @Test
  void classify_WithLowScore_ShouldReturnScoreLevel() {
    // Create normal features
    double[] features = features(5, 0.1, 0.0, 0.4, 0.0, 0.0, 0.0, 0.0, 0.0);

    DetectionResult result = classificationService.classify(0.30, features);

    // Check result
    assertEquals(IdsEventLevel.SCORE, result.getLevel());
    assertEquals(0.30, result.getScore(), 0.001);
    assertEquals("Traffic scored within expected range", result.getMessage());
  }

  @Test
  void classify_WithWatchScore_ShouldReturnWatchLevel() {
    // Create unusual features
    double[] features = features(5, 0.1, 0.0, 0.4, 0.0, 0.0, 0.0, 0.0, 0.0);

    DetectionResult result = classificationService.classify(0.56, features);

    // Check result
    assertEquals(IdsEventLevel.WATCH, result.getLevel());
    assertEquals("Watch: suspicious behaviour under observation", result.getMessage());
  }

  @Test
  void classify_WithAlertScoreAndEnoughEvents_ShouldReturnAlertLevel() {
    // Create alert features
    double[] features = features(5, 0.1, 0.0, 0.4, 0.0, 0.0, 0.0, 0.0, 0.0);

    DetectionResult result = classificationService.classify(0.65, features);

    // Check result
    assertEquals(IdsEventLevel.ALERT, result.getLevel());
    assertEquals("Alert: anomalous behaviour detected", result.getMessage());
  }

  @Test
  void classify_WithAlertScoreButTooFewEvents_ShouldReturnWatchLevel() {
    // Create small request sample
    double[] features = features(3, 0.1, 0.0, 0.4, 0.0, 0.0, 0.0, 0.0, 0.0);

    DetectionResult result = classificationService.classify(0.65, features);

    // Check result
    assertEquals(IdsEventLevel.WATCH, result.getLevel());
    assertEquals("Watch: suspicious behaviour under observation", result.getMessage());
  }

  @Test
  void classify_WithRepeatedFailedLoginPattern_ShouldReturnLoginMessage() {
    // Create brute-force style features
    double[] features = features(10, 0.9, 0.0, 0.2, 0.8, 0.0, 0.0, 0.8, 0.9);

    DetectionResult result = classificationService.classify(0.70, features);

    // Check result
    assertEquals(IdsEventLevel.ALERT, result.getLevel());
    assertEquals("Alert: repeated failed login behaviour detected", result.getMessage());
  }

  @Test
  void classify_WithAdminProbingPattern_ShouldReturnAdminMessage() {
    // Create admin probing features
    double[] features = features(10, 0.5, 0.8, 0.8, 0.4, 0.0, 0.2, 0.0, 0.0);

    DetectionResult result = classificationService.classify(0.70, features);

    // Check result
    assertEquals(IdsEventLevel.ALERT, result.getLevel());
    assertEquals("Alert: admin route probing detected", result.getMessage());
  }

  @Test
  void classify_WithEndpointScanningPattern_ShouldReturnScanningMessage() {
    // Create endpoint scanning features
    double[] features = features(10, 0.1, 0.0, 0.9, 0.1, 0.0, 0.3, 0.0, 0.0);

    DetectionResult result = classificationService.classify(0.70, features);

    // Check result
    assertEquals(IdsEventLevel.ALERT, result.getLevel());
    assertEquals("Alert: endpoint scanning behaviour detected", result.getMessage());
  }

  @Test
  void classify_WithHighUnauthorizedTraffic_ShouldReturnUnauthorizedMessage() {
    // Create unauthorized traffic features
    double[] features = features(10, 0.1, 0.0, 0.3, 0.7, 0.0, 0.0, 0.0, 0.0);

    DetectionResult result = classificationService.classify(0.70, features);

    // Check result
    assertEquals(IdsEventLevel.ALERT, result.getLevel());
    assertEquals("Alert: high unauthorized response rate detected", result.getMessage());
  }

  private double[] features(
      double requestCount,
      double postRatio,
      double adminRatio,
      double uniqueEndpointRatio,
      double unauthorizedRatio,
      double forbiddenRatio,
      double notFoundRatio,
      double loginAttemptRatio,
      double failedLoginRatio) {
    // Create feature vector
    return new double[] {
      requestCount,
      postRatio,
      adminRatio,
      uniqueEndpointRatio,
      unauthorizedRatio,
      forbiddenRatio,
      notFoundRatio,
      loginAttemptRatio,
      failedLoginRatio
    };
  }
}
