package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnomalyDetectionServiceTest {

    private AnomalyDetectionService anomalyDetectionService;

    @BeforeEach
    void setUp() {
        // Create service
        anomalyDetectionService = new AnomalyDetectionService();
    }

    @Test
    void startCollecting_ShouldEnableCollectionAndResetTrainingState() {
        // Start collection mode
        anomalyDetectionService.startCollecting();

        // Check state
        assertTrue(anomalyDetectionService.isCollecting());
        assertFalse(anomalyDetectionService.isTrained());
    }

    @Test
    void collect_WhenNotCollecting_ShouldIgnoreFeatures() {
        // Try collecting before collection mode
        anomalyDetectionService.collect(validFeatureVector());

        // Try training with no collected data
        anomalyDetectionService.trainOnCollected();

        // Check model was not trained
        assertFalse(anomalyDetectionService.isTrained());
        assertFalse(anomalyDetectionService.isCollecting());
    }

    @Test
    void collect_WhenCollecting_ShouldTrainOnCollectedData() {
        // Start collection mode
        anomalyDetectionService.startCollecting();

        // Collect training samples
        for (double[] sample : trainingData()) {
            anomalyDetectionService.collect(sample);
        }

        // Train model
        anomalyDetectionService.trainOnCollected();

        // Check state
        assertTrue(anomalyDetectionService.isTrained());
        assertFalse(anomalyDetectionService.isCollecting());
    }

    @Test
    void train_WithValidData_ShouldTrainModel() {
        // Train model directly
        anomalyDetectionService.train(trainingData());

        // Check state
        assertTrue(anomalyDetectionService.isTrained());
        assertFalse(anomalyDetectionService.isCollecting());
    }

    @Test
    void score_WhenModelIsTrained_ShouldReturnFiniteScore() {
        // Train model
        anomalyDetectionService.train(trainingData());

        // Score new sample
        double score = anomalyDetectionService.score(validFeatureVector());

        // Check score is usable
        assertTrue(Double.isFinite(score));
    }

    @Test
    void score_WhenModelIsNotTrained_ShouldThrowException() {
        // Score before training
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> anomalyDetectionService.score(validFeatureVector())
        );

        // Check error message
        assertEquals("Model not yet trained", exception.getMessage());
    }

    @Test
    void train_WithEmptyData_ShouldThrowException() {
        // Train with empty data
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> anomalyDetectionService.train(new double[0][])
        );

        // Check error message
        assertEquals("Training data must not be empty", exception.getMessage());
    }

    @Test
    void train_WithInconsistentFeatureLengths_ShouldThrowException() {
        // Create invalid training data
        double[][] data = {
                {1.0, 0.0, 0.0},
                {1.0, 0.0}
        };

        // Train with inconsistent rows
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> anomalyDetectionService.train(data)
        );

        // Check error message
        assertEquals("Inconsistent feature vector lengths in training data", exception.getMessage());
    }

    @Test
    void collect_WithInconsistentFeatureLength_ShouldThrowException() {
        // Start collection mode
        anomalyDetectionService.startCollecting();

        // Collect first valid shape
        anomalyDetectionService.collect(validFeatureVector());

        // Collect wrong shape
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> anomalyDetectionService.collect(new double[]{1.0, 0.5})
        );

        // Check error message
        assertTrue(exception.getMessage().contains("Unexpected feature vector length"));
    }

    @Test
    void collect_WithEmptyFeatureVector_ShouldThrowException() {
        // Start collection mode
        anomalyDetectionService.startCollecting();

        // Collect empty vector
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> anomalyDetectionService.collect(new double[0])
        );

        // Check error message
        assertEquals("Feature vector must not be empty", exception.getMessage());
    }

    private double[] validFeatureVector() {
        // Create one valid feature vector
        return new double[]{4.0, 0.25, 0.25, 1.0, 0.25, 0.25, 0.25, 0.25, 1.0};
    }

    private double[][] trainingData() {
        // Create simple training data
        return new double[][]{
                {1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {2.0, 0.0, 0.0, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0},
                {3.0, 0.33, 0.0, 0.66, 0.0, 0.0, 0.0, 0.33, 0.0},
                {4.0, 0.25, 0.0, 0.75, 0.0, 0.0, 0.0, 0.25, 0.0},
                {5.0, 0.20, 0.0, 0.60, 0.0, 0.0, 0.0, 0.20, 0.0},
                {6.0, 0.16, 0.0, 0.50, 0.0, 0.0, 0.0, 0.16, 0.0},
                {7.0, 0.14, 0.0, 0.42, 0.0, 0.0, 0.0, 0.14, 0.0},
                {8.0, 0.12, 0.0, 0.37, 0.0, 0.0, 0.0, 0.12, 0.0},
                {9.0, 0.11, 0.0, 0.33, 0.0, 0.0, 0.0, 0.11, 0.0},
                {10.0, 0.10, 0.0, 0.30, 0.0, 0.0, 0.0, 0.10, 0.0}
        };
    }
}