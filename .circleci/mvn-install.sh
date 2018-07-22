#!/usr/bin/env bash
export MVN_DOWNLOAD="http://mirrors.ocf.berkeley.edu/apache//maven/maven-3/3.5.3/binaries/apache-maven-3.5.3-bin.tar.gz"

echo "Downloading maven from: ${MVN_DOWNLOAD}"

cd /tmp
wget -q ${MVN_DOWNLOAD}
tar -zxvf apache-maven* &>/dev/null
mv apache-maven-*/ /opt
ln -sf /opt/apache-maven-*/bin/mvn /usr/local/bin/
rm -rf /tmp/*

echo "##################################################################################"
mvn --version || exit 99
echo "##################################################################################"
