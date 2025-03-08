# **Task 5: Integration with S3** 🚀


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
