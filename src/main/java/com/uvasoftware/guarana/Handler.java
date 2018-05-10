package com.uvasoftware.guarana;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final String BUCKET_NAME = "DESTINATION_BUCKET";
    private static final String BUCKET_PREFIX = "DESTINATION_BUCKET_PREFIX";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private String bucketName;
    private PersistenceCapable persister;
    private String prefix = "guarana";
    private ObjectMapper objectMapper;

    @SuppressWarnings("unused")
    public Handler() {
        bucketName = System.getenv(BUCKET_NAME);
        if (bucketName == null) {
            throw new IllegalStateException("Destination bucket variable not set, please set: " + BUCKET_NAME);
         }

        initialize();
        persister = new S3BucketPublisher(bucketName);
    }

    public Handler(PersistenceCapable persister) {
        initialize();
        this.persister = persister;
    }

    private void initialize() {
        if (System.getenv(BUCKET_PREFIX) != null) {
            prefix = System.getenv(BUCKET_PREFIX);
        }

        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {

        LambdaLogger log = context.getLogger();
        log.log(String.format("processing event with path: [%s] and method: [%s]\n", request.getPath(), request.getHttpMethod()));
        log.log(String.format("using bucket [%s] with prefix [%s]\n", bucketName, prefix));

        // adding temporal path elements:
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY/MM/dd");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);


        try {
            // metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("id", context.getAwsRequestId());
            metadata.put("method", request.getHttpMethod());
            metadata.put("path", request.getPath());
            metadata.put("queryString", request.getQueryStringParameters());
            metadata.put("headers", request.getHeaders());
            metadata.put("functionName", context.getFunctionName());
            metadata.put("functionVersion", context.getFunctionVersion());

            final String headerPath = String.join("/", prefix, sanitizePath(request.getPath()), formatter.format(now), context.getAwsRequestId(), "metadata") + ".json";
            persister.persist(headerPath, objectMapper.writeValueAsString(metadata));

            // handling POST:
            if (request.getHttpMethod().equals("POST")) {
                if (request.getIsBase64Encoded() == null || !request.getIsBase64Encoded()) {
                    String extension;
                    if (request.getHeaders().containsKey(CONTENT_TYPE_HEADER)) {
                        String contentType = request.getHeaders().get(CONTENT_TYPE_HEADER);
                        extension = Extensions.resolveOrDefault(contentType);
                        log.log(String.format("using extension [%s] for content type [%s]\n", extension, contentType));
                    } else {
                        extension = Extensions.resolveOrDefault("none");
                    }
                    final String bodyPath = String.join("/", prefix, sanitizePath(request.getPath()), formatter.format(now), context.getAwsRequestId(), "content") + extension;
                    persister.persist(bodyPath, request.getBody());

                } else {
                    log.log("ignoring base64 encoded body");
                }

            }

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(204);
        return response;
    }

    private String sanitizePath(String requestPath) {
        if (requestPath.endsWith("/")) {
            requestPath = requestPath.substring(0, requestPath.length() - 1);
        }
        if (requestPath.startsWith("/")) {
            requestPath = requestPath.substring(1, requestPath.length());
        }
        return requestPath;
    }
}
