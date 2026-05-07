package org.example.detection;

import org.example.idslog.IdsEventLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DetectionResultTest {

    @Test
    void constructor_ShouldSetAllFields() {
        // Create feature vector
        double[] features = {5.0, 0.2, 0.1, 0.6, 0.3, 0.0, 0.2, 0.1, 0.0};

        // Create result
        DetectionResult result = new DetectionResult(
                IdsEventLevel.ALERT,
                0.75,
                "Alert: endpoint scanning behaviour detected",
                features
        );

        // Check field values
        assertEquals(IdsEventLevel.ALERT, result.getLevel());
        assertEquals(0.75, result.getScore(), 0.001);
        assertEquals("Alert: endpoint scanning behaviour detected", result.getMessage());
        assertArrayEquals(features, result.getFeatures(), 0.001);
    }
}