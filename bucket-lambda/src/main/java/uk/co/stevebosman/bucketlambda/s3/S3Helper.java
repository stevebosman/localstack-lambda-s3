package uk.co.stevebosman.bucketlambda.s3;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Interact with S3.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class S3Helper {
  /**
   * Retrieve text object from bucket.
   *
   * @param s3Client The S3 Client.
   * @param bucket   bucket name.
   * @param key      object key.
   * @return Text
   * @throws IOException failed to process object.
   */
  public static String readTextObject(
          final S3Client s3Client, final String bucket, final String key
  ) throws IOException {
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
