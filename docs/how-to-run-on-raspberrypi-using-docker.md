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
14. Create a new directory for your code projects. For example on the path `home/pi/Code`.
    ```
    mkdir Code
    ```
15. Add a file for the environment variables. On the path `befit/backend` create a file with the name `.env.local`, inside op the file add the variables seen below and specify the values.
    ```
    SPRING_PROFILES_ACTIVE=dev
    SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/befit
    SPRING_DATASOURCE_USERNAME=<DATABASE-USERNAME>
    SPRING_DATASOURCE_PASSWORD=<DATABASE-PASSWORD>
    DISCORD_TOKEN=<TOKEN>
    DISCORD_CHANNELS_MOTIVATIONAL=<CHANNEL-ID>
    DISCORD_CHANNELS_GYM-REMINDER=<CHANNEL-ID>
    ```
16. On a separate CMD terminal. Copy the code from your local repository to your PI.
    ```
    rsync -r -e "ssh -p 22" <LOCAL-PATH>/befit pi@<IP-ADDRESS>:/home/pi/Code
    ```
17. Navigate into the project folder.
    ```
    cd Code/befit/backend
    ```
18. Build the Docker image
    ```
    sudo docker build -t befit .
    ```
19. Create a custom docker network
    ```
    sudo docker network create befit
    ```
20. Create a container for the database.
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
21. Create a container for the `befit` image we just created.
    ```
    sudo docker run -d --name befit \
        --network befit \
        --restart on-failure \
        --env-file .env.local \
        befit
    ```
