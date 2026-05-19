package org.example.detection;

import org.example.idslog.IdsEventLevel;

/**
 * Holds the result of classifying one scored request event.
 *
 * <p>This keeps the IDS decision together in one object: the event level, anomaly score,
 * explanation message and feature vector that was used to make the decision.
 */
public class DetectionResult {

  private final IdsEventLevel level;
  private final double score;
  private final String message;
  private final double[] features;

  public DetectionResult(IdsEventLevel level, double score, String message, double[] features) {
    this.level = level;
    this.score = score;
    this.message = message;
    this.features = features;
  }

  public IdsEventLevel getLevel() {
    return level;
  }

  public double getScore() {
    return score;
  }

  public String getMessage() {
    return message;
  }

  public double[] getFeatures() {
    return features;
  }
}
