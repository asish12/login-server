#!/bin/bash

set -e

cd `dirname $0`

set -x

sudo apt-get update
sudo apt-get install slapd ldap-utils
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f src/main/resources/ldap_db_init.ldif
sudo ldapadd -x -D cn=admin,dc=test,dc=com -w password -f src/main/resources/ldap_init.ldif
ldapsearch -h localhost -p 389 -D 'cn=admin,ou=Users,dc=test,dc=com' -w 'adminsecret' -b 'dc=test,dc=com' 'objectClass=*'