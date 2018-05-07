#!/usr/bin/env bash

echo "installing dependencies"
apt-get install -qqy openjdk-8-jdk-headless wget
update-ca-certificates -f &>/dev/null

# maven
cd /tmp
wget -q http://mirror.cogentco.com/pub/apache/maven/maven-3/3.5.2/binaries/apache-maven-3.5.2-bin.tar.gz
tar -zxvf apache-maven* &>/dev/null
mv apache-maven-3.5.2/ /opt
ln -sf /opt/apache-maven-3.5.2/bin/mvn /usr/local/bin/
rm -rf /tmp/*

# running tests
cd ~/ci
mvn -q test