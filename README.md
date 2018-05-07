# Guarana
Guarana is a SAM packaged webhook debugging tool, it captures HTTP requests and saves them kindly to S3.

## Using it 

```sam deploy --template-file guarana.yaml --stack-name guarana --parameter-overrides bucketName=scanii-test --capabilities CAPABILITY_IAM```

Important config options: 
* bucketName: the name of the bucket in which to store the webhook data
* bucketPrefix: the prefix to be appended to object paths stored in the S3 bucket. For example, a bucketName of "bucket1" and a prefix "webhooks" will store all inbound HTTP requests under s3://bucket1/webhooks/
