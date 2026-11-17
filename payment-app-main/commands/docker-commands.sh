docker run -p 5432:5432 \
-e POSTGRES_USER=payment \
-e POSTGRES_PASSWORD=payment \
-e POSTGRES_DB=payment-db \
--volume ./db-scripts:/docker-entrypoint-initdb.d:ro \
--volume postgres-data:/var/lib/postgresql \
--rm \
--name postgresql \
postgres:18.1