package com.myorg.infrastructure;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;

public class ProductServiceStack extends Stack {
    public ProductServiceStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Lambda function for getProductsList
        Function getProductsListFunction = Function.Builder.create(this, "getProductsList")
            .runtime(Runtime.JAVA_11)
            .handler("com.myorg.handlers.GetProductsListHandler::handleRequest") // 🔥 Corrected handler path
            .code(Code.fromAsset("target/product-service-1.0-SNAPSHOT.jar")) // 🔥 Correct path
            .build();

        // Lambda function for getProductsById
        Function getProductsByIdFunction = Function.Builder.create(this, "getProductsById")
            .runtime(Runtime.JAVA_11)
            .handler("com.myorg.handlers.GetProductsByIdHandler::handleRequest") // 🔥 Corrected handler path
            .code(Code.fromAsset("target/product-service-1.0-SNAPSHOT.jar")) // 🔥 Correct path
            .build();

        // API Gateway
        LambdaRestApi api = LambdaRestApi.Builder.create(this, "ProductApi")
            .defaultIntegration(new LambdaIntegration(getProductsListFunction)) // 🔥 Correct integration
            .proxy(false)
            .build();

        // Define API resources and methods
        api.getRoot().addResource("products").addMethod("GET", new LambdaIntegration(getProductsListFunction));
        api.getRoot().addResource("products").addResource("{productId}").addMethod("GET", new LambdaIntegration(getProductsByIdFunction));
    }
}
