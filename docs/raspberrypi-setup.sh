#!/bin/bash

LOG_PREFIX="\n-------------------------\n>"

# Update system packages.
echo -e "$LOG_PREFIX Updating system packages"
sudo apt-get -y update

# Install some necessary packages.
echo -e "$LOG_PREFIX Install some necessary packages"
sudo apt-get -y install \
        ca-certificates \
        curl \
        gnupg \
        lsb-release

# Add Docker's official GPG key.
echo -e "$LOG_PREFIX Adding the Docker's official GPG key"
curl -fsSL https://download.docker.com/linux/debian/gpg | sudo gpg --dearmor --yes -o /usr/share/keyrings/docker-archive-keyring.gpg

# Set up the stable repository.
echo -e "$LOG_PREFIX Setting up the stable Docker repository"
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian \
    $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Installing Docker.
echo -e "$LOG_PREFIX Installing Docker"
sudo apt-get -y install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Setting up env file.
ENV_FILE_PATH=~/.befit.env.local
if [ ! -e "$ENV_FILE_PATH" ]; then
cat <<EOF > $ENV_FILE_PATH
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/befit
SPRING_DATASOURCE_USERNAME=<DATABASE-USERNAME>
SPRING_DATASOURCE_PASSWORD=<DATABASE-PASSWORD>
DISCORD_TOKEN=<TOKEN>
DISCORD_CHANNELS_MOTIVATIONAL=<CHANNEL-ID>
DISCORD_CHANNELS_GYM-REMINDER=<CHANNEL-ID>
DISCORD_GUILDS_MANAGEMENT=<GUILD-ID>
EOF
fi

# Open the config file in Nano
nano -m $ENV_FILE_PATH

# Check for non filled in params
if grep -q "[=>]$" "$ENV_FILE_PATH"; then
    echo -e "$LOG_PREFIX Error: Some parameters are not filled in. Please complete all parameters."
    exit 1
fi

# Extract the database username and password
DATABASE_USERNAME=$(grep "^SPRING_DATASOURCE_USERNAME=" "$ENV_FILE_PATH" | cut -d'=' -f2)
DATABASE_PASSWORD=$(grep "^SPRING_DATASOURCE_PASSWORD=" "$ENV_FILE_PATH" | cut -d'=' -f2)

# Error handling if values are not found
if [ -z "$DATABASE_USERNAME" ]; then
    echo -e "$LOG_PREFIX Error: SPRING_DATASOURCE_USERNAME not found in $ENV_FILE_PATH"
    exit 1
fi
if [ -z "$DATABASE_PASSWORD" ]; then
    echo -e "$LOG_PREFIX Error: SPRING_DATASOURCE_PASSWORD not found in $ENV_FILE_PATH"
    exit 1
fi

# Create a custom Docker network.
echo -e "$LOG_PREFIX Creating a custom Docker network"
sudo docker network create befit

# Create a container for the database.
echo -e "$LOG_PREFIX Creating a container for the database"
sudo docker run -d --name postgres \
        --network befit \
        --restart on-failure \
        -p 5432:5432 \
        -e POSTGRES_DB=befit \
        -e POSTGRES_USER="$DATABASE_USERNAME" \
        -e POSTGRES_PASSWORD="$DATABASE_PASSWORD" \
        -v postgres-data:/var/lib/postgresql/data \
        postgres:latest

# Pull the befit Docker image.
echo -e "$LOG_PREFIX Pulling the befit image"
sudo docker pull ghcr.io/jordybronowicki37/befit:latest
sudo docker tag ghcr.io/jordybronowicki37/befit:latest befit
sudo docker rmi ghcr.io/jordybronowicki37/befit:latest

# Create a container for the befit image.
echo -e "$LOG_PREFIX Creating a container for the befit image"
sudo docker run -d --name befit \
        --network befit \
        --restart on-failure \
        --env-file "$ENV_FILE_PATH" \
        befit
