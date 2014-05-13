#!/bin/bash

set -e

cd `dirname $0`/../..

set -x

sudo apt-get -qy update
sudo apt-get -qy install slapd ldap-utils
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f src/main/resources/ldap_db_init.ldif
sudo ldapadd -x -D cn=admin,dc=test,dc=com -w password -f src/main/resources/ldap_init.ldif
