package com.myorg.infrastructure;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.services.apigateway.*;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;

import java.util.List;
import java.util.Map;

public class ProductServiceStack extends Stack {
    public ProductServiceStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Lambda function for getProductsList
        Function getProductsListFunction = Function.Builder.create(this, "getProductsList")
            .runtime(Runtime.JAVA_11)
            .handler("com.myorg.handlers.GetProductsListHandler::handleRequest")
            .code(Code.fromAsset("target/product_service-1.0-SNAPSHOT.jar"))
            .build();

        // Lambda function for getProductsById
        Function getProductByIdFunction = Function.Builder.create(this, "getProductsById")
            .runtime(Runtime.JAVA_11)
            .handler("com.myorg.handlers.GetProductByIdHandler::handleRequest")
            .code(Code.fromAsset("target/product_service-1.0-SNAPSHOT.jar"))
            .build();

        // API Gateway with CORS enabled
        LambdaRestApi api = LambdaRestApi.Builder.create(this, "ProductApi")
            .handler(getProductsListFunction)
            .proxy(false)
            .build();

        // Create 'products' resource only ONCE
        IResource productsResource = api.getRoot().addResource("products");

        // Add GET method for listing all products
        productsResource.addMethod("GET", new LambdaIntegration(getProductsListFunction),
            MethodOptions.builder()
                .authorizationType(AuthorizationType.NONE)
                .methodResponses(List.of(
                    MethodResponse.builder()
                        .statusCode("200")
                        .responseParameters(Map.of(
                            "method.response.header.Access-Control-Allow-Origin", true
                        ))
                        .build()))
                .build());

        // Add '{productId}' sub-resource under 'products'
        productsResource.addResource("{productId}")
            .addMethod("GET", new LambdaIntegration(getProductByIdFunction),
                MethodOptions.builder()
                    .authorizationType(AuthorizationType.NONE)
                    .methodResponses(List.of(
                        MethodResponse.builder()
                            .statusCode("200")
                            .responseParameters(Map.of(
                                "method.response.header.Access-Control-Allow-Origin", true
                            ))
                            .build()))
                    .build());

        // Output API Gateway URL after deployment
        CfnOutput.Builder.create(this, "ApiGatewayUrl")
            .description("The URL for the Product Service API")
            .value(api.getUrl())
            .build();
    }
}
