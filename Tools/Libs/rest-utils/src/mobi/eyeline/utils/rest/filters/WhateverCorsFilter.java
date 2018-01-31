package mobi.eyeline.utils.rest.filters;

import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * Allows any CORS checks.
 */
public class WhateverCorsFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext resp) throws IOException {
    resp.getHeaders().add("Access-Control-Allow-Origin", "*");
    resp.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS, DELETE");
    resp.getHeaders().add("Access-Control-Allow-Headers", "*");

    final String reqHeaders = req.getHeaderString("Access-Control-Request-Headers");
    if (StringUtils.isNotBlank(reqHeaders)) {
      resp.getHeaders().add("Access-Control-Allow-Headers", reqHeaders);
    }
  }
}
