# Guarana
Guarana is a [SAM](https://github.com/awslabs/serverless-application-model) packaged [webhook](https://en.wikipedia.org/wiki/Webhook) debugging tool, it captures HTTP requests and saves them kindly to S3 to help you review/debug them later.

## Deploying it 
The easiest way to install this tool is using the  server less application repository: https://serverlessrepo.aws.amazon.com/applications

Important config options:

* **bucketName**: the name of the bucket in which to store the webhook data
* **bucketPrefix**: the prefix to be appended to object paths stored in the S3 bucket. For example, a bucketName of “bucket1” and a prefix “webhooks” will store all inbound HTTP requests under s3://bucket1/webhooks/

## Features

### It allows a single lambda function to be used across multiple services
Guarana automatically pass on proxied paths to the S3 destination bucket allowing a single lambda function to be easily used for many services. For example, assuming your lambda function URL is https://123.execute-api.us-east-1.amazonaws.com/Prod you can use https://123.execute-api.us-east-1.amazonaws.com/Prod/stripe.com at Stripe and all its webhooks will be stored as `s3:///${bucketName}/${bucketPrefix}/stripe.com/${year}/${month}/${day}`

### It captures both the request body as well as relevant metadata
For every HTTP request Guarana sees it stores two JSON objects:
* metadata.json - containing request metadata such as the HTTP verb and headers
* contents.json - containing the body of the HTTP request using the extension extracted from the provided content type header. If no content type is provided it will default to .txt

_example metadata object:_
```
{
  "path" : "/test",
  "headers" : {
    "Accept" : "*/*",
    "CloudFront-Forwarded-Proto" : "https",
    "CloudFront-Is-Desktop-Viewer" : "true",
    "CloudFront-Is-Mobile-Viewer" : "false",
    "CloudFront-Is-SmartTV-Viewer" : "false",
    "CloudFront-Is-Tablet-Viewer" : "false",
    "CloudFront-Viewer-Country" : "US",
    "content-type" : "application/x-www-form-urlencoded",
    "Host" : "helloworld.execute-api.us-east-1.amazonaws.com",
    "User-Agent" : "curl/7.54.0",
    "Via" : "2.0 6ac65de939573cb26099f6407fa8e169.cloudfront.net (CloudFront)",
    "X-Amz-Cf-Id" : "ZWvIxOFunW1DKIqYa20dpV1M91rOPCEOhDK5rse7EkuFEUC1pJsJlA==",
    "X-Amzn-Trace-Id" : "Root=1-5adcb614-5891de00e823dc7095af29e0",
    "X-Forwarded-For" : "98.167.137.47, 52.46.35.155",
    "X-Forwarded-Port" : "443",
    "X-Forwarded-Proto" : "https"
  },
  "method" : "POST",
  "functionVersion" : "$LATEST",
  "functionName" : "guarana-Guarana-S91VVGJ7WH0I",
  "id" : "f096e2f8-4648-11e8-a17b-2308d7d30a46",
  "queryString" : null
}
```


_example content object:_
```
{
  "hellow" : "world",
}
```
