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
8. Copy the setup script from your local machine to your PI.
   ```shell
   rsync -r -e "ssh -p 22" <LOCAL-PATH>/raspberrypi-setup.sh pi@<IP-ADDRESS>:~
   ```
9. Run the script
   ```shell
   ~/raspberrypi-setup.sh
   ```
10. If you do not have execution rights for the script then run the command below and retry the previous step.
   ```shell
   chmod 755 ~/raspberrypi-setup.sh
   ```
11. During the execution of the script, the text editor `nano` will be opened so that you can make some changes to the parameters. You can save your changes and exit `nano` by typing `CTRL+O`, `ENTER`, `CTRL+X`.
