package mobi.eyeline.utils.rest.errors;

import org.apache.log4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.Family.SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.fromStatusCode;

/**
 * Processes all unhandled exceptions to:
 * <ol>
 *   <li>Ensure no internal details are returned (e.g. exception traces)</li>
 *   <li>Comply with unified error-reporting schema (see {@linkplain ErrorMessage})</li>
 * </ol>
 */
@SuppressWarnings("WeakerAccess")
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

  private final Logger log = Logger.getLogger(DefaultExceptionMapper.class);

  @Context private UriInfo uriInfo;
  @Context private Request req;

  @Override
  public Response toResponse(Exception e) {

    final int rc;
    final Object entity;

    if (e instanceof WebApplicationException) {
      final WebApplicationException wae = (WebApplicationException) e;

      rc = wae.getResponse().getStatus();
      entity = wae.getResponse().getEntity() instanceof ErrorMessage ?
          wae.getResponse().getEntity() : new ErrorMessage("INTERNAL_ERROR");

    } else {
      rc = INTERNAL_SERVER_ERROR.getStatusCode();
      entity = new ErrorMessage("INTERNAL_ERROR");
    }

    reportError(e, rc);

    return Response.status(rc)
        .entity(entity)
        .type(APPLICATION_JSON)
        .build();
  }

  private void reportError(Exception cause, int mappedStatusCode) {
    try {
      final boolean isServerError =
          fromStatusCode(mappedStatusCode).getFamily() == SERVER_ERROR;

      if (isServerError || log.isDebugEnabled()) {
        final String msg = "Error [" + mappedStatusCode + "]" +
            " during processing request [" + req.getMethod() + " " + uriInfo.getRequestUri() + "]";

        if (isServerError)  log.warn(msg, cause);
        else                log.debug(msg, cause);
      }

    } catch (Exception e) {
      // UriInfo is inaccessible?
      log.error("Fatal error during exception mapping", e);
    }
  }
}
