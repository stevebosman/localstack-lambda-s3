package uk.co.stevebosman.bucketlambda;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static uk.co.stevebosman.bucketlambda.LogHelper.logResult;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LambdaCommands {
  public static void createFunction(
          final LocalStackContainer localStack,
          final String functionName,
          final String region,
          final String handler,
          final String jarPath
  ) throws IOException, InterruptedException {
    final Container.ExecResult createFunctionResult = localStack.execInContainer(
            "awslocal", "lambda", "create-function",
            "--function-name", functionName,
            "--runtime", "java11",
            "--region", region,
            "--handler", handler,
            "--role", "arn:aws:iam::123456:role/test",
            "--zip-file", jarPath,
            "--environment", "Variables={"
                    + "AWS_ACCESS_KEY_ID=" + localStack.getAccessKey() + ","
                    + "AWS_SECRET_ACCESS_KEY=" + localStack.getSecretKey()
                    + "}"
    );
    logResult(log, "create-function", createFunctionResult);
  }

  public static void invoke(final LocalStackContainer localStack, final String functionName, final String region, final String payload, final String resultsFileName) throws IOException, InterruptedException {
    final Container.ExecResult invokeResult = localStack.execInContainer(
            "awslocal", "lambda", "invoke",
            "--function-name", functionName,
            "--payload", payload,
            "--region", region,
            "--output", "json",
            "/home/user/localstack/" + resultsFileName
    );
    logResult(log, "invoke", invokeResult);

    assertEquals("", invokeResult.getStderr());
    assertNotEquals("", invokeResult.getStdout());
  }
}
