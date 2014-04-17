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

package org.cloudfoundry.identity.uaa.login;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.ui.Model;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * Contains basic information used by the
 * login-server controllers.
 * 
 * @author fhanik
 * 
 */
public abstract class AbstractControllerInfo {
    private final Log logger = LogFactory.getLog(getClass());
    private Map<String, String> links = new HashMap<String, String>();
    private static String DEFAULT_BASE_UAA_URL = "https://uaa.cloudfoundry.com";
    protected static final String HOST = "Host";

    private String baseUrl;

    private String uaaHost;

    /**
     * @param links the links to set
     */
    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    protected void initProperties() {
        setUaaBaseUrl(DEFAULT_BASE_UAA_URL);
    }

    /**
     * @param baseUrl the base uaa url
     */
    public void setUaaBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        try {
            this.uaaHost = new URI(baseUrl).getHost();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not extract host from URI: " + baseUrl);
        }
    }

    protected String getUaaBaseUrl() {
        return baseUrl;
    }

    protected String getUaaHost() {
        return uaaHost;
    }

    protected Map<String, ?> getLinksInfo() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("uaa", getUaaBaseUrl());
        model.put("login", getUaaBaseUrl().replaceAll("uaa", "login"));
        model.putAll(getLinks());
        return model;
    }

    protected HttpHeaders getRequestHeaders(HttpHeaders headers) {
        // Some of the headers coming back are poisonous apparently
        // (content-length?)...
        HttpHeaders outgoingHeaders = new HttpHeaders();
        outgoingHeaders.putAll(headers);
        outgoingHeaders.remove(HOST);
        outgoingHeaders.remove(HOST.toLowerCase());
        outgoingHeaders.set(HOST, getUaaHost());
        logger.debug("Outgoing headers: " + outgoingHeaders);
        return outgoingHeaders;
    }

    protected String extractPath(HttpServletRequest request) {
        String query = request.getQueryString();
        try {
            query = query == null ? "" : "?" + URLDecoder.decode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Cannot decode query string: " + query);
        }
        String path = request.getRequestURI() + query;
        String context = request.getContextPath();
        path = path.substring(context.length());
        if (path.startsWith("/")) {
            // In the root context we have to remove this as well
            path = path.substring(1);
        }
        logger.debug("Path: " + path);
        return path;
    }

    protected void populateBuildAndLinkInfo(Model model) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("links", getLinksInfo());
        model.addAllAttributes(attributes);
        model.addAttribute("links", getLinks());
    }
}
