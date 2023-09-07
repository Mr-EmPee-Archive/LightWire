package mr.empee.lightwire.exceptions;

/**
 * LightwireException is the base exception for all exceptions in the library
 */

public class LightwireException extends RuntimeException {

  public LightwireException(String message) {
    super(message);
  }

  public LightwireException(String message, Throwable cause) {
    super(message, cause);
  }

  public LightwireException(Throwable cause) {
    super(cause);
  }

}
