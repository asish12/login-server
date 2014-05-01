/*******************************************************************************
 *     Cloud Foundry
 *     Copyright (c) [2009-2014] Pivotal Software, Inc. All Rights Reserved.
 *
 *     This product is licensed to you under the Apache License, Version 2.0 (the "License").
 *     You may not use this product except in compliance with the License.
 *
 *     This product includes a number of subcomponents with
 *     separate copyright notices and license terms. Your use of these
 *     subcomponents is subject to the terms and conditions of the
 *     subcomponent's license, as noted in the LICENSE file.
 *******************************************************************************/

package org.cloudfoundry.identity.uaa.keystone;

import java.util.Arrays;
import java.util.Map;

import org.cloudfoundry.identity.uaa.login.RemoteUaaAuthenticationManager;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public class KeystoneAuthenticationManager extends RemoteUaaAuthenticationManager {

    public KeystoneAuthenticationManager() {
        super();
    }

    @Override
    protected HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return headers;
    }

    
    @Override
    protected boolean evaluateResponse(Authentication authentication, ResponseEntity<Map> response) {
        Map<String, Object> map = (Map<String, Object>)response.getBody().get("access");
        Map<String, Object> user = (Map<String, Object>)map.get("user");
        return (authentication.getPrincipal().toString().equals(user.get("username")));
    }

    @Override
    protected KeystoneAuthenticationRequest getParameters(String username, String password) {
        // {"auth":{"tenantName": "", "passwordCredentials": {"username": "marissa","password": "koala"}}}
        return new KeystoneAuthenticationRequest("", username, password);
    }

    
    // {"auth":{"tenantName": "", "passwordCredentials": {"username": "marissa",
    // "password": "koala"}}}
    public static class KeystoneAuthenticationRequest {
        private KeystoneAuthentication auth;

        public KeystoneAuthenticationRequest(String tenant, String username, String password) {
            auth = new KeystoneAuthentication(tenant, username, password);
        }

        public KeystoneAuthenticationRequest(KeystoneAuthentication auth) {
            this.auth = auth;
        }

        @JsonProperty("auth")
        public KeystoneAuthentication getAuth() {
            return auth;
        }

        @JsonProperty("auth")
        public void setAuth(KeystoneAuthentication auth) {
            this.auth = auth;
        }

    }

    public static class KeystoneAuthentication {
        private String tenant;
        private KeystoneCredentials credentials;

        public KeystoneAuthentication(String tenant, String username, String password) {
            this.tenant = tenant;
            this.credentials = new KeystoneCredentials(username, password);
        }

        @JsonProperty("tenantName")
        public String getTenant() {
            return tenant;
        }

        @JsonProperty("tenantName")
        public void setTenant(String tenant) {
            this.tenant = tenant;
        }

        @JsonProperty("passwordCredentials")
        public KeystoneCredentials getCredentials() {
            return credentials;
        }

        public void setCredentials(KeystoneCredentials credentials) {
            this.credentials = credentials;
        }
    }
    
    public static class KeystoneCredentials {
        private String username;
        private String password;

        public KeystoneCredentials(String username, String password) {
            super();
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }


}
