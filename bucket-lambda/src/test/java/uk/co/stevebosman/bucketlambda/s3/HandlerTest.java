package uk.co.stevebosman.bucketlambda.s3;

import com.amazonaws.services.lambda.runtime.Context;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import uk.co.stevebosman.bucketlambda.Handler;
import uk.co.stevebosman.bucketlambda.HandlerResult;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static uk.co.stevebosman.bucketlambda.testhelpers.S3TestHelper.getS3Client;

@Slf4j
@Testcontainers
public class HandlerTest {
  @Container
  static final LocalStackContainer localStack
          = new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.3.1"))
          .withServices(S3);

  @Test
  void badCommand() {
    final Handler instance = new Handler(getS3Client(localStack));
    final Context mockContext = mock(Context.class);

    // When
    final String command = "unknown";
    final HandlerResult result = instance.handleRequest(Map.of("command", command), mockContext);

    // Then
    assertEquals("error", result.getStatus(), "Expected error");
    assertEquals("Unrecognised command: " + command, result.getMessage(), "Unexpected message");
  }

  @Test
  void handlerHello() {
    final Handler instance = new Handler(getS3Client(localStack));
    final Context mockContext = mock(Context.class);

    // When
    final HandlerResult result = instance.handleRequest(Map.of("command", "hello"), mockContext);

    // Then
    assertEquals("success", result.getStatus(), "Expected success");
    assertEquals("Hello World", result.getMessage(), "Unexpected message");
  }

  @Test
  void handlerRead() {
    // Given
    final S3Client s3Client = getS3Client(localStack);
    final Handler instance = new Handler(s3Client);
    final Context mockContext = mock(Context.class);

    final String bucketName = "my-bucket";
    s3Client.createBucket(CreateBucketRequest.builder()
                                             .bucket(bucketName)
                                             .build());

    final String keyName = "my-key";
    final PutObjectRequest build = PutObjectRequest.builder()
                                                   .bucket(bucketName)
                                                   .key(keyName)
                                                   .build();

    final String propertyName = "my-property";
    final String propertyValue = "Hello";
    final RequestBody requestBody = RequestBody.fromBytes(
            ("{\"" + propertyName + "\":\"" + propertyValue + "\"}").getBytes(StandardCharsets.UTF_8));
    final PutObjectResponse putObjectResponse = s3Client.putObject(build, requestBody);
    assertNotNull(putObjectResponse.eTag(), "Object wasn't properly saved");

    // When
    final HandlerResult result = instance.handleRequest(
            Map.of("command", "read",
                   "bucket", bucketName,
                   "object-key", keyName,
                   "property", propertyName),
            mockContext);

    // Then
    assertEquals("success", result.getStatus(), "Expected success");
    assertEquals(propertyValue, result.getMessage(), "Unexpected message");
  }

  @Test
  void handlerReadBadKey() {
    // Given
    final S3Client s3Client = getS3Client(localStack);
    final Handler instance = new Handler(s3Client);
    final Context mockContext = mock(Context.class);

    final String bucketName = "my-bucket";
    s3Client.createBucket(CreateBucketRequest.builder()
                                             .bucket(bucketName)
                                             .build());

    // When
    final HandlerResult result = instance.handleRequest(
            Map.of("command", "read",
                   "bucket", bucketName,
                   "object-key", "keyName",
                   "property", "propertyName"),
            mockContext);

    // Then
    assertEquals("error", result.getStatus(), "Expected success");
    assertNotNull(result.getMessage(), "expected a message");
  }
}
