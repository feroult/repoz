#!/bin/bash -xe

export LC_ALL=en_US.UTF-8
export DEBIAN_FRONTEND=noninteractive

############
mkdir -p repoz-repo packs/WEB-INF/classes 
sudo mount /dev/disk/by-id/google-repoz-repository repoz-repo
chown -R repoz:repoz $HOME
if [ ! -f repoz-repo/repoz.passwd ]; then
	echo "File not found: repoz.passwd"
	exit 1;
fi
if [ ! -f repoz-repo/repoz.properties ]; then
	echo "No repoz.properties found in repoz-repo/repoz.properties"
	exit 1;
fi

############
echo "repoz:$(cat repoz-repo/repoz.passwd)" | sudo chpasswd
chmod -v 600 repoz-repo/repoz.passwd
sudo sed -i.sedbak "s/^PasswordAuthentication .*/PasswordAuthentication yes/g" /etc/ssh/sshd_config
sudo service ssh reload

# Public Keys
echo "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDPGpAUfOPunVPuW+Kfb4rgmx5DNogD9gzktWWfWRqGUrMqChzM439d90dfhXY1Yc1pa00arGRJyDg/1qj5HtD6WGpkmRKBNvA+7/DoijC6a9wzhbdtJX6MJXpX7hv+gM3RT7pz5Neq6/+GTy1/njJBuBo2qGxhQ3LlCXgwVuCFar+8r9vI8L99xyeezp8hTJI+00CWeBx9sEJAGNo+L0tBcExh0iE30AegvEBWriZfwJHw460Xf/r0t/D0S8CjZMVxqCOe4dlGSFzZHo80+Xak8OzrCM5aZaLTd80zk1di44me1fIdiTj4HDVLhemkAXsutvPnucl6mu01TLYVf+b3 murer@frostnova" >> .ssh/authorized_keys
echo "ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEApof0ZqvZPwq04ul4RS27elpp4WoeCH9tKBJMO9z8EJ09mfZCDAjAmLOjzED2aiNRXSs43ixDTraUtdlRdfR9ozJx07yDJ06f062v/vvasVld0NsLkBKyrk4jTrPf1aFYvhiXKaSbDXaXy7rW/ALB8PC12yYXbnnsxaUtIfPEjgCQG9tznjByeYZvFrdgu+w7sfkU5A4bSwsBb7KAo3//iCbmDDCEJ6T9D16h2vhKiEeg/taB4SEVpGYTAI5lTJ+JtI5h307nHdFb8srX4pIcT+tka38hnGRmZNsAscEQA0ijNOr2Vt/P/DgNob9I2iEUmoBLJ42691X/36qPYry5dQ== murer@dls185" >> .ssh/authorized_keys

############
REPOZ_PASSWORD=$(grep "^repoz\.access\.root=.\+$" repoz-repo/repoz.properties | sed "s/^repoz\.access\.root=\(.\+\)$/\1/g")
if [ "x$REPOZ_PASSWORD" == "x" ]; then
	echo "repoz.access.root not found in repoz-repo/repoz.properties";
	exit 1;
fi
mkdir repoz-repo/repository | cat
cp repoz-repo/repoz.properties packs/WEB-INF/classes/repoz.properties

############
echo "America/Sao_Paulo" | sudo tee /etc/timezone
sudo dpkg-reconfigure --frontend noninteractive tzdata
sudo service cron restart

############
sudo apt-get -y update
#sudo apt-get -y upgrade
sudo apt-get -y install vim nmap netcat tcpdump zip pv apache2-utils apache2 curl ntpdate psmisc

############
sudo service ntp stop
sudo ntpdate ntp.ubuntu.com
sudo service ntp start
sudo service cron restart
date

cd /opt

############
sudo wget http://storage.googleapis.com/dextra-pdoc-pub/repo/ext/br/com/portaldedocumentos/ext/jdk/7u45-linux-x64/jdk-7u45-linux-x64.zip -O jdk.zip

sudo unzip jdk.zip
sudo ln -s jdk1.7.0_45 jdk

sudo tee /etc/bash.bashrc.repoz <<-EOF
export JAVA_HOME=/opt/jdk
export PATH=\$JAVA_HOME/bin:$PATH
EOF

echo "# repoz" | sudo tee -a /etc/bash.bashrc
echo "source /etc/bash.bashrc.repoz" | sudo tee -a /etc/bash.bashrc

source /etc/bash.bashrc.repoz

java -version

############
sudo a2enmod proxy proxy_http

sudo htpasswd -bc /etc/apache2/passwd root "$REPOZ_PASSWORD"

sudo rm /etc/apache2/sites-enabled/000-default
sudo tee /etc/apache2/sites-available/repoz <<-EOF
<Location /repoz>
    ProxyPass http://localhost:8080/repoz
    ProxyPassReverse http://localhost:8080/repoz
</Location>
<Location /home/repoz>
    AuthType Basic
    AuthName "repoz"
    AuthUserFile /etc/apache2/passwd
    Require user root
</Location>
<VirtualHost *:80>
    DocumentRoot /var/www
    <Directory />
        Options FollowSymLinks
        AllowOverride None
    </Directory>
    Alias /home/repoz "/home/repoz"
    <Directory /home/repoz>
        Options Indexes FollowSymLinks MultiViews
        AllowOverride None
        Order allow,deny
        allow from all
    </Directory>
</VirtualHost>
EOF
sudo cp /var/www/index.html /var/www/index.html.old
sudo tee /var/www/index.html <<-EOF
<html><head><title>Repoz</title></head><body>
<a href="repoz">repoz</a><br/>
<a href="home/repoz">home/repoz</a>
</body></html>
EOF
sudo ln -s /etc/apache2/sites-available/repoz /etc/apache2/sites-enabled/repoz
sudo service apache2 restart

cd -

############
mkdir opt
cd opt
wget http://storage.googleapis.com/dextra-pdoc-pub/repo/ext/br/com/portaldedocumentos/ext/jboss-as/7.1.1.Final/jboss-as-7.1.1.Final.zip -O jboss.zip
unzip jboss.zip
ln -s jboss-as-7.1.1.Final jboss

cd -

echo "install-instance done"




