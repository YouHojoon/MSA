package com.thoughtmechanix.licenses.client;

import com.thoughtmechanix.licenses.model.Organization;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient("organizationService")
public interface OrganizationFeginClient {
    @GetMapping(value = "/organizations/{organizationId}")
    Organization getOrganization(@PathVariable("orgainizationId") String organizationId);
}
