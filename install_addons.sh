# something is broken with apt when using the German locale...
export LC_ALL=C

# update package lists
sudo apt-get update

# install libdvdcss
# (otherwise playback of encrypted DVDs does not work)
sudo wget http://download.videolan.org/pub/libdvdcss/1.2.9/deb/libdvdcss2_1.2.9-1_i386.deb
sudo dpkg -i libdvdcss2_1.2.9-1_i386.deb
sudo rm libdvdcss2_1.2.9-1_i386.deb

# install Google-Earth
# http://dl.google.com/earth/client/current/GoogleEarthLinux.bin
sudo apt-get -y install googleearth-package gcc
make-googleearth-package
sudo dpkg -i googleearth_*
sudo rm googleearth_*
sudo rm GoogleEarthLinux.bin
sed -i -e "s/googleearth-icon/32x32/1" /usr/share/applications/googleearth.desktop

# install Skype
sudo wget http://www.skype.com/go/getskype-linux-deb
sudo dpkg -i skype-debian_*
sudo rm skype-debian_*
cp /usr/share/applications/skype.desktop /home/knoppix/Desktop/

