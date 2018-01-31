package mobi.eyeline.utils.rest.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * Sets response charset in the {@literal Content-Type} header to {@literal UTF-8} unless
 * charset is already specified.
 */
public class CharsetResponseFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext resp) {
    final MediaType type = resp.getMediaType();

    if ((type != null) &&
        !type.getParameters().containsKey(MediaType.CHARSET_PARAMETER)) {
      resp.getHeaders().putSingle(
          HttpHeaders.CONTENT_TYPE,
          type.withCharset("utf-8")
      );
    }
  }

}