package mobi.eyeline.utils.mx.rest;

import com.j256.simplejmx.common.JmxAttributeField;
import com.j256.simplejmx.common.JmxOperation;
import com.j256.simplejmx.common.JmxResource;
import com.sun.net.httpserver.HttpServer;
import mobi.eyeline.utils.mx.service.JmxBeansService;
import mobi.eyeline.utils.mx.service.JmxConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * Sample application demonstrating JMX REST API.
 */
public class SampleApp {

  private static final int RMI_SERVER_PORT = 11900;
  private static final int REST_SERVER_PORT = 11901;

  public static void main(String[] args) throws URISyntaxException {

    final JmxBeansService beanService = new JmxBeansService(
        new JmxConfig() {
          @Override public boolean isJmxEnabled() { return true; }
          @Override public int getJmxPort()       { return RMI_SERVER_PORT; }
          @Override public String getJmxHost()    { return null; }
        }
    );

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() { beanService.stop(); }
    });

    beanService.register(new SampleBean());

    System.out.println("Bean service initialized.");

    final HttpServer server = JdkHttpServerFactory.createHttpServer(
        new URI("http://localhost:" + REST_SERVER_PORT + "/"),
        new ResourceConfig() {{
          register(JacksonFeature.class);
          registerResources(MxEndpoint.resources());
        }}
    );

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() { server.stop((int) TimeUnit.SECONDS.toMillis(5)); }
    });

    System.out.println("REST endpoint initialized.");
  }


  //
  //
  //

  @JmxResource(domainName = "mobi.eyeline.utils.mx.rest", beanName = "SampleBean")
  public static class SampleBean {

    @JmxAttributeField(description = "Int value", isWritable = true)
    public int intValue;

    @JmxAttributeField(description = "String array value", isWritable = true)
    public String[] stringArrayValue = new String[] {"foo", "bar"};

    @JmxOperation(
        description = "Sample operation",
        parameterNames = {"v1", "v2"},
        parameterDescriptions = {"String param", "Int param"}
    )
    public String call(String v1, int v2) {
      System.out.println("call(" + v1 + ", " + v2 + ")");
      return "ok";
    }
  }

}
