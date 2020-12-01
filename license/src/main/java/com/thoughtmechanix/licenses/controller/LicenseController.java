package com.thoughtmechanix.licenses.controller;

import com.thoughtmechanix.licenses.domain.License;
import com.thoughtmechanix.licenses.service.LicenseService;
import com.thoughtmechanix.licenses.util.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/{organizationId}/licenses")
@Slf4j
public class LicenseController {
    private final LicenseService licenseService;
    @GetMapping("/{licenseId}/{clientType}")
    public License getLicensesWithClient(@PathVariable("organizationId") String organizationId,
                                         @PathVariable("licenseId") String licenseId,
                                         @PathVariable("clientType") String clientType){
        return licenseService.getLicense(organizationId,licenseId,clientType);

    }
    @GetMapping
    public List<License> getLicenseList(@PathVariable("organizationId") String organizationId){
        return licenseService.getLicenseListByOrganizationId(organizationId);
    }
}
