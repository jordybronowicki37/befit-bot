# How to run the Befit-bot on your RaspberryPi

1. Get an SD-card and install the Raspberry PI OS Lite (64-bit) Debian port using the [Raspberry Pi Imager](https://www.raspberrypi.com/software/)
2. Make sure that you enabled `ssh`.
3. Make sure that your PI is connected to your local network.
4. Scan your network to find the exact IP-address using a command like:
   ```shell
   nmap -sn 192.168.1.0/24
   ```
5. Look at the output for the name of your PI and save the IP-address.
6. SSH into your PI.
   * Password default: 'raspberry'
   ```shell
   ssh pi@<IP-ADDRESS>
   ```
7. Change the default password (optional). 
   ```shell
   passwd
   ```
8. Update system packages.
   ```shell
    sudo apt-get update
   ```
9. Install some necessary packages. 
    ```shell
    sudo apt-get install \
        ca-certificates \
        curl \
        gnupg \
        lsb-release
    ```
10. Add Docker's official GPG key: 
    ```shell
    curl -fsSL https://download.docker.com/linux/debian/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
    ```
11. Set up the stable repository. 
    ```shell
    echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian \
    $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    ```
12. Install Docker.
    ```shell
    sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    ```
13. Verify Docker is running correctly.
    ```shell
    sudo docker run hello-world
    ```
14. Open the file in a text editor of your choosing. Like for example `nano`.
    ```shell
    nano ~/.env.local
    ```
15. Inside the file add the variables seen below and specify the values.
    ```shell
    SPRING_PROFILES_ACTIVE=prod
    SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/befit
    SPRING_DATASOURCE_USERNAME=<DATABASE-USERNAME>
    SPRING_DATASOURCE_PASSWORD=<DATABASE-PASSWORD>
    DISCORD_TOKEN=<TOKEN>
    DISCORD_GUILDS_MANAGEMENT=<GUILD-ID>
    ```
16. Save changes and exit `nano` by typing `CTRL+O`, `ENTER`, `CTRL+X`.
17. Pull the Docker image.
    ```shell
    sudo docker pull ghcr.io/jordybronowicki37/befit:latest
    ```
18. Create a custom docker network.
    ```shell
    sudo docker network create befit
    ```
19. Create a container for the database.
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
20. Create a container for the `befit` image.
    ```shell
    sudo docker run -d --name befit \
        --network befit \
        --restart on-failure \
        --env-file ~/.env.local \
        ghcr.io/jordybronowicki37/befit
    ```
