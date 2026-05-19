package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import smile.anomaly.IsolationForest;

/**
 * Handles the machine learning side of Strix.
 *
 * <p>This service collects normal traffic feature vectors, trains an Isolation Forest model and
 * then scores new traffic against that trained model. It does not decide whether something is an
 * alert by itself. It only returns the anomaly score.
 */
@Service
public class AnomalyDetectionService {

  private IsolationForest model;
  private boolean trained = false;
  private boolean collectingTrainingData = false;

  // Used to make sure all feature vectors have the same structure.
  private int expectedFeatureLength = -1;

  // Synchronized because request events can arrive from multiple HTTP requests.
  private final List<double[]> trainingData = Collections.synchronizedList(new ArrayList<>());

  public void startCollecting() {
    trainingData.clear();

    // Starting a fresh collection run should reset the old model state.
    collectingTrainingData = true;
    trained = false;
    model = null;
    expectedFeatureLength = -1;

    System.out.println("[IDS] Collecting training data...");
  }

  public void collect(double[] features) {
    if (!collectingTrainingData) {
      return;
    }

    validateFeatureLength(features);
    trainingData.add(features);
  }

  public void trainOnCollected() {
    if (trainingData.isEmpty()) {
      System.out.println("[IDS] No training data collected");
      return;
    }

    collectingTrainingData = false;

    double[][] data;

    // Copy the collected samples safely before training.
    synchronized (trainingData) {
      data = trainingData.toArray(new double[0][]);
    }

    model = IsolationForest.fit(data);
    trained = true;

    System.out.println("[IDS] Model trained on " + data.length + " real samples");
  }

  public void train(double[][] data) {
    if (data == null || data.length == 0) {
      throw new IllegalArgumentException("Training data must not be empty");
    }

    // Every row must have the same number of features.
    int featureLength = data[0].length;
    for (double[] row : data) {
      if (row.length != featureLength) {
        throw new IllegalArgumentException("Inconsistent feature vector lengths in training data");
      }
    }

    expectedFeatureLength = featureLength;
    model = IsolationForest.fit(data);
    trained = true;
    collectingTrainingData = false;

    System.out.println("[IDS] Model trained on " + data.length + " samples");
  }

  public double score(double[] features) {
    if (!trained || model == null) {
      throw new IllegalStateException("Model not yet trained");
    }

    validateFeatureLength(features);

    return model.score(features);
  }

  public boolean isTrained() {
    return trained;
  }

  public boolean isCollecting() {
    return collectingTrainingData;
  }

  private void validateFeatureLength(double[] features) {
    if (features == null || features.length == 0) {
      throw new IllegalArgumentException("Feature vector must not be empty");
    }

    // First valid vector sets the expected shape for the rest.
    if (expectedFeatureLength == -1) {
      expectedFeatureLength = features.length;
      return;
    }

    if (features.length != expectedFeatureLength) {
      throw new IllegalArgumentException(
          "Unexpected feature vector length. Expected "
              + expectedFeatureLength
              + " but got "
              + features.length);
    }
  }
}
