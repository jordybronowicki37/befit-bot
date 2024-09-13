# How to build and run the Befit-bot on Docker

1. Add a file for the environment variables. On the path `befit/backend` create a file with the name `.env.local`, inside of the file add the variables seen below and specify the values.
   ```shell
   SPRING_PROFILES_ACTIVE=prod
   SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/befit
   SPRING_DATASOURCE_USERNAME=<DATABASE-USERNAME>
   SPRING_DATASOURCE_PASSWORD=<DATABASE-PASSWORD>
   DISCORD_TOKEN=<TOKEN>
   DISCORD_CHANNELS_MOTIVATIONAL=<CHANNEL-ID>
   DISCORD_CHANNELS_GYM-REMINDER=<CHANNEL-ID>
   DISCORD_GUILDS_MANAGEMENT=<GUILD-ID>
   ```
2. Get an image by either:
   1. Building the image yourself
      ```shell
      sudo docker build -t befit .
      ```
   2. Pulling the image from GitHub
      ```shell
      sudo docker pull ghcr.io/jordybronowicki37/befit:latest
      sudo docker tag ghcr.io/jordybronowicki37/befit:latest befit
      sudo docker rmi ghcr.io/jordybronowicki37/befit:latest
      ```
3. Create a custom docker network
   ```shell
   sudo docker network create befit
   ```
4. Create a container for the database.
   ```shell
   sudo docker run -d --name postgres \
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
   sudo docker run -d --name befit \
       --network befit \
       --restart on-failure \
       --env-file .env.local \
       befit
   ```
