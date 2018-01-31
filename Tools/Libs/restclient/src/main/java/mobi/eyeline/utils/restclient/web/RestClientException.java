package mobi.eyeline.utils.restclient.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class RestClientException extends RuntimeException {

  private RestClientException(Throwable cause) { super(cause); }

  RestClientException(String message, Throwable cause)  { super(message, cause); }


  /** Connection to the specified URL failed. */
  public static class ConnectionFailedException extends RestClientException {
    private final URL url;

    public ConnectionFailedException(URL url, Exception cause) {
      super("Connection to [" + url + "] failed", cause);
      this.url = url;
    }

    public URL getUrl() {
      return url;
    }
  }


  /** Connection succeeded, but further request processing failed due to HTTP failure. */
  public static class HttpRequestFailedException extends RestClientException {

    private final int code;
    private final String response;

    private final String method;
    private final URL url;

    static HttpRequestFailedException wrap(IOException cause, HttpURLConnection conn) {
      try {
        return new HttpRequestFailedException(
            conn.getResponseCode(),
            Resource.toString(conn.getErrorStream(), "UTF-8"),
            conn.getRequestMethod(),
            conn.getURL(),
            cause);

      } catch (IOException e) {
        throw new RestClientException(cause);
      }
    }

    HttpRequestFailedException(int code,
                               String response,
                               String method,
                               URL url,
                               Exception cause) {
      //noinspection StringBufferReplaceableByString
      super(
          new StringBuilder()
              .append("Request")
              .append(method == null || url == null ? " " : " [" + method + " " + url + "] ")
              .append("failed with HTTP code [").append(code).append("]")
              .append(response == null ? "" : ": [" + response + "]")
              .toString(),
          cause
      );

      this.code = code;
      this.response = response;
      this.method = method;
      this.url = url;
    }

    public int getCode()        { return code; }
    public String getResponse() { return response; }
    public String getMethod()   { return method; }
    public URL getUrl()         { return url; }
  }


  public static class ResponseProcessingException extends RestClientException {
    private final String content;

    ResponseProcessingException(String content, Exception cause) {
      super("Unable to process response content [" + content + "]", cause);
      this.content = content;
    }

    public String getContent() {
      return content;
    }
  }

}
