package com.myorg;

import com.myorg.infrastructure.ProductServiceStack;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class ProductServiceApp {
    public static void main(final String[] args) {
        App app = new App();

        Environment env = Environment.builder()
            .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
            .region(System.getenv("CDK_DEFAULT_REGION"))
            .build();

        new ProductServiceStack(app, "ProductServiceStack",
            StackProps.builder()
                .env(env)
                .build());

        app.synth();
    }
}

