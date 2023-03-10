package uk.co.stevebosman.bucketlambda.s3;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import uk.co.stevebosman.bucketlambda.testhelpers.S3TestHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Slf4j
@Testcontainers
class S3HelperTest {
  @Container
  static final LocalStackContainer localStack
          = new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.3.1"))
          .withServices(S3);

  @Test
  void readTextObjectCanReadSingleLineFile() throws IOException {
    final S3Client s3 = S3TestHelper.getS3Client(localStack);
    final String bucketName = "bucket";
    final String objectKey = "key";
    s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    final Path testFile = Path.of("test-objects", "single-line.txt");
    s3.putObject(PutObjectRequest.builder().bucket(bucketName).key(objectKey).build(),
                 testFile);

    // When
    final String result = S3Helper.readTextObject(s3, bucketName, objectKey);

    // Then
    assertEquals(Files.readString(testFile), result);
  }

}