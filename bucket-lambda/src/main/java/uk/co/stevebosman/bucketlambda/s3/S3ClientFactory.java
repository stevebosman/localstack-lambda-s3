package uk.co.stevebosman.bucketlambda.s3;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

/**
 * S3 client creation.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class S3ClientFactory {
  /**
   * @return an instance of S3Client
   */
  public static S3Client s3Client() {
    final S3ClientBuilder builder = S3Client.builder();
    getEndpointOverride().ifPresent(builder::endpointOverride);
    return builder
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .region(Region.of(getEnvironmentVariable("AWS_REGION").orElse("us-east-1")))
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .build();
  }

  /**
   * Determine any endpoint override.
   *
   * @return Optional endpoint override value.
   */
  private static Optional<URI> getEndpointOverride() {
    Optional<URI> result = Optional.empty();
    final Optional<String> localstackHostname = getEnvironmentVariable("LOCALSTACK_HOSTNAME");
    if (localstackHostname.isPresent()) {
      final Optional<String> port = getEnvironmentVariable("EDGE_PORT");
      try {
        result = Optional.of(new URI("http://" + localstackHostname.get() + ":" + port.orElse("4566")));
      } catch (final URISyntaxException e) {
        log.error("Failed to create URI", e);
      }
    }
    return result;
  }

  /**
   * Read environment variable.
   *
   * @param environmentVariable Variable to read.
   * @return Optional.empty() if value is not present, empty or blank.
   */
  private static Optional<String> getEnvironmentVariable(final String environmentVariable) {
    Optional<String> result = Optional.empty();
    final String value = System.getenv(environmentVariable);
    if (!Objects.isNull(value) && !value.isBlank()) {
      result = Optional.of(value);
    }
    return result;
  }
}