# Task 4 **Integration With Nosql Database**

## Useful commands

 * `mvn package`     compile and run tests
 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation

---

## **🚀 Prerequisites**
Before running the project, ensure you have the following installed:

1. **Java 11**
    - Check version:
      ```sh
      java -version
      ```

2. **Maven 3**
    - Check version:
      ```sh
      mvn -version
      ```

3. **AWS CLI** (Configured with access keys)
    - Check version:
      ```sh
      aws --version
      ```
    - Configure AWS credentials if not set up:
      ```sh
      aws configure
      ```

4. **AWS CDK**
    - Check version:
      ```sh
      cdk --version
      ```

---

## **🛠️ Build the Project**
Compile and package the application:

```
mvn clean package
```

---

## **🚀 Deploy to AWS**
To deploy the application to **AWS Lambda** with API Gateway:

```sh
cdk bootstrap   # (Only required for first-time setup)
cdk synth       # Synthesizes the CloudFormation template
cdk deploy      # Deploys the stack
```

After deployment, the API Gateway URL will be displayed:
```
Outputs:
ProductServiceStack.ApiGatewayUrl = https://your-api-id.execute-api.eu-west-1.amazonaws.com/prod/
```

---

## **🧪 Running Tests**
Run unit tests using:

```
mvn test
```

---

## **📡 API Endpoints**

---

## **🐛 Debugging**
### **Check Lambda Logs**
If something isn’t working, view logs using:

```
aws logs tail /aws/lambda/ProductServiceStack-getProductsList --follow
aws logs tail /aws/lambda/ProductServiceStack-getProductsById --follow
```

---

## **🗑️ Cleanup**
To delete all AWS resources (API Gateway, Lambda, IAM roles):

```
cdk destroy
```

