package mobi.eyeline.utils.restclient.web;

import java.io.IOException;

public class ExceptionTest {

  public static void main(String[] args) throws IOException {

    getValue();

  }

  static String getValue() {
  try {
    return new RestClient().json("http://localhost:8000/myResource")
        .object()
        .getString("value");

  } catch (RestClientException.HttpRequestFailedException e) {
    if (e.getCode() == 404) {
      return null;    // Entity not found.
    }

    throw e;
  }

  }

}
