package mobi.eyeline.utils.restclient.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SimpleServer {

  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

    server.createContext("/", new HttpHandler() {
      @Override
      public void handle(HttpExchange t) throws IOException {
//        t.sendResponseHeaders(500, 0);
//        t.getResponseBody().close();

        final String response = "Server response";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
      }
    });
    server.setExecutor(null); // creates a default executor
    server.start();
  }

}
