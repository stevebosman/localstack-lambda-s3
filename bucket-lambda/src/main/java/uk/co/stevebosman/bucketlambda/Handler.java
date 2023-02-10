package uk.co.stevebosman.bucketlambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    this.s3Client = S3ClientFactory.s3Client();
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
        final String json;
        json = readJsonObject(bucket, key);
        @SuppressWarnings("unchecked") final Map<String, Object> jsonMap = OBJECT_MAPPER.readValue(json, HashMap.class);
        result.message(jsonMap.get(input.get("property")).toString());
      } else if ("hello".equals(command)) {
        result.message("Hello World");
      } else {
        result.status("error");
        result.message("Unrecognised command: " + command);
      }
    } catch (final IOException e) {
      result.status("error");
      result.message(e.getMessage());
    }
    return result.build();
  }

  /**
   * Retrieve JSON object from bucket.
   *
   * @param bucket bucket name.
   * @param key object key.
   * @return JSON Text
   * @throws IOException failed to process object.
   */
  private String readJsonObject(final String bucket, final String key) throws IOException {
    final GetObjectRequest request = GetObjectRequest.builder()
                                                     .bucket(bucket)
                                                     .key(key)
                                                     .build();
    final ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(request);
    final BufferedReader reader = new BufferedReader(new InputStreamReader(responseInputStream));
    final List<String> lines = new ArrayList<>();
    String line;
    while ((line = reader.readLine()) != null) {
      lines.add(line);
    }
    return String.join("\n", lines);
  }

}