package mobi.eyeline.utils.rest.errors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NOT_IMPLEMENTED;

public class ErrorUtils {

  public static WebApplicationException badRequest(String errorCode, String message) {
    return new BadRequestException(Response.status(BAD_REQUEST)
        .entity(new ErrorMessage(errorCode, message))
        .type(MediaType.APPLICATION_JSON)
        .build());
  }

  public static WebApplicationException notImplemented(String errorCode, String message) {
    return new BadRequestException(Response.status(NOT_IMPLEMENTED)
        .entity(new ErrorMessage(errorCode, message))
        .type(MediaType.APPLICATION_JSON)
        .build());
  }

  public static WebApplicationException notFound(String errorCode, String message) {
    return new NotFoundException(Response.status(NOT_FOUND)
        .entity(new ErrorMessage(errorCode, message))
        .type(MediaType.APPLICATION_JSON)
        .build());
  }
}
