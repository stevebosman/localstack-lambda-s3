package uk.co.stevebosman.bucketlambda.helpers;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.io.IOException;

import static uk.co.stevebosman.bucketlambda.helpers.LogHelper.logResult;

@Slf4j
@NoArgsConstructor
public final class S3ApiCommands {
  public static void createBucket(
          final LocalStackContainer localStack, final String region, final String bucket
  ) throws IOException, InterruptedException {
    final Container.ExecResult createBucketResult = localStack.execInContainer(
            "awslocal", "s3api", "create-bucket",
            "--bucket", bucket,
            "--region", region,
            "--create-bucket-configuration", "{\"LocationConstraint\": \"" + region+ "\"}"
    );
    logResult(S3ApiCommands.log, "create-bucket", createBucketResult);
  }

  public static void putObject(
          final LocalStackContainer localStack, final String bucket, final String key, final String bodyPath
  ) throws IOException, InterruptedException {
    final Container.ExecResult createBucketResult = localStack.execInContainer(
            "awslocal", "s3api", "put-object",
            "--bucket", bucket,
            "--key", key,
            "--body", bodyPath
    );
    logResult(S3ApiCommands.log, "put-object", createBucketResult);
  }

}
