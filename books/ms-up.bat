./mvnw clean package -DskipTests
docker stop books-service
docker rm  books-service
docker build -t  books-image .
docker run -d -p 8080:8080 --name  books-service --network ms-network  books-image
