package uk.co.stevebosman.bucketlambda;

import software.amazon.awssdk.services.s3.S3Client;

/**
 * The module containing all dependencies required by the {@link Handler}.
 */
public class DependencyFactory {

  private DependencyFactory() {
  }

  /**
   * @return an instance of S3Client
   */
  public static S3Client s3Client() {
    return S3Client.builder()
                   .build();
  }
}
