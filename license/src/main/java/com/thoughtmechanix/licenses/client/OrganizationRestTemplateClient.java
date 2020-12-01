package com.thoughtmechanix.licenses.client;

import com.thoughtmechanix.licenses.model.Organization;
import com.thoughtmechanix.licenses.repository.OrganizationRedisRepository;
import com.thoughtmechanix.licenses.util.UserContext;
import com.thoughtmechanix.licenses.util.UserContextHolder;
import jdk.internal.org.jline.utils.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class OrganizationRestTemplateClient {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrganizationRedisRepository redisRepository;

    private Organization checkCache(String organizationId){
        try{
            return redisRepository.findOrganization(organizationId);
        }catch (Exception e){
            log.error("Error encountered while trying to retrieve organization {} check Redis Cache." +
                    "Exception {}",organizationId, e);
            return null;
        }
    }
    /*
        OAuth2RestTemplate restTemplate;//JWT 토큰 안쓸 때는 요거 쓰면 됨
     */

    public Organization getOrganization(String organizationId) {
        Organization organization= checkCache(organizationId);
        if(organization != null){
            log.info("I have successfully retrieved an organization {} from the reids cache: {}", organizationId, organization);
        }
        ResponseEntity<Organization> restExchange = restTemplate.exchange(
                "http://zuulServer:5555/organization/organizations/{organizationId}",
                HttpMethod.GET, null, Organization.class, organizationId);
        organization=restExchange.getBody();
        if(organization!= null)
            redisRepository.saveOrganization(organization);

        return organization;
    }
}
