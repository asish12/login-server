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
import org.cloudfoundry.identity.uaa.oauth.approval.Approval;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UaaApprovalsService implements ApprovalsService {

    private final Log logger = LogFactory.getLog(getClass());

    private final RestOperations restTemplate;
    private final String approvalsUrl;

    public UaaApprovalsService(RestOperations restTemplate, String approvalsUrl) {
        this.restTemplate = restTemplate;
        this.approvalsUrl = approvalsUrl;
    }

    @Override
    public Map<String, List<DescribedApproval>> getCurrentApprovalsByClientId() {
        Map<String, List<DescribedApproval>> result = new HashMap<String, List<DescribedApproval>>();
        ResponseEntity<Set<DescribedApproval>> approvalsResponse = restTemplate.exchange(approvalsUrl, HttpMethod.GET, null, new ParameterizedTypeReference<Set<DescribedApproval>>() {});
        Set<DescribedApproval> approvals = approvalsResponse.getBody();

        for (DescribedApproval approval : approvals) {
            List<DescribedApproval> clientApprovals = result.get(approval.getClientId());
            if (clientApprovals == null) {
                clientApprovals = new ArrayList<DescribedApproval>();
                result.put(approval.getClientId(), clientApprovals);
            }

            String scope = approval.getScope();
            if (!scope.contains(".")) {
                approval.setDescription("Access your data with scope '" + scope + "'");
                clientApprovals.add(approval);
            } else {
                String resource = scope.substring(0, scope.lastIndexOf("."));
                if ("uaa".equals(resource)) {
                    // special case: don't need to prompt for internal uaa
                    // scopes
                    continue;
                }
                String access = scope.substring(scope.lastIndexOf(".") + 1);
                approval.setDescription("Access your '" + resource + "' resources with scope '" + access + "'");
                clientApprovals.add(approval);
            }
        }
        for (List<DescribedApproval> approvalList : result.values()) {
            Collections.sort(approvalList, new Comparator<DescribedApproval>() {
                @Override
                public int compare(DescribedApproval o1, DescribedApproval o2) {
                    return o1.getScope().compareTo(o2.getScope());
                }
            });
        }
        return result;
    }

    @Override
    public void updateApprovals(List<DescribedApproval> approvals) {
        restTemplate.put(approvalsUrl, approvals);
    }

    @Override
    public void deleteApprovalsForClient(String clientId) {
        ResponseEntity<String> response = restTemplate.exchange(approvalsUrl + "?clientId=" + clientId,
                HttpMethod.DELETE, null, String.class);
        logger.debug("Delete approvals request for client " + clientId + " resulted in " + response);
    }

    public static class DescribedApproval extends Approval {
        private String description;

        @JsonIgnore
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
