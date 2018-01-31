package mobi.eyeline.utils.rest.errors;

import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class ErrorMessage {

  private final String error;
  private final String errorMessage;

  public ErrorMessage(String error, String errorMessage) {
    this.error = error;
    this.errorMessage = errorMessage;
  }

  public ErrorMessage(String error) {
    this(error, null);
  }

  public String getError() {
    return error;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public static ErrorMessage error(String error) {
    return new ErrorMessage(error);
  }

  public static ErrorMessage error(String error, String errorMessage) {
    return new ErrorMessage(error, errorMessage);
  }

  @Override
  public String toString() {
    return "ErrorMessage{" +
        "error='" + error + '\'' +
        ", errorMessage='" + errorMessage + '\'' +
        '}';
  }
}
