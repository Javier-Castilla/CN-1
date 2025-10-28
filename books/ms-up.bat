call mvnw clean package -DskipTests
docker stop books-service
docker rm  books-service
docker build -t  books-image .
docker run -d -p 8081:8081 --name  books-service --network ms-network  books-image
