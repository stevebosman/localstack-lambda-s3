package uk.co.stevebosman.bucketlambda;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Handler response object.
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class HandlerResult {
  private final String timestamp = LocalDateTime.now().toString();
  private final String status;
  private final String message;
}
