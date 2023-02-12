package uk.co.stevebosman.bucketlambda.testhelpers;

import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3TestHelper {
  public static S3Client getS3Client(final LocalStackContainer localStack) {
    return S3Client.builder()
                   .endpointOverride(localStack.getEndpointOverride(LocalStackContainer.Service.S3))
                   .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                           localStack.getAccessKey(), localStack.getSecretKey()
                   )))
                   .region(Region.of(localStack.getRegion()))
                   .build();
  }
}
