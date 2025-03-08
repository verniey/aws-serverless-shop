#!/bin/bash

# Clean the 'products' table
echo "Cleaning the 'products' table..."

aws dynamodb scan \
    --table-name products \
    --query "Items[*].[id.S]" \
    --output text | \
while read -r id; do
    aws dynamodb delete-item \
        --table-name products \
        --key "{\"id\": {\"S\": \"$id\"}}"
done && echo "Cleaned 'products' table."

# Clean the 'stocks' table
echo "Cleaning the 'stocks' table..."

aws dynamodb scan \
    --table-name stocks \
    --query "Items[*].[product_id.S]" \
    --output text | \
while read -r product_id; do
    aws dynamodb delete-item \
        --table-name stocks \
        --key "{\"product_id\": {\"S\": \"$product_id\"}}"
done && echo "Cleaned 'stocks' table."

# Insert test data into the 'products' table
echo "Inserting data into the 'products' table..."

aws dynamodb put-item \
    --table-name products \
    --item '{
        "id": {"S": "19ba3d6a-f8ed-491b-a192-0a33b71b38c4"},
        "title": {"S": "Product 1"},
        "description": {"S": "This is product 1"},
        "price": {"N": "100"}
    }' && echo "Inserted Product 1 into 'products' table."

aws dynamodb put-item \
    --table-name products \
    --item '{
        "id": {"S": "28ba3d6a-f8ed-491b-a192-0a33b71b38c5"},
        "title": {"S": "Product 2"},
        "description": {"S": "This is product 2"},
        "price": {"N": "200"}
    }' && echo "Inserted Product 2 into 'products' table."

aws dynamodb put-item \
    --table-name products \
    --item '{
        "id": {"S": "37ba3d6a-f8ed-491b-a192-0a33b71b38c6"},
        "title": {"S": "Product 3"},
        "description": {"S": "This is product 3"},
        "price": {"N": "300"}
    }' && echo "Inserted Product 3 into 'products' table."

aws dynamodb put-item \
    --table-name products \
    --item '{
        "id": {"S": "46ba3d6a-f8ed-491b-a192-0a33b71b38c7"},
        "title": {"S": "Product 4"},
        "description": {"S": "This is product 4"},
        "price": {"N": "400"}
    }' && echo "Inserted Product 4 into 'products' table."

# Insert test data into the 'stocks' table
echo "Inserting data into the 'stocks' table..."

aws dynamodb put-item \
    --table-name stocks \
    --item '{
        "product_id": {"S": "19ba3d6a-f8ed-491b-a192-0a33b71b38c4"},
        "count": {"N": "10"}
    }' && echo "Inserted stock for Product 1 into 'stocks' table."

aws dynamodb put-item \
    --table-name stocks \
    --item '{
        "product_id": {"S": "28ba3d6a-f8ed-491b-a192-0a33b71b38c5"},
        "count": {"N": "5"}
    }' && echo "Inserted stock for Product 2 into 'stocks' table."

aws dynamodb put-item \
    --table-name stocks \
    --item '{
        "product_id": {"S": "37ba3d6a-f8ed-491b-a192-0a33b71b38c6"},
        "count": {"N": "15"}
    }' && echo "Inserted stock for Product 3 into 'stocks' table."

aws dynamodb put-item \
    --table-name stocks \
    --item '{
        "product_id": {"S": "46ba3d6a-f8ed-491b-a192-0a33b71b38c7"},
        "count": {"N": "20"}
    }' && echo "Inserted stock for Product 4 into 'stocks' table."

echo "Data insertion completed successfully!"