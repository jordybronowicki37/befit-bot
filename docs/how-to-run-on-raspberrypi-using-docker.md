# How to run the Befit-bot on your RaspberryPi

1. Get an SD-card and install the Raspberry PI OS Lite (64-bit) Debian port using the [Raspberry Pi Imager](https://www.raspberrypi.com/software/)
2. Make sure that you enabled `ssh`.
3. Make sure that your PI is connected to your local network.
4. Scan your network to find the exact IP-address using a command like:
   ```
   nmap -sn 192.168.1.0/24
   ```
5. Look at the output for the name of your PI and save the IP-address.
6. SSH into your PI.
   * Password default: 'raspberry'
   ```
   ssh pi@<IP-ADDRESS>
   ```
7. Change the default password (optional). 
   ```
   passwd
   ```
8. Update system packages.
   ```
    sudo apt-get update
   ```
9. Install some necessary packages. 
    ```
    sudo apt-get install \
        ca-certificates \
        curl \
        gnupg \
        lsb-release
    ```
10. Add Docker's official GPG key: 
    ```
    curl -fsSL https://download.docker.com/linux/debian/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
    ```
11. Set up the stable repository. 
    ```
    echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian \
    $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    ```
12. Install Docker.
    ```
    sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    ```
13. Verify Docker is running correctly.
    ```
    sudo docker run hello-world
    ```
14. Create a new file for you environment variables.
    ```
    touch .env.local
    ```
15. Open the file in a text editor of your choosing. Like for example `vi`.
    ```
    vi .env.local
    ```
16. Enter insert mode in `vi` by pressing the `i` key.
17. Inside the file add the variables seen below and specify the values.
    ```
    SPRING_PROFILES_ACTIVE=prod
    SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/befit
    SPRING_DATASOURCE_USERNAME=<DATABASE-USERNAME>
    SPRING_DATASOURCE_PASSWORD=<DATABASE-PASSWORD>
    DISCORD_TOKEN=<TOKEN>
    DISCORD_CHANNELS_MOTIVATIONAL=<CHANNEL-ID>
    DISCORD_CHANNELS_GYM-REMINDER=<CHANNEL-ID>
    DISCORD_GUILDS_MANAGEMENT=<GUILD-ID>
    ```
18. Exit insert mode in `vi` by pressing the `ESC` key.
19. Save changes and exit `vi` by typing `:wq`.
20. Pull the Docker image.
    ```
    sudo docker pull ghcr.io/jordybronowicki37/befit:latest
    ```
21. Create a custom docker network.
    ```
    sudo docker network create befit
    ```
22. Create a container for the database.
    ```
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
23. Create a container for the `befit` image.
    ```
    sudo docker run -d --name befit \
        --network befit \
        --restart on-failure \
        --env-file .env.local \
        ghcr.io/jordybronowicki37/befit
    ```
