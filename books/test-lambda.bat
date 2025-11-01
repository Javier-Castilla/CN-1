call ./mvnw clean package -X -DskipTests
call docker stop books-lambda
call docker rm books-lambda
call docker build --no-cache -t books-lambda-java -f Dockerfile-Lambda .
call docker run -d -p 9000:8080 --network ms-network --name books-lambda -e DB_HOST=my_postgres -e DB_TYPE=postgresql -e DB_PORT=5432 -e DB_NAME=example_db -e DB_USERNAME=admin -e DB_PASSWORD=admin123 books-lambda-java