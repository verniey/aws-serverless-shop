# **Task 4: Integration With NoSQL Database** 🚀

🔗 **Deployed Frontend URL:**  
[CloudFront Distribution](https://d2i9wqrns222hu.cloudfront.net/)

🔗 **API Gateway Base URL:**  
`https://8c0xz8gwof.execute-api.eu-west-1.amazonaws.com/prod`

---

## 📌 **Table of Contents**
- [🚀 DynamoDB Initialization Script](#-dynamodb-initialization-script-dynamodb-initsh)
- [🛠️ Prerequisites](#-prerequisites)
- [🚀 Build & Deploy](#-build--deploy)
- [📡 API Endpoints](#-api-endpoints)
- [📜 OpenAPI Specification](#-openapi-specification)
- [🧪 Running Tests](#-running-tests)
- [🗑️ Cleanup](#-cleanup)

---

## 🚀 **DynamoDB Initialization Script (`dynamodb-init.sh`)**

This script populates **DynamoDB** with test data for **products** and **stocks**.

### 🛠️ **Usage Instructions**
1️⃣ **Navigate to the backend directory**
   ```sh
   cd backend
   ```

2️⃣ **Make the script executable**
   ```sh
   chmod +x dynamodb-init.sh
   ```

3️⃣ **Run the script**
   ```sh
   ./dynamodb-init.sh
   ```

### ⚠️ **Prerequisites**
✅ AWS CLI must be **configured** with valid credentials.  
✅ The **DynamoDB tables (`products` and `stocks`) must exist** before running the script.

---

## 🛠️ **Prerequisites**
Before building and deploying, ensure you have the following installed:

### ✅ **Software Requirements**
| **Software** | **Version Check Command** |
|--------------|---------------------------|
| **Java 17+** | `java -version` |
| **Maven 3+** | `mvn -version` |
| **AWS CLI**  | `aws --version` |
| **AWS CDK**  | `cdk --version` |

### ✅ **AWS Configuration**
If AWS CLI is not set up, configure it using:
```sh
aws configure
```

---

## 🚀 **Build & Deploy**
### 🛠️ **Build the Project**
Compile and package the application:
```sh
mvn clean package
```

### 🚀 **Deploy to AWS**
To deploy the backend to **AWS Lambda & API Gateway**, run:
```sh
cdk bootstrap   # (Only required for first-time setup)
cdk synth       # Synthesizes the CloudFormation template
cdk deploy      # Deploys the stack
```

After deployment, the **API Gateway URL** will be displayed:
```
Outputs:
ProductServiceStack.ApiGatewayUrl = https://8c0xz8gwof.execute-api.eu-west-1.amazonaws.com/prod/
```

---

## 📡 **API Endpoints**
### 📝 **Create a Product**
```sh
curl -X POST "https://8c0xz8gwof.execute-api.eu-west-1.amazonaws.com/prod/products" \
     -H "Content-Type: application/json" \
     -d '{"title":"Test Product","description":"This is a test product","price":100,"count":5}'
```

### 🔍 **Get All Products**
```sh
curl -X GET "https://8c0xz8gwof.execute-api.eu-west-1.amazonaws.com/prod/products"
```

### 🔍 **Get Product by ID**
```sh
curl -X GET "https://8c0xz8gwof.execute-api.eu-west-1.amazonaws.com/prod/products/{productId}"
```

---

## 📜 **OpenAPI Specification**
An OpenAPI (Swagger) documentation file (`openapi.yaml`) is available for testing.

### 🛠️ **How to View in Swagger Editor**
1. Open [Swagger Editor](https://editor.swagger.io/)
2. Click **File** → **Import File**
3. Select **`openapi.yaml`** from `src/main/resources`

---

## 🧪 **Running Tests**
Run **unit tests** with:
```sh
mvn test
```

---

## 🗑️ **Cleanup**
To delete all AWS resources (API Gateway, Lambda, IAM roles):
```sh
cdk destroy
```

---

## **🔗 References**
- **AWS DynamoDB Docs:** [https://docs.aws.amazon.com/dynamodb](https://docs.aws.amazon.com/dynamodb)
- **AWS CDK Docs:** [https://docs.aws.amazon.com/cdk](https://docs.aws.amazon.com/cdk)
- **Swagger Editor:** [https://editor.swagger.io/](https://editor.swagger.io/)

---

Now the **README is complete, structured, and ready for deployment!** 🚀