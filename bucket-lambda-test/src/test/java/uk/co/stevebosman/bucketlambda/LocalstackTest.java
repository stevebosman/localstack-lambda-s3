package uk.co.stevebosman.bucketlambda;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.CLOUDWATCHLOGS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.LAMBDA;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Slf4j
@Testcontainers
public class LocalstackTest {
  public static final String FUNCTION_NAME = "bucket-lambda";
  static Network shared = Network.SHARED;

  public static String networkName = ((Network.NetworkImpl)shared).getName();

  static ObjectMapper objectMapper = new ObjectMapper();

  public static final String JAR_PATH_CONTAINER = "/tmp/localstack/bucket-lambda.jar";
  public static final String JAR_PATH_LOCAL = "../bucket-lambda/target/bucket-lambda-1.0-SNAPSHOT.jar";
  public static final String REGION = "us-east-1";
  @Container
  static final LocalStackContainer localStack
          = new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.3.1"))
          .withServices(S3, LAMBDA, CLOUDWATCHLOGS)
          .withNetwork(shared)
          .withEnv("LAMBDA_DOCKER_NETWORK", networkName)
          .withCopyFileToContainer(MountableFile.forHostPath(new File(JAR_PATH_LOCAL).getPath()),
                                   JAR_PATH_CONTAINER)
          .withFileSystemBind("results", "/home/user/localstack", BindMode.READ_WRITE);

  @BeforeAll
  static void beforeAll() throws IOException, InterruptedException, URISyntaxException {
    final ExecResult createFunctionResult = localStack.execInContainer(
            "awslocal", "lambda", "create-function",
            "--function-name", FUNCTION_NAME,
            "--runtime", "java11",
            "--region", REGION,
            "--handler", "uk.co.stevebosman.bucketlambda.Handler::handleRequest",
            "--role", "arn:aws:iam::123456:role/test",
            "--zip-file", "fileb://" + JAR_PATH_CONTAINER,
            "--environment", "Variables={"
                    + "AWS_ACCESS_KEY_ID=" + localStack.getAccessKey() + ","
                    + "AWS_SECRET_ACCESS_KEY=" + localStack.getSecretKey()
                    + "}"
    );
    log("create-function", createFunctionResult);
  }


  @Test
  public void invoke() throws IOException, InterruptedException {
    final ExecResult invokeResult = localStack.execInContainer(
            "awslocal", "lambda", "invoke",
            "--function-name", FUNCTION_NAME,
            "--payload", "{\"foo\": \"bar\"}",
            "--region", REGION,
            "--output", "json",
            "/home/user/localstack/response.json"
    );
    log("invoke", invokeResult);

    assertEquals("", invokeResult.getStderr());
    assertNotEquals("", invokeResult.getStdout());
  }

  private static void log(final String method, final ExecResult result) {
    log.info("{} result : {}", method, result.getStdout());
    log.info("{} error : {}", method, result.getStderr());
  }
}