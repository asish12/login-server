#!/bin/bash

set -e

cd `dirname $0`/../..

set -x

echo "
ldap:
  profile:
    file:ldap/ldap-search-and-compare.xml
  base:
    url: 'ldap://localhost:10389/'
    userDn: 'cn=admin,ou=Users,dc=test,dc=com'
    password: 'password'
    searchBase: ''
    searchFilter: 'cn={0}'
    passwordAttributeName: userPassword
    passwordEncoder: org.cloudfoundry.identity.uaa.login.ldap.DynamicPasswordComparator
    localPasswordCompare: true
">> src/main/resources/login.yml
