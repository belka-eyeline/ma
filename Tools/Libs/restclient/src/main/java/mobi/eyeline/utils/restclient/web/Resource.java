package mobi.eyeline.utils.restclient.web;

import mobi.eyeline.utils.restclient.web.RestClientException.ConnectionFailedException;
import mobi.eyeline.utils.restclient.web.RestClientException.HttpRequestFailedException;
import mobi.eyeline.utils.restclient.web.RestClientException.ResponseProcessingException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;

abstract class Resource extends RestClient {

  @SuppressWarnings("WeakerAccess") protected HttpURLConnection conn;
  @SuppressWarnings("WeakerAccess") protected InputStream inputStream;

  private Resource(Option... options) { super(options); }

  protected abstract String getAcceptedTypes();

  void fill(HttpURLConnection conn) {
    this.conn = conn;

    try {
      inputStream = "gzip".equals(conn.getContentEncoding()) ?
          new GZIPInputStream(conn.getInputStream()) : conn.getInputStream();

    } catch (ConnectException | UnknownHostException e) {
      throw new ConnectionFailedException(conn.getURL(), e);

    } catch (IOException e) {
      throw HttpRequestFailedException.wrap(e, conn);
    }
  }

  @SuppressWarnings("unused")
  public String printResponseHeaders() {
    final StringBuilder buf = new StringBuilder();

    if (conn != null) {
      for (String key : conn.getHeaderFields().keySet()) {
        for (String val : conn.getHeaderFields().get(key)) {
          buf.append(key).append(": ").append(val).append("\n");
        }
      }
    }
    return buf.toString();
  }


  //
  //
  //

  public static class JSONResource extends Resource {
    private String content;
    private Object json;

    JSONResource(Option... options) {
      super(options);
    }

    @SuppressWarnings("unused")
    public JSONArray array() {
      try {
        ensureParsed();
        return (JSONArray) json;
      }
      catch (RestClientException e) { throw e; }
      catch (Exception e)           { throw new ResponseProcessingException(content, e); }
    }

    @SuppressWarnings("unused")
    public JSONObject object() {
      try {
        ensureParsed();
        return (JSONObject) json;
      }
      catch (RestClientException e) { throw e; }
      catch (Exception e)           { throw new ResponseProcessingException(content, e); }
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public String text() {
      ensureRead();
      return content;
    }

    private void ensureRead() {
      try {
        content = toString(inputStream, "UTF-8");
        inputStream.close();

      } catch (IOException e) {
        throw HttpRequestFailedException.wrap(e, conn);
      }
    }

    private void ensureParsed() {
      if (content == null)  ensureRead();
      if (json == null)     json = new JSONTokener(content).nextValue();
    }

    @Override
    public String getAcceptedTypes() {
      return "application/json";
    }

  }

  static String toString(InputStream is, String charset) throws IOException {
    final ByteArrayOutputStream rc = new ByteArrayOutputStream();
    final byte[] buf = new byte[1024];

    int length;
    while ((length = is.read(buf)) != -1) {
      rc.write(buf, 0, length);
    }

    return rc.toString(charset);
  }
}
