# How to run the Befit-bot on your RaspberryPi

## Installation steps
1. Get an SD-card (16 GB recommended) and install the Raspberry PI OS Lite (64-bit) Debian port using the [Raspberry Pi Imager](https://www.raspberrypi.com/software/)
2. Make sure that you enabled `ssh`.
3. Make sure that your PI is connected to your local network.
4. SSH into your PI.
   * Password default: 'raspberry'
   * Name default: 'raspberrypi#' (with the '#' being the version of your PI, e.g. 4)
   ```shell
   ssh pi@<NAME-OF-PI>
   ```
5. Change the default password (optional). 
   ```shell
   passwd
   ```
6. Copy the setup script from your local machine to your PI.
   ```shell
   rsync -r -e "ssh -p 22" <LOCAL-PATH>/raspberrypi-setup.sh pi@<IP-ADDRESS>:~
   ```
7. Run the script
   ```shell
   ~/raspberrypi-setup.sh
   ```
8. If you do not have execution rights for the script then run the command below and retry the previous step.
   ```shell
   chmod 755 ~/raspberrypi-setup.sh
   ```
9. During the execution of the script, the text editor `nano` will be opened so that you can make some changes to the parameters. You can save your changes and exit `nano` by typing `CTRL+O`, `ENTER`, `CTRL+X`.

## Want to be more hands on?
If you do not want an automated script to make all the adjustments, you can also follow the [manual](./how-to-run-on-raspberrypi-using-docker-manual.md) guide

## Updating the image
If you want to update the befit image, simply run the `raspberrypi-update-image.sh` script on your PI.
