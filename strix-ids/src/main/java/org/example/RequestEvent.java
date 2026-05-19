package org.example;

/**
 * Represents one HTTP request event sent from the target web application to Strix.
 *
 * <p>The IDS uses these events as its raw input. Each event records where the request came from,
 * what endpoint was accessed, when it happened and what response status the target application
 * returned.
 */
public class RequestEvent {

  private String ip;
  private String method;
  private String uri;
  private long timestamp;
  private int statusCode;

  // Needed so Spring/Jackson can create objects from incoming JSON.
  public RequestEvent() {}

  public RequestEvent(String ip, String method, String uri, long timestamp, int statusCode) {
    this.ip = ip;
    this.method = method;
    this.uri = uri;
    this.timestamp = timestamp;
    this.statusCode = statusCode;
  }

  public String getIp() {
    return ip;
  }

  public String getMethod() {
    return method;
  }

  public String getUri() {
    return uri;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }
}
