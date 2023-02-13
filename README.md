# localstack-lambda-s3
 
Demonstrate use of testcontainers localstack to test an AWS Lambda accessing an S3 bucket.

This project consists of two projects.

To run the demo:

* Ensure Docker is running;
* build `bucket-lambda`: `cd bucket-lambda; mvn clean package`;
  * This will run unit tests using testcontainers localstack confirming the S3 interactions. 
* run tests in `bucket-lambda-test`: `cd ../bucket-lambda-test; mvn clean verify`.
  * This will run a localstack based test which demonstrates testing the complete AWS Lambda 
    using testcontainers and localstack.

## Unit Testing of S3 Interactions

import `org.testcontainers:localstack` and  `org.testcontainers:junit-jupiter`.

    <dependency>
      <groupId>org.testcontainers</groupId>
      <artifactId>localstack</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.testcontainers</groupId>
      <artifactId>junit-jupiter</artifactId>
      <scope>test</scope>
    </dependency>

### Main code

The
[Startup](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/lambda-optimize-starttime.html)
guidance should be followed.

If when running tests errors occur regrding multiple HTTP implementations then it probably hasn't been.

### Test class changes
Test class annotated with `@TestContainers`

    import org.testcontainers.junit.jupiter.Testcontainers;

Unit test methods annotated with `@Test` as normal.

Create a static `LocalStackContainer`.

    import org.testcontainers.containers.localstack.LocalStackContainer;
    import org.testcontainers.junit.jupiter.Container;
    import org.testcontainers.utility.DockerImageName;
    
    @Container
    static final LocalStackContainer localStack
            = new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.3.1"))
              .withServices(LocalStackContainer.Service.S3);

Since starting a container takes a while, consider centralising S3 tests in the one test class.
Testcontainers has an [experimental mode](https://www.testcontainers.org/features/reuse/) supporting container reuse, 
but it doesn't close the container afterwards. 

S3Client instance passed to the method under test created using, 
this can also be used in the test method to create buckets, and objects etc.

    import org.testcontainers.containers.localstack.LocalStackContainer;
    import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
    import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
    import software.amazon.awssdk.regions.Region;
    import software.amazon.awssdk.services.s3.S3Client;
    
    S3Client.builder()
            .endpointOverride(localStack.getEndpointOverride(LocalStackContainer.Service.S3))
            .credentialsProvider(
              StaticCredentialsProvider.create(
                AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())
              )
            )
            .region(Region.of(localStack.getRegion()))
            .build();

## Functional Testing of Lambda

### Test Class

Very similar to Unit testing, but `LocalStackContainer` needs to use the Jar file, 
and bind any directories needed during the tests.

    private static final Network SHARED = Network.SHARED;
    private static final String NETWORK_NAME = ((Network.NetworkImpl) SHARED).getName();
    @Container
    static final LocalStackContainer localStack
            = new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.3.1"))
              .withServices(S3, LAMBDA, CLOUDWATCHLOGS)
              .withNetwork(SHARED)
              .withEnv("LAMBDA_DOCKER_NETWORK", NETWORK_NAME)
              .withCopyFileToContainer(
                MountableFile.forHostPath(new File(JAR_PATH_LOCAL).getPath()), JAR_PATH_CONTAINER
              )
              .withFileSystemBind(RESULTS_PATH_LOCAL, RESULTS_PATH_CONTAINER, BindMode.READ_WRITE)
              .withClasspathResourceMapping(OBJECTS_PATH_LOCAL, OBJECTS_PATH_CONTAINER, BindMode.READ_ONLY);

#### Setting up the S3 Client
In order for the lambda to communicate correctly with the localstack S3 implementation, 
changes are necessary when creating an `S3Client` instance for the lambda to set the `endpointOverride` 
using the provided environment variables `LOCALSTACK_HOSTNAME` and `EDGE_PORT`.

Like most developers I don't like adding test-specific code to the main code base, 
but it appears to be essential here.

##### Error messages when accessing S3
If the above is not done the following errors will occur (oddly they depend on the tested region):
###### US-EAST-1
  "errorMessage": "The AWS Access Key Id you provided does not exist in our records. (Service: S3, Status Code: 403, Request ID: ██████████, Extended Request ID: ██████████)"
###### Other regions, e.g. EU-WEST-2
  "errorMessage": "The bucket you are attempting to access must be addressed using the specified endpoint. Please send all future requests to this endpoint. (Service: S3, Status Code: 301, Request ID: ██████████, Extended Request ID: ██████████)",
