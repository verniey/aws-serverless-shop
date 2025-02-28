# Task 3 **Product Service**

## Useful commands

 * `mvn package`     compile and run tests
 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation

---

## **Product Service**

This is a **serverless AWS Lambda-based product service** that provides endpoints to:
- Retrieve a list of products
- Fetch product details by ID

The service is deployed using **AWS CDK** and exposed via **AWS API Gateway**.

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

## **💾 Installation**
Clone the repository and navigate to the project directory:

```
cd product-service
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
### **🔹 Get All Products**
```http
GET /products
```
**Example Response:**
```json
[
  {
    "id": "1",
    "title": "Product A",
    "description": "Sample product",
    "price": 29.99
  },
  {
    "id": "2",
    "title": "Product B",
    "description": "Another product",
    "price": 35.50
  }
]
```

---

### **🔹 Get Product by ID**
```http
GET /products/{productId}
```
#### Example Request:
```http
GET /products/1
```
**Example Response (200 OK):**
```json
{
  "id": "1",
  "title": "Product A",
  "description": "Sample product",
  "price": 29.99
}
```

**Example Response (404 Not Found):**
```json
{
  "error": "Product not found"
}
```

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

---

## **📜 OpenAPI Specification**
A Swagger documentation file (`openapi.yaml`) is provided for testing in [Swagger Editor](https://editor.swagger.io/).

To view it:
1. Open [Swagger Editor](https://editor.swagger.io/)
2. Click "File" → "Import File"
3. Select **`openapi.yaml` located in `src/main/resources`.**

---

## **📌 Notes**
- This project is **serverless** and does **not** use a database.
- Products are loaded from `products.json`.
- Ensure AWS credentials are correctly set up before deployment.

---
