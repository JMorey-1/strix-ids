package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestEventTest {

    @Test
    void constructor_ShouldSetAllFields() {
        // Create request event
        RequestEvent event = new RequestEvent(
                "10.0.0.1",
                "GET",
                "/products",
                123456789L,
                200
        );

        // Check field values
        assertEquals("10.0.0.1", event.getIp());
        assertEquals("GET", event.getMethod());
        assertEquals("/products", event.getUri());
        assertEquals(123456789L, event.getTimestamp());
        assertEquals(200, event.getStatusCode());
    }

    @Test
    void setters_ShouldUpdateAllFields() {
        // Create empty request event
        RequestEvent event = new RequestEvent();

        // Set field values
        event.setIp("192.168.1.5");
        event.setMethod("POST");
        event.setUri("/auth/login");
        event.setTimestamp(987654321L);
        event.setStatusCode(401);

        // Check updated values
        assertEquals("192.168.1.5", event.getIp());
        assertEquals("POST", event.getMethod());
        assertEquals("/auth/login", event.getUri());
        assertEquals(987654321L, event.getTimestamp());
        assertEquals(401, event.getStatusCode());
    }
}