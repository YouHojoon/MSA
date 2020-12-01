package com.thoughmechanix.organization.controller;

import com.thoughmechanix.organization.domain.Organization;
import com.thoughmechanix.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/organizations")
@Slf4j
public class OrganizationController {
    private final OrganizationService organizationService;

    @GetMapping(value = "/{organizationId}")
    public Organization getOrganization(@PathVariable("organizationId") String organizationId) {
        return organizationService.getOrganization(organizationId);
    }
    @PostMapping(value="/{organizationId}")
    public void saveOrganization(@RequestBody Organization org) {
        organizationService.saveOrg( org );
    }
}
