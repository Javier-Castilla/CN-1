call mvnw clean package -DskipTests
docker stop orders-service
docker rm  orders-service
docker build -t  orders-image .
docker run -d -p 8083:8083 --name  orders-service --network ms-network  orders-image
