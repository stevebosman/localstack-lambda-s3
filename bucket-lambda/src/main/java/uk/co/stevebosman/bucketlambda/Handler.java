package uk.co.stevebosman.bucketlambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;
import uk.co.stevebosman.bucketlambda.s3.S3ClientFactory;
import uk.co.stevebosman.bucketlambda.s3.S3Helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AWS Lambda Handler.
 */
@Slf4j
@SuppressWarnings("unused")
public class Handler implements RequestHandler<Map<String, String>, HandlerResult> {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final S3Client s3Client;

  @SuppressWarnings("unused")
  public Handler() {
    this(S3ClientFactory.s3Client());
  }

  public Handler(final S3Client s3Client) {
    this.s3Client = s3Client;
  }

  @Override
  public HandlerResult handleRequest(final Map<String, String> input, final Context context) {
    log.info("Processing: {}", input);
    final HandlerResult.HandlerResultBuilder result = HandlerResult.builder();
    result.status("success");
    try {
      final String command = input.get("command");
      if ("read".equals(command)) {
        result.message("reading");
        final String bucket = input.get("bucket");
        final String key = input.get("object-key");
        final String json = S3Helper.readTextObject(s3Client, bucket, key);
        @SuppressWarnings("unchecked") final Map<String, Object> jsonMap = OBJECT_MAPPER.readValue(json, HashMap.class);
        result.message(jsonMap.get(input.get("property")).toString());
      } else if ("hello".equals(command)) {
        result.message("Hello World");
      } else {
        result.status("error");
        result.message("Unrecognised command: " + command);
      }
    } catch (final IOException | S3Exception e) {
      result.status("error");
      result.message(e.getMessage());
    }
    return result.build();
  }

}