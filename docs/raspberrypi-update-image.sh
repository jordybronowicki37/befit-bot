ENV_FILE_PATH=~/.befit.env.local

# Clear existing container and image
sudo docker stop befit
sudo docker rm befit
sudo docker rmi befit

# Pull updated image
sudo docker pull ghcr.io/jordybronowicki37/befit:latest
sudo docker tag ghcr.io/jordybronowicki37/befit:latest befit
sudo docker rmi ghcr.io/jordybronowicki37/befit:latest

# Create new container
sudo docker run -d --name befit \
        --network befit \
        --restart on-failure \
        --env-file "$ENV_FILE_PATH" \
        befit
