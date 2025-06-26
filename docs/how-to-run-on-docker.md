# How to build and run the Befit-bot on Docker

## Run production using Docker compose
1. Copy the file `example.env` and rename it as `.env`. Inside the file you must fill in all values.
2. Build the project
   ```shell
   docker compose -f docker-compose.yml up -d
   ```

## Run production using separately deployed containers
1. Copy the file `example.env` and rename it as `.env`. Inside the file you must fill in all values.
2. Get an image by either:
   1. Building the image yourself
      ```shell
      docker build -t befit ./backend
      ```
   2. Pulling the image from GitHub
      ```shell
      docker pull ghcr.io/jordybronowicki37/befit:latest
      docker tag ghcr.io/jordybronowicki37/befit:latest befit
      docker rmi ghcr.io/jordybronowicki37/befit:latest
      ```
3. Create a custom docker network
   ```shell
   docker network create befit
   ```
4. Create a container for the database.
   ```shell
   docker run -d --name postgres \
       --network befit \
       --restart on-failure \
       -p 5432:5432 \
       -e POSTGRES_DB=befit \
       -e POSTGRES_USER=<DATABASE-USERNAME> \
       -e POSTGRES_PASSWORD=<DATABASE-PASSWORD> \
       -v postgres-data:/var/lib/postgresql/data \
       postgres:latest
   ```
5. Create a container for the `befit` image we just created.
   ```shell
   docker run -d --name befit \
       --network befit \
       --restart on-failure \
       --env-file .env \
       befit
   ```

## Run development using Docker compose
1. Copy the file `example.env.local` and rename it as `.env.local`. Inside the file you must fill in all values.
2. Build the project
   ```shell
   docker compose -f docker-compose-local.yml up
   ```
