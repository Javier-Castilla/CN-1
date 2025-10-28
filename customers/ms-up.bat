call mvnw clean package -DskipTests
docker stop customers-service
docker rm  customers-service
docker build -t  customers-image .
docker run -d -p 8082:8082 --name  customers-service --network ms-network  customers-image
