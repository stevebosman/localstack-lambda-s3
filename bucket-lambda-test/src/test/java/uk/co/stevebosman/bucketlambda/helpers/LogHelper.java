package uk.co.stevebosman.bucketlambda.helpers;

import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.testcontainers.containers.Container;

@NoArgsConstructor
public final class LogHelper {
  /**
   * Log ExecResult.
   *
   * @param description Description of result.
   * @param result      The result.
   */
  public static void logResult(final Logger log, final String description, final Container.ExecResult result) {
    if (isNotBlank(result.getStdout())) {
      log.info("{} result : {}", description, result.getStdout());
    }
    if (isNotBlank(result.getStderr())) {
      log.info("{} error : {}", description, result.getStderr());
    }
  }
  /**
   * Is text populated?
   *
   * @param text Test subject.
   * @return true if populated.
   */
  private static boolean isNotBlank(final String text) {
    return text != null && !text.isBlank();
  }
}
