package com.uvasoftware.guarana;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static org.mockito.Mockito.*;

class HandlerTest {
  @Test
  void shouldHandlePostRequests() {
    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setBody("hello world");
    event.setPath("/foo/bar");
    event.setHttpMethod("POST");
    event.setIsBase64Encoded(false);
    event.setHeaders(new HashMap<>());
    event.getHeaders().put("h1", "v1");

    Context ctx = mock(Context.class);
    when(ctx.getLogger()).thenReturn(Mockito.mock(LambdaLogger.class));
    when(ctx.getAwsRequestId()).thenReturn("1234");

    PersistenceCapable persisted = mock(PersistenceCapable.class);

    Handler handler = new Handler(persisted);
    APIGatewayProxyResponseEvent r = handler.handleRequest(event, ctx);
    Assertions.assertNotNull(r.getBody());

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

    verify(persisted, times(1)).persist("guarana/foo/bar/" + formatter.format(now) + "/1234/metadata.json", "{\n" +
      "  \"path\" : \"/foo/bar\",\n" +
      "  \"headers\" : {\n" +
      "    \"h1\" : \"v1\"\n" +
      "  },\n" +
      "  \"method\" : \"POST\",\n" +
      "  \"functionVersion\" : null,\n" +
      "  \"functionName\" : null,\n" +
      "  \"id\" : \"1234\",\n" +
      "  \"queryString\" : null\n" +
      "}");
    verify(persisted, times(1)).persist("guarana/foo/bar/" + formatter.format(now) + "/1234/content.txt", "hello world");
  }

  @Test
  void shouldHandlePostRequestsAndSetCorrectExtension() {

    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setBody("{\"foo\" : \"bar\"");
    event.setPath("/foo/bar");
    event.setHttpMethod("POST");
    event.setIsBase64Encoded(false);
    event.setHeaders(new HashMap<>());
    event.getHeaders().put("h1", "v1");
    event.getHeaders().put("Content-Type", "application/javascript");


    Context ctx = mock(Context.class);
    when(ctx.getLogger()).thenReturn(Mockito.mock(LambdaLogger.class));
    when(ctx.getAwsRequestId()).thenReturn("1234");

    PersistenceCapable persister = mock(PersistenceCapable.class);

    Handler handler = new Handler(persister);
    handler.handleRequest(event, ctx);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

    verify(persister, times(1)).persist("guarana/foo/bar/" + formatter.format(now) + "/1234/content.js", "{\"foo\" : \"bar\"");
  }

  @Test
  void shouldHandleGetRequests() {

    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setBody("{\"foo\" : \"bar\"");
    event.setPath("/foo/bar");
    event.setHttpMethod("GET");
    event.setIsBase64Encoded(false);
    event.setHeaders(new HashMap<>());
    event.getHeaders().put("h1", "v1");

    event.setQueryStringParameters(new HashMap<>());
    event.getQueryStringParameters().put("foo", "bar");


    Context ctx = mock(Context.class);
    when(ctx.getLogger()).thenReturn(Mockito.mock(LambdaLogger.class));
    when(ctx.getAwsRequestId()).thenReturn("1234");

    PersistenceCapable persister = mock(PersistenceCapable.class);

    Handler handler = new Handler(persister);
    handler.handleRequest(event, ctx);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

    verify(persister, times(1)).persist("guarana/foo/bar/" + formatter.format(now) + "/1234/metadata.json", "{\n" +
      "  \"path\" : \"/foo/bar\",\n" +
      "  \"headers\" : {\n" +
      "    \"h1\" : \"v1\"\n" +
      "  },\n" +
      "  \"method\" : \"GET\",\n" +
      "  \"functionVersion\" : null,\n" +
      "  \"functionName\" : null,\n" +
      "  \"id\" : \"1234\",\n" +
      "  \"queryString\" : {\n" +
      "    \"foo\" : \"bar\"\n" +
      "  }\n" +
      "}");
  }

  @Test
  void shouldHandleDoubleTrailingSlashes() {
    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
    event.setBody("{\"foo\" : \"bar\"");
    event.setPath("/Prod/test/");
    event.setHttpMethod("POST");
    event.setHeaders(new HashMap<>());

    Context ctx = mock(Context.class);
    when(ctx.getLogger()).thenReturn(Mockito.mock(LambdaLogger.class));
    when(ctx.getAwsRequestId()).thenReturn("1234");

    PersistenceCapable persister = mock(PersistenceCapable.class);

    Handler handler = new Handler(persister);
    handler.handleRequest(event, ctx);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

    verify(persister, times(1)).persist(eq("guarana/Prod/test/" + formatter.format(now) + "/1234/metadata.json"), anyString());
  }

  @Test
  void shouldThrowExceptionIfNoBucket() {
    Assertions.assertThrows(IllegalStateException.class, () -> {
      Handler handler = new Handler();
    });
  }
}
