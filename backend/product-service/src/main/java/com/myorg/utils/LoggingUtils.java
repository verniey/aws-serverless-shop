package com.myorg.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.Context;

public class LoggingUtils {

  /**
   * Logs the details of an incoming request.
   *
   * @param event   The incoming request event.
   * @param context The Lambda execution context.
   */
  public static void logIncomingRequest(APIGatewayProxyRequestEvent event, Context context) {
    context.getLogger().log("Incoming request: " + event.toString());
    context.getLogger().log("HTTP Method: " + event.getHttpMethod());
    context.getLogger().log("Path: " + event.getPath());
    context.getLogger().log("Query Parameters: " + event.getQueryStringParameters());
    context.getLogger().log("Headers: " + event.getHeaders());
    context.getLogger().log("Body: " + event.getBody());
  }
}