call mvnw clean package -DskipTests -U -X
docker stop monolith
docker rm  monolith
docker build -t  customers-image .
docker run -d -p 8080:8080 --name  monolith --network ms-network  customers-image
