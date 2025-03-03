package com.myorg.infrastructure;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
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

    // Lambda function for GET /products (List all products)
    Function getProductsListFunction = Function.Builder.create(this, "getProductsList")
        .runtime(Runtime.JAVA_17)
        .handler("com.myorg.handlers.GetProductsListHandler::handleRequest")
        .code(Code.fromAsset("target/product_service-1.0-SNAPSHOT.jar"))
        .memorySize(512) // Increased memory
        .timeout(Duration.seconds(15)) // Increased timeout to prevent failures
        .build();

    // Lambda function for GET /products/{productId} (Get product by ID)
    Function getProductByIdFunction = Function.Builder.create(this, "getProductsById")
        .runtime(Runtime.JAVA_17)
        .handler("com.myorg.handlers.GetProductByIdHandler::handleRequest")
        .code(Code.fromAsset("target/product_service-1.0-SNAPSHOT.jar"))
        .memorySize(512) // Increased memory
        .timeout(Duration.seconds(15))
        .build();

    // Lambda function for POST /products (Create a new product)
    Function createProductFunction = Function.Builder.create(this, "createProduct")
        .runtime(Runtime.JAVA_17)
        .handler("com.myorg.handlers.CreateProductHandler::handleRequest")
        .code(Code.fromAsset("target/product_service-1.0-SNAPSHOT.jar"))
        .memorySize(512)
        .timeout(Duration.seconds(15))
        .build();

    // Create API Gateway
    RestApi api = RestApi.Builder.create(this, "ProductApi")
        .restApiName("Product Service API")
        .description("API for Product Service")
        .defaultCorsPreflightOptions(CorsOptions.builder()
            .allowOrigins(Cors.ALL_ORIGINS) // Allow all origins
            .allowMethods(Cors.ALL_METHODS) // Allow all methods (GET, POST, OPTIONS, etc.)
            .allowHeaders(List.of("Content-Type", "X-Amz-Date", "Authorization", "X-Api-Key", "X-Amz-Security-Token")) // Allow specific headers
            .build())
        .build();

    // Create "/products" resource
    IResource productsResource = api.getRoot().addResource("products");

    // GET /products
    productsResource.addMethod("GET", new LambdaIntegration(getProductsListFunction),
        MethodOptions.builder()
            .authorizationType(AuthorizationType.NONE)
            .methodResponses(List.of(
                MethodResponse.builder()
                    .statusCode("200")
                    .responseParameters(Map.of(
                        "method.response.header.Access-Control-Allow-Origin", true,
                        "method.response.header.Access-Control-Allow-Methods", true,
                        "method.response.header.Access-Control-Allow-Headers", true
                    ))
                    .build()))
            .build());

    // POST /products
    productsResource.addMethod("POST", new LambdaIntegration(createProductFunction),
        MethodOptions.builder()
            .authorizationType(AuthorizationType.NONE)
            .methodResponses(List.of(
                MethodResponse.builder()
                    .statusCode("201")
                    .responseParameters(Map.of(
                        "method.response.header.Access-Control-Allow-Origin", true,
                        "method.response.header.Access-Control-Allow-Methods", true,
                        "method.response.header.Access-Control-Allow-Headers", true
                    ))
                    .build()))
            .build());

    // Create "/products/{productId}" resource
    IResource productByIdResource = productsResource.addResource("{productId}");

    // GET /products/{productId}
    productByIdResource.addMethod("GET", new LambdaIntegration(getProductByIdFunction),
        MethodOptions.builder()
            .authorizationType(AuthorizationType.NONE)
            .methodResponses(List.of(
                MethodResponse.builder()
                    .statusCode("200")
                    .responseParameters(Map.of(
                        "method.response.header.Access-Control-Allow-Origin", true,
                        "method.response.header.Access-Control-Allow-Methods", true,
                        "method.response.header.Access-Control-Allow-Headers", true
                    ))
                    .build()))
            .build());

    // Grant DynamoDB Scan permission for listing products
    // Grant DynamoDB Scan permission for listing products
    getProductsListFunction.addToRolePolicy(PolicyStatement.Builder.create()
        .effect(Effect.ALLOW)
        .actions(List.of("dynamodb:Scan"))
        .resources(List.of(
            "arn:aws:dynamodb:eu-west-1:503561446710:table/products",
            "arn:aws:dynamodb:eu-west-1:503561446710:table/stocks"
        ))
        .build());


    // Grant DynamoDB GetItem permission for retrieving a specific product
    // Grant DynamoDB GetItem permission for retrieving a specific product
    getProductByIdFunction.addToRolePolicy(PolicyStatement.Builder.create()
        .effect(Effect.ALLOW)
        .actions(List.of("dynamodb:GetItem"))
        .resources(List.of(
            "arn:aws:dynamodb:eu-west-1:503561446710:table/products",
            "arn:aws:dynamodb:eu-west-1:503561446710:table/stocks" // Add this line
        ))
        .build());

    // Allow CreateProduct Lambda to write to both 'products' and 'stocks' tables
    createProductFunction.addToRolePolicy(PolicyStatement.Builder.create()
        .effect(Effect.ALLOW)
        .actions(List.of("dynamodb:PutItem"))
        .resources(List.of(
            "arn:aws:dynamodb:eu-west-1:503561446710:table/products",
            "arn:aws:dynamodb:eu-west-1:503561446710:table/stocks"
        ))
        .build());


    // Output API Gateway URL after deployment
    CfnOutput.Builder.create(this, "ApiGatewayUrl")
        .description("The URL for the Product Service API")
        .value(api.getUrl())
        .build();
  }
}