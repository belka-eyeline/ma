package mobi.eyeline.utils.rest.filters;

import org.apache.log4j.Logger;
import org.glassfish.hk2.api.Rank;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;

@PreMatching
@Priority(Integer.MIN_VALUE)
@Rank(255)
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

  private static final Logger log = Logger.getLogger(LoggingFilter.class);

  private static final String CTX_ENTITY_STREAM = LoggingFilter.class.getName() + ".stream";
  private static final String CTX_REQUEST_ID = LoggingFilter.class.getName() + ".id";

  private static final int MAX_ENTITY_SIZE = 1 * 1024 * 1024;

  @SuppressWarnings("WeakerAccess")
  protected int getMaxEntitySize(){
    return MAX_ENTITY_SIZE;
  }

  @Override
  public void filter(ContainerRequestContext req) throws IOException {
    if (!log.isDebugEnabled()) {
      return;
    }

    final long id = System.nanoTime();
    req.setProperty(CTX_REQUEST_ID, id);

    final StringBuilder buf = new StringBuilder();

    printRequestLine(buf, "Request @ " + id, req.getMethod(), req.getUriInfo().getRequestUri());
    printHeaders(buf, req.getHeaders());

    if (req.hasEntity()) {
      final Charset charset = getCharset(req.getMediaType());
      req.setEntityStream(printEntity(buf, req.getEntityStream(), charset));
    }

    log.debug(buf.toString());
  }

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext resp) throws IOException {
    if (!log.isDebugEnabled()) {
      return;
    }

    final StringBuilder buf = new StringBuilder();

    final Object id = req.getProperty(CTX_REQUEST_ID);
    printResponseLine(buf, "Response @ " + id, resp.getStatus());
    printHeaders(buf, resp.getStringHeaders());

    if (resp.hasEntity()) {
      final LoggingStream stream = new LoggingStream(buf, resp.getEntityStream());
      resp.setEntityStream(stream);
      req.setProperty(CTX_ENTITY_STREAM, stream);

    } else {
      log.debug(buf.toString());
    }
  }

  @Override
  public void aroundWriteTo(WriterInterceptorContext ctx) throws IOException, WebApplicationException {
    if (!log.isDebugEnabled()) {
      ctx.proceed();
      return;
    }

    final LoggingStream stream = (LoggingStream)ctx.getProperty(CTX_ENTITY_STREAM);
    ctx.proceed();
    if (stream != null) {
      final Charset charset = getCharset(ctx.getMediaType());
      log.debug(stream.getStringBuilder(charset).toString());
    }
  }

  //
  //
  //

  private static Charset getCharset(MediaType m) {
    String name = m == null ? null : m.getParameters().get(MediaType.CHARSET_PARAMETER);
    return Charset.forName(name == null ? "UTF-8" : name);
  }


  private static void printRequestLine(StringBuilder buf, String title, String method, URI uri) {
    buf.append(title)
        .append('\n')
        .append(method).append(' ')
        .append(uri.toASCIIString())
        .append('\n');
  }

  private static void printResponseLine(StringBuilder buf, String title, int status) {
    buf.append(title)
        .append('\n')
        .append("Code: ")
        .append(Integer.toString(status))
        .append('\n');
  }

  private static void printHeaders(StringBuilder buf, MultivaluedMap<String, String> headers) {
    if (headers != null) {
      buf.append("Headers: ").append(headers).append('\n');
    }
  }

  private InputStream printEntity(StringBuilder buf, InputStream stream, Charset charset) throws IOException{

    if (!stream.markSupported()) {
      stream = new BufferedInputStream(stream);
    }

    stream.mark(getMaxEntitySize() + 1);

    final byte[] entity = new byte[getMaxEntitySize() + 1];
    final int entitySize = stream.read(entity);
    buf.append(new String(entity, 0, Math.min(entitySize, getMaxEntitySize()), charset));
    if (entitySize > getMaxEntitySize()) {
      buf.append("...more...");
    }
    buf.append('\n');
    stream.reset();
    return stream;
  }

  //
  //
  //

  private class LoggingStream extends FilterOutputStream {

    private final StringBuilder buf;
    private final ByteArrayOutputStream bufStream = new ByteArrayOutputStream();

    LoggingStream(StringBuilder buf, OutputStream inner) {
      super(inner);
      this.buf = buf;
    }

    StringBuilder getStringBuilder(Charset charset) {
      final byte[] entity = bufStream.toByteArray();

      buf.append(new String(entity, 0, Math.min(entity.length, getMaxEntitySize()), charset));
      if (entity.length > getMaxEntitySize()) {
        buf.append("...more...");
      }
      buf.append('\n');
      return buf;
    }

    @Override
    public void write(int i) throws IOException {
      if (bufStream.size() <= getMaxEntitySize()) {
        bufStream.write(i);
      }
      out.write(i);
    }
  }
}
