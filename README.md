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

## Setting up the S3 Client
In order for the lambda to communicate correctly with the localstack S3 implementation, 
changes are necessary when creating an `S3Client` instance for the lambda to set the `endpointOverride` 
using the provided environment variables `LOCALSTACK_HOSTNAME` and `EDGE_PORT`.

Like most developers I don't like adding test-specific code to the main code base, 
but it appears to be essential here.

### Error messages when accessing S3
If the above is not done the following errors will occur (oddly they depend on the tested region):
#### US-EAST-1
  "errorMessage": "The AWS Access Key Id you provided does not exist in our records. (Service: S3, Status Code: 403, Request ID: ██████████, Extended Request ID: ██████████)"
#### Other regions, e.g. EU-WEST-2
  "errorMessage": "The bucket you are attempting to access must be addressed using the specified endpoint. Please send all future requests to this endpoint. (Service: S3, Status Code: 301, Request ID: ██████████, Extended Request ID: ██████████)",
