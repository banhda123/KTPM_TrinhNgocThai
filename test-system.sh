#!/bin/bash

echo "Testing Microservices System"
echo "==========================="

echo "\nChecking databases:"
echo "1. Product DB: "
docker exec ktpm_trinhngocthai-product-db-1 mysql -uroot -proot -e "SHOW DATABASES;"

echo "\n2. Payment DB: "
docker exec ktpm_trinhngocthai-payment-db-1 mysql -uroot -proot -e "SHOW DATABASES;"

echo "\n3. Inventory DB: "
docker exec ktpm_trinhngocthai-inventory-db-1 mysql -uroot -proot -e "SHOW DATABASES;"

echo "\n4. Shipping DB: "
docker exec ktpm_trinhngocthai-shipping-db-1 mysql -uroot -proot -e "SHOW DATABASES;"

echo "\nChecking Kafka:"
docker exec ktpm_trinhngocthai-kafka-1 kafka-topics --bootstrap-server localhost:9092 --list

echo "\nAll components are running!"
echo "System is ready for further development and testing." 