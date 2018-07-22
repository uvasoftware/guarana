package com.uvasoftware.guarana;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class S3BucketPublisher implements PersistenceCapable {
    private final String bucketName;

    S3BucketPublisher(String bucketName) {
        this.bucketName = bucketName;
    }

    @Override
    public void persist(String path, String contents) {
        final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
        s3.putObject(bucketName, path, contents);
    }
}
