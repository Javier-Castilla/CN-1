docker stop my_postgres
docker rm my_postgres
docker build -t postgres-example .
docker run -d -p 5432:5432 --name my_postgres --network ms-network postgres-example
