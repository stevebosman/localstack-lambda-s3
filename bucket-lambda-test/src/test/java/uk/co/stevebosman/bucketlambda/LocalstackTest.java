package uk.co.stevebosman.bucketlambda;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.CLOUDWATCHLOGS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.LAMBDA;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static uk.co.stevebosman.bucketlambda.LambdaCommands.createFunction;
import static uk.co.stevebosman.bucketlambda.LambdaCommands.invoke;
import static uk.co.stevebosman.bucketlambda.S3ApiCommands.createBucket;
import static uk.co.stevebosman.bucketlambda.S3ApiCommands.putObject;

@Slf4j
@Testcontainers
public class LocalstackTest {
  private static final String FUNCTION_NAME = "bucket-lambda";
  private static final Network SHARED = Network.SHARED;
  private static final String NETWORK_NAME = ((Network.NetworkImpl) SHARED).getName();
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String JAR_PATH_CONTAINER = "/tmp/localstack/bucket-lambda.jar";
  private static final String JAR_PATH_LOCAL = "../bucket-lambda/target/bucket-lambda-1.0-SNAPSHOT.jar";
  private static final String REGION = "eu-west-2";
  private static final String RESULTS_PATH_CONTAINER = "/home/user/localstack";
  private static final String RESULTS_PATH_LOCAL = "results";
  private static final String OBJECTS_PATH_CONTAINER = "/home/user/objects";
  public static final String OBJECTS_PATH_LOCAL = "objects";
  @Container
  static final LocalStackContainer localStack
          = new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.3.1"))
          .withServices(S3, LAMBDA, CLOUDWATCHLOGS)
          .withNetwork(SHARED)
          .withEnv("LAMBDA_DOCKER_NETWORK", NETWORK_NAME)
          .withCopyFileToContainer(MountableFile.forHostPath(new File(JAR_PATH_LOCAL).getPath()),
                                   JAR_PATH_CONTAINER)
          .withFileSystemBind(RESULTS_PATH_LOCAL, RESULTS_PATH_CONTAINER, BindMode.READ_WRITE)
          .withClasspathResourceMapping(OBJECTS_PATH_LOCAL, OBJECTS_PATH_CONTAINER, BindMode.READ_ONLY);
  public static final String HANDLER = "uk.co.stevebosman.bucketlambda.Handler::handleRequest";

  @BeforeAll
  static void beforeAll() throws IOException, InterruptedException {
    createFunction(localStack, FUNCTION_NAME, REGION, HANDLER, "fileb://" + JAR_PATH_CONTAINER);
  }

  @Test
  public void hello() throws IOException, InterruptedException {
    final String resultsFileName = "helloResult.json";
    invoke(localStack, FUNCTION_NAME, REGION, "{\"command\": \"hello\"}", resultsFileName);

    try (final Stream<String> lines = Files.lines(Path.of(RESULTS_PATH_LOCAL, resultsFileName))) {
      final Map<String, Object> result = readResultAsMap(lines);
      assertNotNull(result.get("timestamp"), "No timestamp");
      assertEquals("success", result.get("status"), "Unexpected status");
      assertEquals("Hello World", result.get("message"), "Unexpected message");
    }
  }

  @Test
  public void readFromBucket() throws IOException, InterruptedException {
    final String bucket = "test-bucket";
    createBucket(localStack, REGION, bucket);

    final String objectKey = "readFromBucket.json";
    putObject(localStack, bucket, objectKey, OBJECTS_PATH_CONTAINER + "/" + objectKey);

    final String resultsFileName = "readFromBucketResult.json";
    invoke(localStack, FUNCTION_NAME, REGION,
           "{"
                   + "\"command\":\"read\","
                   + "\"bucket\":\"" + bucket + "\","
                   + "\"object-key\":\"" + objectKey + "\","
                   + "\"property\":\"message\""
                   + "}",
           resultsFileName);

    try (final Stream<String> lines = Files.lines(Path.of(RESULTS_PATH_LOCAL, resultsFileName))) {
      final Map<String, Object> result = readResultAsMap(lines);
      assertNotNull(result.get("timestamp"), "No timestamp");
      assertEquals("success", result.get("status"), "Unexpected status");
      assertEquals("Pass it on", result.get("message"), "Unexpected message");
    }
  }

  private static Map<String, Object> readResultAsMap(final Stream<String> lines) throws IOException {
    final String results = lines.collect(Collectors.joining("\n"));
    @SuppressWarnings("unchecked") final Map<String, Object> result = OBJECT_MAPPER.readValue(results, HashMap.class);
    return result;
  }

}