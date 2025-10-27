call mvnw clean package -DskipTests
docker stop orders-service
docker rm  orders-service
docker build -t  orders-image .
docker run -d -p 8082:8080 --name  orders-service --network ms-network  orders-image
