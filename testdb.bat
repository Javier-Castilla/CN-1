docker build -t postgres-example .
docker run -d -p 5432:5432 --name my_postgres postgres-example
